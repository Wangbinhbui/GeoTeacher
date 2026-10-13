package com.geoteacher.classroom.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ArchiveRequest(@NotNull @PositiveOrZero Long version) {
}
