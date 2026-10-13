package com.geoteacher.question.api;

import com.geoteacher.question.domain.CognitiveLevel;
import com.geoteacher.question.domain.Question;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record QuestionDetailResponse(
        UUID id,
        QuestionType type,
        int gradeLevel,
        String topic,
        CognitiveLevel cognitiveLevel,
        String content,
        String explanation,
        String suggestedScore,
        QuestionStatus status,
        List<QuestionOptionResponse> options,
        long version
) {
    public static QuestionDetailResponse from(Question question) {
        var options = question.getOptions().stream()
                .sorted(Comparator.comparingInt(option -> option.getPosition()))
                .map(QuestionOptionResponse::from)
                .toList();
        return new QuestionDetailResponse(
                question.getId(), question.getType(), question.getGradeLevel(), question.getTopic(),
                question.getCognitiveLevel(), question.getContent(), question.getExplanation(),
                question.getSuggestedScore() == null ? null : question.getSuggestedScore().toPlainString(),
                question.getStatus(), options, question.getVersion()
        );
    }
}
