package com.geoteacher.classroom.application;

public class ClassroomException extends RuntimeException {

    private final String code;
    private final ClassroomErrorType type;

    public ClassroomException(String code, ClassroomErrorType type, String message) {
        super(message);
        this.code = code;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public ClassroomErrorType getType() {
        return type;
    }
}
