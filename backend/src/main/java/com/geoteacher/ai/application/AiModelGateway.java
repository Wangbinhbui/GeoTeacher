package com.geoteacher.ai.application;

public interface AiModelGateway {
    AiGeneratedExam generate(String prompt);
    boolean configured();
    String provider();
    String model();
}
