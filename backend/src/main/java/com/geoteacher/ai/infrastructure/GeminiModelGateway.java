package com.geoteacher.ai.infrastructure;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.geoteacher.ai.application.AiErrorType;
import com.geoteacher.ai.application.AiException;
import com.geoteacher.ai.application.AiGeneratedExam;
import com.geoteacher.ai.application.AiModelGateway;
import com.geoteacher.ai.application.AiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** Adapter Gemini Interactions API; chỉ gửi prompt đã được giáo viên xác nhận và không lưu interaction. */
@Component
public class GeminiModelGateway implements AiModelGateway {

    private static final String SCHEMA = """
            {"type":"object","additionalProperties":false,"required":["title","instructions","sections"],"properties":{
              "title":{"type":"string"},"instructions":{"type":"string"},
              "sections":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["title","description","questions"],"properties":{
                "title":{"type":"string"},"description":{"type":"string"},
                "questions":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["type","content","options","answer","explanation","score","sourceChunkIds"],"properties":{
                  "type":{"type":"string","enum":["SINGLE_CHOICE","TRUE_FALSE","SHORT_ANSWER","ESSAY"]},
                  "content":{"type":"string"},"options":{"type":"array","items":{"type":"string"}},
                  "answer":{"type":"string"},"explanation":{"type":"string"},"score":{"type":"number"},
                  "sourceChunkIds":{"type":"array","items":{"type":"string"}}
                }}}
              }}}
            }}
            """;

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient client;
    private final JsonNode schema;

    public GeminiModelGateway(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.client = RestClient.builder().baseUrl(properties.baseUrl()).build();
        try {
            this.schema = objectMapper.readTree(SCHEMA);
        } catch (JacksonException exception) {
            throw new IllegalStateException("JSON Schema AI nội bộ không hợp lệ.", exception);
        }
    }

    @Override
    public AiGeneratedExam generate(String prompt) {
        if (!configured()) throw new AiException(
                "AI_NOT_CONFIGURED", AiErrorType.INVALID_STATE,
                "Chưa cấu hình GEMINI_API_KEY cho backend."
        );
        var body = Map.of(
                "model", properties.model(),
                "store", false,
                "system_instruction", "Bạn là giáo viên Địa lý Việt Nam. Chỉ tạo nội dung dựa trên NGUỒN KIẾN THỨC được cung cấp. Trả đúng JSON Schema.",
                "input", prompt,
                "response_format", Map.of(
                        "type", "text", "mime_type", "application/json", "schema", schema
                )
        );
        try {
            var response = client.post()
                    .uri("/v1/interactions")
                    .header("x-goog-api-key", properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            var outputText = extractOutputText(response);
            return objectMapper.readValue(outputText, AiGeneratedExam.class);
        } catch (AiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AiException(
                    "AI_PROVIDER_FAILED", AiErrorType.PROVIDER_UNAVAILABLE,
                    "Gemini không tạo được đề. Hãy kiểm tra API key, model hoặc thử lại."
            );
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) throw providerFailed();
        for (var step : response.path("steps")) {
            if (!"model_output".equals(step.path("type").asText())) continue;
            for (var content : step.path("content")) {
                if ("text".equals(content.path("type").asText()) && !content.path("text").asText().isBlank()) {
                    return content.path("text").asText();
                }
                if ("refusal".equals(content.path("type").asText())) {
                    throw new AiException("AI_REFUSED", AiErrorType.PROVIDER_UNAVAILABLE,
                            "Gemini từ chối tạo đề từ yêu cầu này.");
                }
            }
        }
        throw providerFailed();
    }

    private AiException providerFailed() {
        return new AiException("AI_EMPTY_RESPONSE", AiErrorType.PROVIDER_UNAVAILABLE,
                "Gemini không trả về nội dung đề.");
    }

    @Override public boolean configured() {
        return properties.enabled() && properties.apiKey() != null && !properties.apiKey().isBlank();
    }
    @Override public String provider() { return "GEMINI"; }
    @Override public String model() { return properties.model(); }
}
