package com.geoteacher.question.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionOptionRequest(
        @NotBlank @Size(max = 10) String key,
        @NotBlank @Size(max = 2_000) String content,
        boolean correct
) {
}
