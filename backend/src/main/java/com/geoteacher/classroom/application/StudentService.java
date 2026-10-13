package com.geoteacher.classroom.application;

import com.geoteacher.classroom.api.EnrollmentRequest;
import com.geoteacher.classroom.api.EnrollmentResponse;
import com.geoteacher.classroom.api.StudentRequest;
import com.geoteacher.classroom.api.StudentResponse;
import com.geoteacher.classroom.domain.ClassEnrollment;
import com.geoteacher.classroom.domain.ClassGroupStatus;
import com.geoteacher.classroom.domain.EnrollmentStatus;
import com.geoteacher.classroom.domain.SchoolYearStatus;
import com.geoteacher.classroom.domain.Student;
import com.geoteacher.classroom.domain.StudentStatus;
import com.geoteacher.classroom.infrastructure.ClassEnrollmentRepository;
import com.geoteacher.classroom.infrastructure.ClassGroupRepository;
import com.geoteacher.classroom.infrastructure.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Điều phối học sinh và ghi danh. Student giữ identity xuyên năm; enrollment giữ lịch sử lớp,
 * vì vậy chuyển/rời lớp không sửa hoặc xóa hồ sơ học sinh.
 */
@Service
public class StudentService {

    private static final int SEARCH_LIMIT = 100;

    private final StudentRepository studentRepository;
    private final ClassGroupRepository classGroupRepository;
    private final ClassEnrollmentRepository enrollmentRepository;
    private final Clock clock;

    public StudentService(
            StudentRepository studentRepository,
            ClassGroupRepository classGroupRepository,
            ClassEnrollmentRepository enrollmentRepository,
            Clock clock
    ) {
        this.studentRepository = studentRepository;
        this.classGroupRepository = classGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> searchStudents(String query) {
        var normalizedQuery = query == null ? "" : Student.normalize(query);
        return studentRepository.search(normalizedQuery, PageRequest.of(0, SEARCH_LIMIT)).stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentCodeNormalized(Student.normalize(request.studentCode()))) {
            throw duplicateStudentCode();
        }
        try {
            return StudentResponse.from(studentRepository.saveAndFlush(Student.create(
                    request.studentCode(), request.fullName(), request.dateOfBirth(), now()
            )));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateStudentCode();
        }
    }

    @Transactional
    public StudentResponse updateStudent(UUID id, StudentRequest request) {
        var student = getStudent(id);
        verifyVersion(student.getVersion(), request.version());
        if (studentRepository.existsByStudentCodeNormalizedAndIdNot(
                Student.normalize(request.studentCode()), id
        )) {
            throw duplicateStudentCode();
        }
        try {
            student.update(request.studentCode(), request.fullName(), request.dateOfBirth(), now());
            return StudentResponse.from(studentRepository.saveAndFlush(student));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateStudentCode();
        } catch (IllegalStateException exception) {
            throw invalidState("STUDENT_ARCHIVED", exception.getMessage());
        }
    }

    @Transactional
    public StudentResponse archiveStudent(UUID id, long version) {
        var student = getStudent(id);
        verifyVersion(student.getVersion(), version);
        if (enrollmentRepository.existsByStudentIdAndStatus(id, EnrollmentStatus.ACTIVE)) {
            throw invalidState(
                    "STUDENT_HAS_ACTIVE_ENROLLMENTS",
                    "Hãy kết thúc các ghi danh đang hoạt động trước khi lưu trữ học sinh."
            );
        }
        student.archive(now());
        return StudentResponse.from(studentRepository.saveAndFlush(student));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listEnrollments(UUID classGroupId, UUID studentId) {
        if ((classGroupId == null) == (studentId == null)) {
            throw invalidState(
                    "ENROLLMENT_FILTER_REQUIRED",
                    "Chỉ cung cấp một trong hai bộ lọc classGroupId hoặc studentId."
            );
        }
        var enrollments = classGroupId != null
                ? enrollmentRepository.findAllByClassGroupIdOrderByStudentFullNameAsc(classGroupId)
                : enrollmentRepository.findAllByStudentIdOrderByEnrolledAtDesc(studentId);
        return enrollments.stream().map(EnrollmentResponse::from).toList();
    }

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        var classGroup = classGroupRepository.findById(request.classGroupId()).orElseThrow(() -> notFound(
                "CLASS_GROUP_NOT_FOUND", "Không tìm thấy lớp học."
        ));
        var student = getStudent(request.studentId());
        if (classGroup.getStatus() == ClassGroupStatus.ARCHIVED
                || classGroup.getSchoolYear().getStatus() == SchoolYearStatus.ARCHIVED) {
            throw invalidState("CLASS_GROUP_ARCHIVED", "Không thể ghi danh vào lớp đã lưu trữ.");
        }
        if (student.getStatus() == StudentStatus.ARCHIVED) {
            throw invalidState("STUDENT_ARCHIVED", "Không thể ghi danh học sinh đã lưu trữ.");
        }
        if (enrollmentRepository.existsByClassGroupIdAndStudentId(
                request.classGroupId(), request.studentId()
        )) {
            throw duplicateEnrollment();
        }
        try {
            return EnrollmentResponse.from(enrollmentRepository.saveAndFlush(ClassEnrollment.create(
                    classGroup, student, request.enrolledAt(), now()
            )));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateEnrollment();
        }
    }

    @Transactional
    public EnrollmentResponse leaveEnrollment(UUID id, LocalDate leftAt, long version) {
        var enrollment = enrollmentRepository.findById(id).orElseThrow(() -> notFound(
                "ENROLLMENT_NOT_FOUND", "Không tìm thấy ghi danh."
        ));
        verifyVersion(enrollment.getVersion(), version);
        try {
            enrollment.leave(leftAt, now());
            return EnrollmentResponse.from(enrollmentRepository.saveAndFlush(enrollment));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw invalidState("INVALID_ENROLLMENT_STATE", exception.getMessage());
        }
    }

    private Student getStudent(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> notFound(
                "STUDENT_NOT_FOUND", "Không tìm thấy học sinh."
        ));
    }

    private void verifyVersion(long currentVersion, Long requestedVersion) {
        if (requestedVersion == null || currentVersion != requestedVersion) {
            throw new ClassroomException(
                    "OPTIMISTIC_CONFLICT",
                    ClassroomErrorType.CONFLICT,
                    "Dữ liệu đã thay đổi. Hãy tải lại trước khi lưu."
            );
        }
    }

    private ClassroomException duplicateStudentCode() {
        return new ClassroomException(
                "STUDENT_CODE_DUPLICATE",
                ClassroomErrorType.CONFLICT,
                "Mã học sinh đã tồn tại trên hệ thống."
        );
    }

    private ClassroomException duplicateEnrollment() {
        return new ClassroomException(
                "ENROLLMENT_DUPLICATE",
                ClassroomErrorType.CONFLICT,
                "Học sinh đã có ghi danh trong lớp này."
        );
    }

    private ClassroomException notFound(String code, String message) {
        return new ClassroomException(code, ClassroomErrorType.NOT_FOUND, message);
    }

    private ClassroomException invalidState(String code, String message) {
        return new ClassroomException(code, ClassroomErrorType.INVALID_STATE, message);
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
