package com.geoteacher.ai.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AiDraftValidatorTest {
    private final AiDraftValidator validator = new AiDraftValidator();
    private final UUID sourceId = UUID.randomUUID();

    @Test
    void acceptsGroundedDraftWithExactTenPoints() {
        var exam = exam(new BigDecimal("10"), sourceId.toString(), List.of("A", "B", "C", "D"), "A");
        assertThatCode(() -> validator.validate(exam, Set.of(sourceId))).doesNotThrowAnyException();
    }

    @Test
    void rejectsCitationOutsideSelectedKnowledge() {
        var exam = exam(new BigDecimal("10"), UUID.randomUUID().toString(), List.of("A", "B", "C", "D"), "A");
        assertThatThrownBy(() -> validator.validate(exam, Set.of(sourceId)))
                .isInstanceOf(AiException.class)
                .extracting(error -> ((AiException) error).getCode())
                .isEqualTo("AI_DRAFT_INVALID_CITATION");
    }

    @Test
    void rejectsWrongTotalAndMalformedSingleChoice() {
        var wrongTotal = exam(new BigDecimal("9.5"), sourceId.toString(), List.of("A", "B", "C", "D"), "A");
        assertThatThrownBy(() -> validator.validate(wrongTotal, Set.of(sourceId)))
                .isInstanceOf(AiException.class)
                .extracting(error -> ((AiException) error).getCode())
                .isEqualTo("AI_DRAFT_SCORE_TOTAL");

        var malformed = exam(new BigDecimal("10"), sourceId.toString(), List.of("A", "B"), "X");
        assertThatThrownBy(() -> validator.validate(malformed, Set.of(sourceId)))
                .isInstanceOf(AiException.class)
                .extracting(error -> ((AiException) error).getCode())
                .isEqualTo("AI_DRAFT_INVALID_ANSWER");
    }

    private AiGeneratedExam exam(BigDecimal score, String citation, List<String> options, String answer) {
        var question = new AiGeneratedExam.Question(
                "SINGLE_CHOICE", "Câu hỏi", options, answer, "Giải thích", score, List.of(citation));
        return new AiGeneratedExam("Đề", "Hướng dẫn", List.of(
                new AiGeneratedExam.Section("Phần I", "Mô tả", List.of(question))));
    }
}
