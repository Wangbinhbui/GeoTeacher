package com.geoteacher.question.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record QuestionArchiveRequest(
        @NotNull @PositiveOrZero Long version
) {
}
