package com.geoteacher.classroom.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequest(
        @NotBlank @Size(max = 50) String studentCode,
        @NotBlank @Size(max = 150) String fullName,
        @PastOrPresent LocalDate dateOfBirth,
        Long version
) {
}
