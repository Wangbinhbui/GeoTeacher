package com.geoteacher.file.application;

public interface FileStorage {

    String store(byte[] content);

    void deleteIfExists(String storageKey);
}
