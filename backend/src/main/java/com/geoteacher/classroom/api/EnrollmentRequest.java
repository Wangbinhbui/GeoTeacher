package com.geoteacher.classroom.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentRequest(
        @NotNull UUID classGroupId,
        @NotNull UUID studentId,
        @NotNull @PastOrPresent LocalDate enrolledAt
) {
}
