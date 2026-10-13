package com.geoteacher.classroom.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClassGroupRequest(
        @NotNull UUID schoolYearId,
        @NotBlank @Size(max = 30) String classCode,
        @NotBlank @Size(max = 150) String displayName,
        @Min(1) @Max(12) int gradeLevel,
        Long version
) {
}
