package com.geoteacher.classroom.api;

import com.geoteacher.classroom.domain.SchoolYear;
import com.geoteacher.classroom.domain.SchoolYearStatus;

import java.time.LocalDate;
import java.util.UUID;

public record SchoolYearResponse(
        UUID id,
        String code,
        LocalDate startDate,
        LocalDate endDate,
        SchoolYearStatus status,
        long version
) {
    public static SchoolYearResponse from(SchoolYear schoolYear) {
        return new SchoolYearResponse(
                schoolYear.getId(),
                schoolYear.getCode(),
                schoolYear.getStartDate(),
                schoolYear.getEndDate(),
                schoolYear.getStatus(),
                schoolYear.getVersion()
        );
    }
}
