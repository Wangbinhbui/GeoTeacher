package com.geoteacher.classroom.infrastructure;

import com.geoteacher.classroom.domain.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
    List<SchoolYear> findAllByOrderByStartDateDesc();

    boolean existsByCodeNormalized(String codeNormalized);

    boolean existsByCodeNormalizedAndIdNot(String codeNormalized, UUID id);
}
