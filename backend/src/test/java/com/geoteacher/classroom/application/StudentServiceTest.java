package com.geoteacher.classroom.application;

import com.geoteacher.classroom.api.EnrollmentRequest;
import com.geoteacher.classroom.api.StudentRequest;
import com.geoteacher.classroom.domain.ClassEnrollment;
import com.geoteacher.classroom.domain.ClassGroup;
import com.geoteacher.classroom.domain.SchoolYear;
import com.geoteacher.classroom.domain.SchoolYearStatus;
import com.geoteacher.classroom.domain.Student;
import com.geoteacher.classroom.infrastructure.ClassEnrollmentRepository;
import com.geoteacher.classroom.infrastructure.ClassGroupRepository;
import com.geoteacher.classroom.infrastructure.StudentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-13T00:00:00Z");

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private ClassEnrollmentRepository enrollmentRepository;

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(
                studentRepository,
                classGroupRepository,
                enrollmentRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void rejectsDuplicateStudentCodeAcrossTheSystem() {
        when(studentRepository.existsByStudentCodeNormalized("hs001")).thenReturn(true);

        assertThatThrownBy(() -> service.createStudent(new StudentRequest(
                " HS001 ", "Nguyễn Văn An", LocalDate.of(2010, 3, 12), null
        )))
                .isInstanceOf(ClassroomException.class)
                .extracting(exception -> ((ClassroomException) exception).getCode())
                .isEqualTo("STUDENT_CODE_DUPLICATE");
    }

    @Test
    void allowsAStudentInTwoClassesDuringTheSameSchoolYear() {
        var schoolYear = schoolYear();
        var classA = classGroup(schoolYear, "10A1");
        var classB = classGroup(schoolYear, "10A2");
        var student = student();

        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(classGroupRepository.findById(classA.getId())).thenReturn(Optional.of(classA));
        when(classGroupRepository.findById(classB.getId())).thenReturn(Optional.of(classB));
        when(enrollmentRepository.existsByClassGroupIdAndStudentId(any(), any())).thenReturn(false);
        when(enrollmentRepository.saveAndFlush(any(ClassEnrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var first = service.enroll(new EnrollmentRequest(
                classA.getId(), student.getId(), LocalDate.of(2026, 8, 15)
        ));
        var second = service.enroll(new EnrollmentRequest(
                classB.getId(), student.getId(), LocalDate.of(2026, 9, 1)
        ));

        assertThat(first.classGroupId()).isEqualTo(classA.getId());
        assertThat(second.classGroupId()).isEqualTo(classB.getId());
        assertThat(first.schoolYearCode()).isEqualTo(second.schoolYearCode());
    }

    @Test
    void rejectsDuplicateEnrollmentInsideTheSameClass() {
        var schoolYear = schoolYear();
        var classGroup = classGroup(schoolYear, "10A1");
        var student = student();

        when(classGroupRepository.findById(classGroup.getId())).thenReturn(Optional.of(classGroup));
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByClassGroupIdAndStudentId(classGroup.getId(), student.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.enroll(new EnrollmentRequest(
                classGroup.getId(), student.getId(), LocalDate.of(2026, 8, 15)
        )))
                .isInstanceOf(ClassroomException.class)
                .extracting(exception -> ((ClassroomException) exception).getCode())
                .isEqualTo("ENROLLMENT_DUPLICATE");
    }

    @Test
    void rejectsLeavingBeforeEnrollmentDate() {
        var schoolYear = schoolYear();
        var enrollment = ClassEnrollment.create(
                classGroup(schoolYear, "10A1"), student(), LocalDate.of(2026, 8, 15), NOW
        );
        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.leaveEnrollment(
                enrollment.getId(), LocalDate.of(2026, 8, 14), enrollment.getVersion()
        ))
                .isInstanceOf(ClassroomException.class)
                .extracting(exception -> ((ClassroomException) exception).getCode())
                .isEqualTo("INVALID_ENROLLMENT_STATE");
    }

    private SchoolYear schoolYear() {
        return SchoolYear.create(
                "2026-2027",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2027, 5, 31),
                SchoolYearStatus.ACTIVE,
                NOW
        );
    }

    private ClassGroup classGroup(SchoolYear schoolYear, String code) {
        return ClassGroup.create(schoolYear, code, "Lớp " + code, 10, NOW);
    }

    private Student student() {
        return Student.create("HS001", "Nguyễn Văn An", LocalDate.of(2010, 3, 12), NOW);
    }
}
