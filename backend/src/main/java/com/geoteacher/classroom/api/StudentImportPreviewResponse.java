package com.geoteacher.classroom.api;

import java.util.List;

public record StudentImportPreviewResponse(
        boolean valid,
        int totalRows,
        int newStudents,
        int existingStudents,
        List<StudentImportRowResponse> rows
) {
}
