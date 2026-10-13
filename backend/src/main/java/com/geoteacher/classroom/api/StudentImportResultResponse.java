package com.geoteacher.classroom.api;

public record StudentImportResultResponse(
        int createdStudents,
        int enrolledStudents
) {
}
