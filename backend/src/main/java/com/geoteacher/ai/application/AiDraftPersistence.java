package com.geoteacher.ai.application;

import tools.jackson.databind.ObjectMapper;
import com.geoteacher.ai.api.AiExamDraftResponse;
import com.geoteacher.ai.api.GenerateAiExamRequest;
import com.geoteacher.ai.domain.AiExamDraft;
import com.geoteacher.ai.domain.AiExamDraftSource;
import com.geoteacher.ai.infrastructure.AiExamDraftRepository;
import com.geoteacher.ai.infrastructure.AiExamDraftSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;

@Service
public class AiDraftPersistence {
    private final AiExamDraftRepository drafts;
    private final AiExamDraftSourceRepository sources;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AiDraftPersistence(AiExamDraftRepository drafts, AiExamDraftSourceRepository sources,
                              ObjectMapper objectMapper, Clock clock) {
        this.drafts = drafts;
        this.sources = sources;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public AiExamDraftResponse save(GenerateAiExamRequest request, AiGeneratedExam content,
                                    String provider, String model) {
        var draft = drafts.saveAndFlush(AiExamDraft.create(
                    request.examType(), request.durationMinutes(), request.gradeLevel(), request.title().trim(),
                    trimToNull(request.teacherInstructions()), provider, model,
                    objectMapper.valueToTree(content), Instant.now(clock)
        ));
        var sourceRows = new ArrayList<AiExamDraftSource>();
        request.knowledgeDocumentIds().stream().distinct()
                .forEach(id -> sourceRows.add(new AiExamDraftSource(draft.getId(), id, "KNOWLEDGE")));
        if (request.sampleDocumentId() != null) {
            sourceRows.add(new AiExamDraftSource(draft.getId(), request.sampleDocumentId(), "SAMPLE"));
        }
        sources.saveAll(sourceRows);
        return AiExamDraftResponse.from(draft, request.knowledgeDocumentIds().stream().distinct().toList(),
                request.sampleDocumentId(), content);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
