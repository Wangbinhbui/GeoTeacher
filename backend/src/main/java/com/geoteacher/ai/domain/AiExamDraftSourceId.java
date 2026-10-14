package com.geoteacher.ai.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AiExamDraftSourceId implements Serializable {
    private UUID draftId;
    private UUID documentId;

    public AiExamDraftSourceId() {
    }

    public AiExamDraftSourceId(UUID draftId, UUID documentId) {
        this.draftId = draftId;
        this.documentId = documentId;
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AiExamDraftSourceId that)) return false;
        return Objects.equals(draftId, that.draftId) && Objects.equals(documentId, that.documentId);
    }

    @Override public int hashCode() { return Objects.hash(draftId, documentId); }
}
