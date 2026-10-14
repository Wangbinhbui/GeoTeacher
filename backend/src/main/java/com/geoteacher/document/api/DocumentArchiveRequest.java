package com.geoteacher.document.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record DocumentArchiveRequest(
        @NotNull @PositiveOrZero Long version
) {
}
