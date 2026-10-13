package com.geoteacher.question.api;

import com.geoteacher.question.domain.CognitiveLevel;
import com.geoteacher.question.domain.Question;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;

import java.util.UUID;

/** DTO danh sách cố ý không chứa options, đáp án đúng hay giải thích bí mật. */
public record QuestionSummaryResponse(
        UUID id,
        QuestionType type,
        int gradeLevel,
        String topic,
        CognitiveLevel cognitiveLevel,
        String content,
        String suggestedScore,
        QuestionStatus status,
        long version
) {
    public static QuestionSummaryResponse from(Question question) {
        return new QuestionSummaryResponse(
                question.getId(), question.getType(), question.getGradeLevel(), question.getTopic(),
                question.getCognitiveLevel(), question.getContent(),
                question.getSuggestedScore() == null ? null : question.getSuggestedScore().toPlainString(),
                question.getStatus(), question.getVersion()
        );
    }
}
