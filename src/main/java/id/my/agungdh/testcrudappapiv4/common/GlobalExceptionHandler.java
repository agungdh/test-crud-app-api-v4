package id.my.agungdh.testcrudappapiv4.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converts every validation/binding failure to snake_case {@code field} names
 * so FE can map errors 1:1 to the JSON properties it sent.
 *
 * <p>Jackson's global {@code SNAKE_CASE} strategy only converts JSON
 * <em>keys</em> — the <em>value</em> of Spring's default {@code field} string
 * stays camelCase ({@code birthDate}). Hence the explicit translation here.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleBodyValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldViolation> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> errors.add(new FieldViolation(
                toSnakePath(fieldError.getField()), fieldError.getRejectedValue(), fieldError.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(objectError -> errors.add(new FieldViolation(
                toSnakePath(objectError.getObjectName()), null, objectError.getDefaultMessage())));
        return error(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleParamValidation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldViolation> errors = ex.getConstraintViolations().stream()
                .map(violation -> new FieldViolation(
                        toSnakeViolationPath(violation), violation.getInvalidValue(), violation.getMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Malformed JSON request", request, List.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleBadParam(Exception ex, HttpServletRequest request) {
        String field = null;
        Object rejected = null;
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            field = mismatch.getName();
            rejected = mismatch.getValue();
        } else if (ex instanceof MissingServletRequestParameterException missing) {
            field = missing.getParameterName();
        }
        List<FieldViolation> errors = field == null
                ? List.of()
                : List.of(new FieldViolation(toSnakePath(field), rejected, ex.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "Invalid request parameter", request, errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return error(status, message, request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Not found: " + ex.getResourcePath(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleFallback(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String message, HttpServletRequest request, List<FieldViolation> errors) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(), errors));
    }

    /** Translates a dotted/bracketed binding path segment-by-segment to snake_case. */
    static String toSnakePath(String field) {
        if (field == null || field.isEmpty()) {
            return field;
        }
        String[] parts = field.split("\\.", -1);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append('.');
            }
            out.append(toSnakeSegment(parts[i]));
        }
        return out.toString();
    }

    /** Keeps {@code [0]} suffixes untouched, translates only the identifier. */
    private static String toSnakeSegment(String segment) {
        int bracket = segment.indexOf('[');
        String head = bracket < 0 ? segment : segment.substring(0, bracket);
        String tail = bracket < 0 ? "" : segment.substring(bracket);
        if (!head.isEmpty()) {
            head = toSnakeCase(head);
        }
        return head + tail;
    }

    /** Minimal camelCase → snake_case (no Jackson dependency across v2/v3). */
    static String toSnakeCase(String input) {
        int len = input.length();
        StringBuilder out = new StringBuilder(len + 8);
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    out.append('_');
                }
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** Drops the method-name prefix ({@code create.arg0.birthDate} → {@code birth_date}). */
    private static String toSnakeViolationPath(ConstraintViolation<?> violation) {
        List<String> nodes = new ArrayList<>();
        for (Path.Node node : violation.getPropertyPath()) {
            if (node.getKind() == ElementKind.METHOD || node.getKind() == ElementKind.CONSTRUCTOR) {
                continue;
            }
            if (node.isInIterable()) {
                nodes.add(node.getName() + "[" + node.getIndex() + "]");
            } else if (node.getName() != null) {
                nodes.add(node.getName());
            }
        }
        if (nodes.isEmpty()) {
            return toSnakePath(violation.getPropertyPath().toString());
        }
        // Strip a leading synthetic arg name (arg0, arg1, ...) when present.
        if (!nodes.isEmpty() && nodes.get(0).matches("arg\\d+")) {
            nodes.remove(0);
        }
        return toSnakePath(String.join(".", nodes));
    }
}
