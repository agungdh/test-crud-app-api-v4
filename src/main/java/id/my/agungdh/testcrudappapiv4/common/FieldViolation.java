package id.my.agungdh.testcrudappapiv4.common;

/**
 * Single field violation. {@code field} is always snake_case so FE can map it
 * 1:1 to the JSON property it sent (e.g. {@code birth_date}, never
 * {@code birthDate}).
 */
public record FieldViolation(String field, Object rejectedValue, String message) {
}
