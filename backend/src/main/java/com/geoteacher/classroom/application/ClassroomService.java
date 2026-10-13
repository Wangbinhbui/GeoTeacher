package com.geoteacher.classroom.application;

import com.geoteacher.classroom.api.ClassGroupRequest;
import com.geoteacher.classroom.api.ClassGroupResponse;
import com.geoteacher.classroom.api.SchoolYearRequest;
import com.geoteacher.classroom.api.SchoolYearResponse;
import com.geoteacher.classroom.domain.ClassGroup;
import com.geoteacher.classroom.domain.SchoolYear;
import com.geoteacher.classroom.domain.SchoolYearStatus;
import com.geoteacher.classroom.infrastructure.ClassGroupRepository;
import com.geoteacher.classroom.infrastructure.SchoolYearRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Điều phối use case năm học/lớp và giữ transaction ở application layer.
 * Database vẫn là hàng rào cuối cho mã trùng; kiểm tra sớm giúp trả lỗi thân thiện.
 */
@Service
public class ClassroomService {

    private final SchoolYearRepository schoolYearRepository;
    private final ClassGroupRepository classGroupRepository;
    private final Clock clock;

    public ClassroomService(
            SchoolYearRepository schoolYearRepository,
            ClassGroupRepository classGroupRepository,
            Clock clock
    ) {
        this.schoolYearRepository = schoolYearRepository;
        this.classGroupRepository = classGroupRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<SchoolYearResponse> listSchoolYears() {
        return schoolYearRepository.findAllByOrderByStartDateDesc().stream()
                .map(SchoolYearResponse::from)
                .toList();
    }

    @Transactional
    public SchoolYearResponse createSchoolYear(SchoolYearRequest request) {
        ensureMutableSchoolYearStatus(request.status());
        var normalizedCode = SchoolYear.normalizeCode(request.code());
        if (schoolYearRepository.existsByCodeNormalized(normalizedCode)) {
            throw duplicateSchoolYear();
        }
        try {
            return SchoolYearResponse.from(schoolYearRepository.saveAndFlush(SchoolYear.create(
                    request.code(), request.startDate(), request.endDate(), request.status(), now()
            )));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateSchoolYear();
        } catch (IllegalArgumentException exception) {
            throw invalidState("INVALID_SCHOOL_YEAR", exception.getMessage());
        }
    }

    @Transactional
    public SchoolYearResponse updateSchoolYear(UUID id, SchoolYearRequest request) {
        ensureMutableSchoolYearStatus(request.status());
        var schoolYear = getSchoolYear(id);
        verifyVersion(schoolYear.getVersion(), request.version());
        if (schoolYearRepository.existsByCodeNormalizedAndIdNot(
                SchoolYear.normalizeCode(request.code()), id
        )) {
            throw duplicateSchoolYear();
        }
        try {
            schoolYear.update(request.code(), request.startDate(), request.endDate(), request.status(), now());
            return SchoolYearResponse.from(schoolYearRepository.saveAndFlush(schoolYear));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateSchoolYear();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw invalidState("INVALID_SCHOOL_YEAR", exception.getMessage());
        }
    }

    @Transactional
    public SchoolYearResponse archiveSchoolYear(UUID id, long version) {
        var schoolYear = getSchoolYear(id);
        verifyVersion(schoolYear.getVersion(), version);
        schoolYear.archive(now());
        return SchoolYearResponse.from(schoolYearRepository.saveAndFlush(schoolYear));
    }

    @Transactional(readOnly = true)
    public List<ClassGroupResponse> listClassGroups(UUID schoolYearId) {
        var classGroups = schoolYearId == null
                ? classGroupRepository.findAllByOrderByClassCodeAsc()
                : classGroupRepository.findAllBySchoolYearIdOrderByClassCodeAsc(schoolYearId);
        return classGroups.stream().map(ClassGroupResponse::from).toList();
    }

    @Transactional
    public ClassGroupResponse createClassGroup(ClassGroupRequest request) {
        var schoolYear = getSchoolYear(request.schoolYearId());
        ensureSchoolYearAllowsClasses(schoolYear);
        var normalizedCode = ClassGroup.normalizeCode(request.classCode());
        if (classGroupRepository.existsBySchoolYearIdAndClassCodeNormalized(
                request.schoolYearId(), normalizedCode
        )) {
            throw duplicateClassCode();
        }
        try {
            return ClassGroupResponse.from(classGroupRepository.saveAndFlush(ClassGroup.create(
                    schoolYear, request.classCode(), request.displayName(), request.gradeLevel(), now()
            )));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateClassCode();
        } catch (IllegalArgumentException exception) {
            throw invalidState("INVALID_CLASS_GROUP", exception.getMessage());
        }
    }

    @Transactional
    public ClassGroupResponse updateClassGroup(UUID id, ClassGroupRequest request) {
        var classGroup = getClassGroup(id);
        verifyVersion(classGroup.getVersion(), request.version());
        if (!classGroup.getSchoolYear().getId().equals(request.schoolYearId())) {
            throw invalidState("CLASS_YEAR_IMMUTABLE", "Không thể chuyển lớp sang năm học khác.");
        }
        ensureSchoolYearAllowsClasses(classGroup.getSchoolYear());
        if (classGroupRepository.existsBySchoolYearIdAndClassCodeNormalizedAndIdNot(
                request.schoolYearId(), ClassGroup.normalizeCode(request.classCode()), id
        )) {
            throw duplicateClassCode();
        }
        try {
            classGroup.update(request.classCode(), request.displayName(), request.gradeLevel(), now());
            return ClassGroupResponse.from(classGroupRepository.saveAndFlush(classGroup));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateClassCode();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw invalidState("INVALID_CLASS_GROUP", exception.getMessage());
        }
    }

    @Transactional
    public ClassGroupResponse archiveClassGroup(UUID id, long version) {
        var classGroup = getClassGroup(id);
        verifyVersion(classGroup.getVersion(), version);
        classGroup.archive(now());
        return ClassGroupResponse.from(classGroupRepository.saveAndFlush(classGroup));
    }

    private SchoolYear getSchoolYear(UUID id) {
        return schoolYearRepository.findById(id).orElseThrow(() -> new ClassroomException(
                "SCHOOL_YEAR_NOT_FOUND", ClassroomErrorType.NOT_FOUND, "Không tìm thấy năm học."
        ));
    }

    private ClassGroup getClassGroup(UUID id) {
        return classGroupRepository.findById(id).orElseThrow(() -> new ClassroomException(
                "CLASS_GROUP_NOT_FOUND", ClassroomErrorType.NOT_FOUND, "Không tìm thấy lớp học."
        ));
    }

    private void ensureMutableSchoolYearStatus(SchoolYearStatus status) {
        if (status == SchoolYearStatus.ARCHIVED) {
            throw invalidState("INVALID_SCHOOL_YEAR_STATUS", "Hãy dùng thao tác lưu trữ riêng.");
        }
    }

    private void ensureSchoolYearAllowsClasses(SchoolYear schoolYear) {
        if (schoolYear.getStatus() == SchoolYearStatus.ARCHIVED) {
            throw invalidState("SCHOOL_YEAR_ARCHIVED", "Không thể thêm hoặc sửa lớp trong năm học đã lưu trữ.");
        }
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

    private ClassroomException duplicateSchoolYear() {
        return new ClassroomException(
                "SCHOOL_YEAR_CODE_DUPLICATE",
                ClassroomErrorType.CONFLICT,
                "Mã năm học đã tồn tại."
        );
    }

    private ClassroomException duplicateClassCode() {
        return new ClassroomException(
                "CLASS_CODE_DUPLICATE_IN_YEAR",
                ClassroomErrorType.CONFLICT,
                "Mã lớp đã tồn tại trong năm học này."
        );
    }

    private ClassroomException invalidState(String code, String message) {
        return new ClassroomException(code, ClassroomErrorType.INVALID_STATE, message);
    }

    private Instant now() {
        return Instant.now(clock);
    }
}
