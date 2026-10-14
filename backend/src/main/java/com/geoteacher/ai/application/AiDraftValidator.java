package com.geoteacher.ai.application;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/** Chặn output model sai schema nghiệp vụ; model không được quyền bỏ qua validator backend. */
@Component
public class AiDraftValidator {

    public void validate(AiGeneratedExam exam, Set<UUID> allowedKnowledgeChunks) {
        if (exam == null || blank(exam.title()) || exam.sections() == null || exam.sections().isEmpty()) {
            throw invalid("AI_DRAFT_INVALID", "AI trả về đề không có tiêu đề hoặc phần thi.");
        }
        var total = BigDecimal.ZERO;
        var questionCount = 0;
        for (var section : exam.sections()) {
            if (section == null || blank(section.title()) || section.questions() == null || section.questions().isEmpty()) {
                throw invalid("AI_DRAFT_INVALID_SECTION", "AI trả về phần thi không hợp lệ.");
            }
            for (var question : section.questions()) {
                questionCount++;
                if (questionCount > 100 || question == null || blank(question.content()) || question.score() == null
                        || question.score().compareTo(BigDecimal.ZERO) <= 0) {
                    throw invalid("AI_DRAFT_INVALID_QUESTION", "AI trả về câu hỏi hoặc điểm không hợp lệ.");
                }
                if (!Set.of("SINGLE_CHOICE", "TRUE_FALSE", "SHORT_ANSWER", "ESSAY").contains(question.type())) {
                    throw invalid("AI_DRAFT_INVALID_TYPE", "AI trả về loại câu hỏi không được hỗ trợ.");
                }
                if ("SINGLE_CHOICE".equals(question.type())
                        && (question.options() == null || question.options().size() != 4
                        || question.answer() == null || !question.answer().matches("[ABCD]"))) {
                    throw invalid("AI_DRAFT_INVALID_ANSWER", "Câu trắc nghiệm AI phải có 4 lựa chọn và đáp án A–D.");
                }
                if (question.sourceChunkIds() == null || question.sourceChunkIds().isEmpty()) {
                    throw invalid("AI_DRAFT_MISSING_CITATION", "Mỗi câu AI phải có ít nhất một nguồn kiến thức.");
                }
                for (var sourceId : question.sourceChunkIds()) {
                    try {
                        if (!allowedKnowledgeChunks.contains(UUID.fromString(sourceId))) throw invalidCitation();
                    } catch (IllegalArgumentException exception) {
                        throw invalidCitation();
                    }
                }
                total = total.add(question.score());
            }
        }
        if (total.compareTo(new BigDecimal("10")) != 0) {
            throw invalid("AI_DRAFT_SCORE_TOTAL", "Tổng điểm AI tạo phải bằng 10, hiện là " + total.toPlainString() + ".");
        }
    }

    private AiException invalidCitation() {
        return invalid("AI_DRAFT_INVALID_CITATION", "AI trích dẫn đoạn không thuộc giáo trình đã chọn.");
    }
    private AiException invalid(String code, String message) {
        return new AiException(code, AiErrorType.INVALID_STATE, message);
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
