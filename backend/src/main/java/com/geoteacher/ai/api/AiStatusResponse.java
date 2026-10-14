package com.geoteacher.ai.api;

public record AiStatusResponse(boolean configured, String provider, String model, String privacyNotice) {
}
