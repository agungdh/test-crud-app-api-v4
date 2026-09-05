package id.my.agungdh.testcrudappapiv4.person.dto;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record PersonResponse(
        UUID uuid,
        String name,
        String address,
        LocalDate birthDate,
        Boolean male,
        Instant createdAt,
        Instant updatedAt) {
}
