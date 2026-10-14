package com.geoteacher.ai.infrastructure;

import com.geoteacher.ai.domain.AiExamDraftSource;
import com.geoteacher.ai.domain.AiExamDraftSourceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiExamDraftSourceRepository extends JpaRepository<AiExamDraftSource, AiExamDraftSourceId> {
    List<AiExamDraftSource> findAllByDraftId(UUID draftId);
}
