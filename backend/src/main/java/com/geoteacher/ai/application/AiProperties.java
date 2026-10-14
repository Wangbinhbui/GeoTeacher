package com.geoteacher.ai.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geoteacher.ai")
public record AiProperties(boolean enabled, String apiKey, String model, String baseUrl) {
}
