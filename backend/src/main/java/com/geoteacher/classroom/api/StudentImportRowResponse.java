package com.geoteacher.classroom.api;

import java.time.LocalDate;
import java.util.List;

public record StudentImportRowResponse(
        int rowNumber,
        String studentCode,
        String fullName,
        LocalDate dateOfBirth,
        String action,
        List<String> errors
) {
}
