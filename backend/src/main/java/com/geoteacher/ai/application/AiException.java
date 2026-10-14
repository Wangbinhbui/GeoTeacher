package com.geoteacher.ai.application;

public class AiException extends RuntimeException {
    private final String code;
    private final AiErrorType type;

    public AiException(String code, AiErrorType type, String message) {
        super(message);
        this.code = code;
        this.type = type;
    }

    public String getCode() { return code; }
    public AiErrorType getType() { return type; }
}
