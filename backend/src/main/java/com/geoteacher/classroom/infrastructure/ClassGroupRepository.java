package com.geoteacher.classroom.infrastructure;

import com.geoteacher.classroom.domain.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID> {
    List<ClassGroup> findAllByOrderByClassCodeAsc();

    List<ClassGroup> findAllBySchoolYearIdOrderByClassCodeAsc(UUID schoolYearId);

    boolean existsBySchoolYearIdAndClassCodeNormalized(UUID schoolYearId, String classCodeNormalized);

    boolean existsBySchoolYearIdAndClassCodeNormalizedAndIdNot(
            UUID schoolYearId,
            String classCodeNormalized,
            UUID id
    );
}
