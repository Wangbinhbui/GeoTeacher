package com.geoteacher.classroom.api;

import com.geoteacher.classroom.domain.ClassGroup;
import com.geoteacher.classroom.domain.ClassGroupStatus;

import java.util.UUID;

public record ClassGroupResponse(
        UUID id,
        UUID schoolYearId,
        String schoolYearCode,
        String classCode,
        String displayName,
        int gradeLevel,
        ClassGroupStatus status,
        long version
) {
    public static ClassGroupResponse from(ClassGroup classGroup) {
        return new ClassGroupResponse(
                classGroup.getId(),
                classGroup.getSchoolYear().getId(),
                classGroup.getSchoolYear().getCode(),
                classGroup.getClassCode(),
                classGroup.getDisplayName(),
                classGroup.getGradeLevel(),
                classGroup.getStatus(),
                classGroup.getVersion()
        );
    }
}
