package com.geoteacher.ai.infrastructure;

import com.geoteacher.ai.application.AiProperties;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiModelGatewayTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void sendsGeminiKeyWithoutPersistingInteractionAndParsesStructuredOutput() throws Exception {
        var requestBody = new AtomicReference<String>();
        var apiKey = new AtomicReference<String>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/interactions", exchange -> {
            apiKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            var generatedExam = """
                    {"title":"Đề thử","instructions":"Làm bài","sections":[{"title":"Phần I","description":"Trắc nghiệm","questions":[{"type":"SINGLE_CHOICE","content":"Câu hỏi","options":["A","B","C","D"],"answer":"A","explanation":"Giải thích","score":10,"sourceChunkIds":["00000000-0000-0000-0000-000000000001"]}]}]}
                    """.strip();
            var response = new ObjectMapper().createObjectNode();
            var step = response.putArray("steps").addObject();
            step.put("type", "model_output");
            step.putArray("content").addObject().put("type", "text").put("text", generatedExam);
            var bytes = response.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        var mapper = new ObjectMapper();
        var properties = new AiProperties(true, "gemini-test-key", "gemini-3.8-flash",
                "http://localhost:" + server.getAddress().getPort());
        var gateway = new GeminiModelGateway(properties, mapper);

        var result = gateway.generate("Tạo đề");

        assertThat(result.title()).isEqualTo("Đề thử");
        assertThat(apiKey.get()).isEqualTo("gemini-test-key");
        var sent = mapper.readTree(requestBody.get());
        assertThat(sent.path("model").asText()).isEqualTo("gemini-3.8-flash");
        assertThat(sent.path("store").asBoolean()).isFalse();
        assertThat(sent.path("response_format").path("mime_type").asText()).isEqualTo("application/json");
        assertThat(sent.path("response_format").path("schema").path("type").asText()).isEqualTo("object");
    }
}
