package com.geoteacher.ai.application;

import java.math.BigDecimal;
import java.util.List;

/** Contract model phải tuân theo; service vẫn kiểm tra lại toàn bộ quy tắc trước khi lưu. */
public record AiGeneratedExam(String title, String instructions, List<Section> sections) {
    public record Section(String title, String description, List<Question> questions) {
    }

    public record Question(
            String type,
            String content,
            List<String> options,
            String answer,
            String explanation,
            BigDecimal score,
            List<String> sourceChunkIds
    ) {
    }
}
