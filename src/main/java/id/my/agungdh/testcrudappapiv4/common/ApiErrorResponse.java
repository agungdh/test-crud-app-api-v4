package id.my.agungdh.testcrudappapiv4.common;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error envelope. Keys are camelCase in Java so the global Jackson
 * {@code SNAKE_CASE} strategy renders them as snake_case
 * ({@code status}, {@code error}, {@code message}, {@code path},
 * {@code timestamp}, {@code errors}).
 */
public record ApiErrorResponse(
        Instant timestamp, int status, String error, String message, String path, List<FieldViolation> errors) {
}
