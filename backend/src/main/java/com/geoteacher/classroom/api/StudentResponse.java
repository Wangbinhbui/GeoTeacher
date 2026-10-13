package com.geoteacher.classroom.api;

import com.geoteacher.classroom.domain.Student;
import com.geoteacher.classroom.domain.StudentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        String studentCode,
        String fullName,
        LocalDate dateOfBirth,
        StudentStatus status,
        long version
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentCode(),
                student.getFullName(),
                student.getDateOfBirth(),
                student.getStatus(),
                student.getVersion()
        );
    }
}
