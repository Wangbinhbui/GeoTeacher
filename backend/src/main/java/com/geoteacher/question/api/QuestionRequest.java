package com.geoteacher.question.api;

import com.geoteacher.question.domain.CognitiveLevel;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionRequest(
        @NotNull QuestionType type,
        @Min(1) @Max(12) int gradeLevel,
        @NotBlank @Size(max = 150) String topic,
        @NotNull CognitiveLevel cognitiveLevel,
        @NotBlank @Size(max = 10_000) String content,
        @Size(max = 10_000) String explanation,
        @Pattern(regexp = "^$|^[0-9]{1,4}(\\.[0-9]{1,3})?$", message = "Điểm gợi ý phải là số dương, tối đa 3 chữ số thập phân.")
        String suggestedScore,
        @NotNull QuestionStatus status,
        @NotEmpty List<@Valid QuestionOptionRequest> options,
        Long version
) {
}
