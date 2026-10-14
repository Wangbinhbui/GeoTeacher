package com.geoteacher.ai.api;

import com.geoteacher.ai.domain.AiExamType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record GenerateAiExamRequest(
        @NotNull AiExamType examType,
        @NotNull Integer durationMinutes,
        @NotNull @Min(1) @Max(12) Integer gradeLevel,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String teacherInstructions,
        @NotEmpty @Size(max = 8) List<UUID> knowledgeDocumentIds,
        UUID sampleDocumentId,
        @AssertTrue(message = "Phải xác nhận gửi các đoạn đã chọn tới dịch vụ AI.") boolean externalProcessingConsent
) {
}
