package com.geoteacher.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@IdClass(AiExamDraftSourceId.class)
@Table(name = "ai_exam_draft_sources")
public class AiExamDraftSource {
    @Id @Column(name = "draft_id") private UUID draftId;
    @Id @Column(name = "document_id") private UUID documentId;
    @Column(name = "source_role", nullable = false, length = 20) private String sourceRole;

    protected AiExamDraftSource() {
    }

    public AiExamDraftSource(UUID draftId, UUID documentId, String sourceRole) {
        this.draftId = draftId;
        this.documentId = documentId;
        this.sourceRole = sourceRole;
    }

    public UUID getDraftId() { return draftId; }
    public UUID getDocumentId() { return documentId; }
    public String getSourceRole() { return sourceRole; }
}
