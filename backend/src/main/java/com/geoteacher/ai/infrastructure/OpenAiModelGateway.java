package com.geoteacher.ai.infrastructure;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.geoteacher.ai.application.AiErrorType;
import com.geoteacher.ai.application.AiException;
import com.geoteacher.ai.application.AiGeneratedExam;
import com.geoteacher.ai.application.AiModelGateway;
import com.geoteacher.ai.application.AiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Adapter OpenAI Responses API; store=false và không upload file, chỉ gửi các đoạn được giáo viên chọn. */
@Component
public class OpenAiModelGateway implements AiModelGateway {

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

    public OpenAiModelGateway(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + nullToEmpty(properties.apiKey()))
                .build();
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
                "Chưa cấu hình OPENAI_API_KEY cho backend."
        );
        var body = Map.of(
                "model", properties.model(),
                "store", false,
                "instructions", "Bạn là giáo viên Địa lý Việt Nam. Chỉ tạo nội dung dựa trên NGUỒN KIẾN THỨC được cung cấp. Trả đúng JSON Schema.",
                "input", prompt,
                "text", Map.of("format", Map.of(
                        "type", "json_schema", "name", "geoteacher_exam_draft", "strict", true, "schema", schema
                ))
        );
        try {
            var response = client.post()
                    .uri("/v1/responses")
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
                    "Dịch vụ AI không tạo được đề. Hãy kiểm tra API key, model hoặc thử lại."
            );
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) throw providerFailed();
        for (var output : response.path("output")) {
            for (var content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText()) && !content.path("text").asText().isBlank()) {
                    return content.path("text").asText();
                }
                if ("refusal".equals(content.path("type").asText())) {
                    throw new AiException("AI_REFUSED", AiErrorType.PROVIDER_UNAVAILABLE,
                            "Model từ chối tạo đề từ yêu cầu này.");
                }
            }
        }
        throw providerFailed();
    }

    private AiException providerFailed() {
        return new AiException("AI_EMPTY_RESPONSE", AiErrorType.PROVIDER_UNAVAILABLE,
                "Dịch vụ AI không trả về nội dung đề.");
    }

    @Override public boolean configured() {
        return properties.enabled() && properties.apiKey() != null && !properties.apiKey().isBlank();
    }
    @Override public String provider() { return "OPENAI"; }
    @Override public String model() { return properties.model(); }
    private static String nullToEmpty(String value) { return value == null ? "" : value; }
}
