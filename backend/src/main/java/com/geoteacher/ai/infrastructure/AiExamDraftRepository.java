package com.geoteacher.ai.infrastructure;

import com.geoteacher.ai.domain.AiExamDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiExamDraftRepository extends JpaRepository<AiExamDraft, UUID> {
    List<AiExamDraft> findAllByOrderByCreatedAtDesc();
}
