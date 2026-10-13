package com.geoteacher.classroom.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record LeaveEnrollmentRequest(
        @NotNull @PastOrPresent LocalDate leftAt,
        @NotNull @PositiveOrZero Long version
) {
}
