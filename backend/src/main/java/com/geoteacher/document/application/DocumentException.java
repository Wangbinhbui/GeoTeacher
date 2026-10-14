package com.geoteacher.document.application;

public class DocumentException extends RuntimeException {

    private final String code;
    private final DocumentErrorType type;

    public DocumentException(String code, DocumentErrorType type, String message) {
        super(message);
        this.code = code;
        this.type = type;
    }

    public String getCode() { return code; }
    public DocumentErrorType getType() { return type; }
}
