package com.geoteacher.ai.api;

import com.geoteacher.ai.application.AiGeneratedExam;
import com.geoteacher.ai.domain.AiExamDraft;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiExamDraftResponse(
        UUID id,
        String examType,
        int durationMinutes,
        int gradeLevel,
        String title,
        String teacherInstructions,
        String status,
        String provider,
        String model,
        Instant createdAt,
        List<UUID> knowledgeDocumentIds,
        UUID sampleDocumentId,
        AiGeneratedExam content
) {
    public static AiExamDraftResponse from(AiExamDraft draft, List<UUID> knowledgeIds,
                                           UUID sampleId, AiGeneratedExam content) {
        return new AiExamDraftResponse(
                draft.getId(), draft.getExamType().name(), draft.getDurationMinutes(), draft.getGradeLevel(),
                draft.getTitle(), draft.getTeacherInstructions(), draft.getStatus(), draft.getProvider(),
                draft.getModelName(), draft.getCreatedAt(), knowledgeIds, sampleId, content
        );
    }
}
