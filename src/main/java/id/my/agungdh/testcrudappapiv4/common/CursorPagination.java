package id.my.agungdh.testcrudappapiv4.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Single source of truth for cursor-pagination bounds. Every list endpoint
 * MUST validate its {@code size} query param through {@link #requireValidSize}
 * — never inline {@code 1..100} checks per service — so no endpoint can
 * silently allow oversized pages.
 */
public final class CursorPagination {

    /** Default page size when {@code size} is omitted. */
    public static final int DEFAULT_SIZE = 20;

    /** Hard ceiling for {@code size} on every list endpoint. */
    public static final int MAX_SIZE = 100;

    /**
     * String form of {@link #DEFAULT_SIZE} for
     * {@code @RequestParam(defaultValue = ...)} (annotations need a String
     * constant). Keep in sync with {@link #DEFAULT_SIZE}.
     */
    public static final String DEFAULT_SIZE_VALUE = "20";

    private CursorPagination() {
    }

    /**
     * Rejects out-of-range page sizes with 400. Returns the size unchanged
     * when valid so call sites can use it inline.
     */
    public static int requireValidSize(int size) {
        if (size < 1 || size > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and " + MAX_SIZE);
        }
        return size;
    }
}
