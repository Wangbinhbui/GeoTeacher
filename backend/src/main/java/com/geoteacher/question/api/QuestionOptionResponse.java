package com.geoteacher.question.api;

import com.geoteacher.question.domain.QuestionOption;

import java.util.UUID;

public record QuestionOptionResponse(
        UUID id,
        String key,
        String content,
        boolean correct,
        int position
) {
    static QuestionOptionResponse from(QuestionOption option) {
        return new QuestionOptionResponse(
                option.getId(), option.getOptionKey(), option.getContent(), option.isCorrect(), option.getPosition()
        );
    }
}
