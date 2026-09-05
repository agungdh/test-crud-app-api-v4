package id.my.agungdh.testcrudappapiv4.common;

import java.util.List;
import java.util.UUID;

/**
 * Cursor page for infinite scroll. FE flow: call without {@code cursor} for
 * the first page, then pass back {@code nextCursor} until {@code hasNext}
 * is {@code false} (where {@code nextCursor} is {@code null}).
 */
public record CursorPageResponse<T>(List<T> content, UUID nextCursor, boolean hasNext, int size) {
}
