package com.geoteacher.auth.application;

public class AccountException extends RuntimeException {

    private final String code;

    public AccountException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
