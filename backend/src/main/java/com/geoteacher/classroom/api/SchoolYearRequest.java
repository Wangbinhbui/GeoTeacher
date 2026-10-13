package com.geoteacher.classroom.api;

import com.geoteacher.classroom.domain.SchoolYearStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SchoolYearRequest(
        @NotBlank @Size(max = 30) String code,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull SchoolYearStatus status,
        Long version
) {
}
