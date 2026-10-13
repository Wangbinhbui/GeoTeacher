package com.geoteacher.classroom.application;

import com.geoteacher.classroom.api.ClassGroupRequest;
import com.geoteacher.classroom.api.SchoolYearRequest;
import com.geoteacher.classroom.domain.ClassGroup;
import com.geoteacher.classroom.domain.SchoolYear;
import com.geoteacher.classroom.domain.SchoolYearStatus;
import com.geoteacher.classroom.infrastructure.ClassGroupRepository;
import com.geoteacher.classroom.infrastructure.SchoolYearRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-13T00:00:00Z");

    @Mock
    private SchoolYearRepository schoolYearRepository;

    @Mock
    private ClassGroupRepository classGroupRepository;

    private ClassroomService service;

    @BeforeEach
    void setUp() {
        service = new ClassroomService(
                schoolYearRepository,
                classGroupRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void rejectsDuplicateClassCodeInsideTheSameSchoolYear() {
        var schoolYear = schoolYear("2026-2027");
        when(schoolYearRepository.findById(schoolYear.getId())).thenReturn(Optional.of(schoolYear));
        when(classGroupRepository.existsBySchoolYearIdAndClassCodeNormalized(
                schoolYear.getId(), "10a1"
        )).thenReturn(true);

        assertThatThrownBy(() -> service.createClassGroup(new ClassGroupRequest(
                schoolYear.getId(), "10A1", "Lớp 10A1", 10, null
        )))
                .isInstanceOf(ClassroomException.class)
                .extracting(exception -> ((ClassroomException) exception).getCode())
                .isEqualTo("CLASS_CODE_DUPLICATE_IN_YEAR");
    }

    @Test
    void allowsTheSameClassCodeInAnotherSchoolYear() {
        var schoolYear = schoolYear("2027-2028");
        when(schoolYearRepository.findById(schoolYear.getId())).thenReturn(Optional.of(schoolYear));
        when(classGroupRepository.existsBySchoolYearIdAndClassCodeNormalized(
                schoolYear.getId(), "10a1"
        )).thenReturn(false);
        when(classGroupRepository.saveAndFlush(any(ClassGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createClassGroup(new ClassGroupRequest(
                schoolYear.getId(), "10A1", "Lớp 10A1", 10, null
        ));

        assertThat(result.classCode()).isEqualTo("10A1");
        assertThat(result.schoolYearId()).isEqualTo(schoolYear.getId());
    }

    @Test
    void rejectsStaleSchoolYearUpdate() {
        var schoolYear = schoolYear("2026-2027");
        when(schoolYearRepository.findById(schoolYear.getId())).thenReturn(Optional.of(schoolYear));

        assertThatThrownBy(() -> service.updateSchoolYear(
                schoolYear.getId(),
                new SchoolYearRequest(
                        "2026-2027",
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2027, 5, 31),
                        SchoolYearStatus.ACTIVE,
                        99L
                )
        ))
                .isInstanceOf(ClassroomException.class)
                .extracting(exception -> ((ClassroomException) exception).getCode())
                .isEqualTo("OPTIMISTIC_CONFLICT");
    }

    private SchoolYear schoolYear(String code) {
        return SchoolYear.create(
                code,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2027, 5, 31),
                SchoolYearStatus.ACTIVE,
                NOW
        );
    }
}
