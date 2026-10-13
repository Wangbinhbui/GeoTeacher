package com.geoteacher.question.application;

public class QuestionException extends RuntimeException {

    private final String code;
    private final QuestionErrorType type;

    public QuestionException(String code, QuestionErrorType type, String message) {
        super(message);
        this.code = code;
        this.type = type;
    }

    public String getCode() { return code; }
    public QuestionErrorType getType() { return type; }
}
