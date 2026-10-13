package com.geoteacher.classroom.api;

import com.geoteacher.classroom.domain.ClassEnrollment;
import com.geoteacher.classroom.domain.EnrollmentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID classGroupId,
        String classCode,
        String classDisplayName,
        String schoolYearCode,
        UUID studentId,
        String studentCode,
        String studentFullName,
        LocalDate enrolledAt,
        LocalDate leftAt,
        EnrollmentStatus status,
        long version
) {
    public static EnrollmentResponse from(ClassEnrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getClassGroup().getId(),
                enrollment.getClassGroup().getClassCode(),
                enrollment.getClassGroup().getDisplayName(),
                enrollment.getClassGroup().getSchoolYear().getCode(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getStudentCode(),
                enrollment.getStudent().getFullName(),
                enrollment.getEnrolledAt(),
                enrollment.getLeftAt(),
                enrollment.getStatus(),
                enrollment.getVersion()
        );
    }
}
