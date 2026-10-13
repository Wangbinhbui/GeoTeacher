package com.geoteacher.classroom.infrastructure;

import com.geoteacher.classroom.domain.ClassEnrollment;
import com.geoteacher.classroom.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {

    @EntityGraph(attributePaths = {"student", "classGroup", "classGroup.schoolYear"})
    List<ClassEnrollment> findAllByClassGroupIdOrderByStudentFullNameAsc(UUID classGroupId);

    @EntityGraph(attributePaths = {"student", "classGroup", "classGroup.schoolYear"})
    List<ClassEnrollment> findAllByStudentIdOrderByEnrolledAtDesc(UUID studentId);

    boolean existsByClassGroupIdAndStudentId(UUID classGroupId, UUID studentId);

    boolean existsByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);
}
