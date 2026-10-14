package com.geoteacher.file.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.geoteacher.file.application.FileStorageProperties;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageTest {

    @TempDir
    java.nio.file.Path temporaryDirectory;

    @Test
    void storesWithServerGeneratedKeyAndCanCleanUp() throws Exception {
        var storage = new LocalFileStorage(new FileStorageProperties(temporaryDirectory));
        var content = "noi dung giao trinh".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        var storageKey = storage.store(content);
        var storedFile = temporaryDirectory.resolve(storageKey);

        assertThat(storageKey).matches("[0-9a-f]{2}/[0-9a-f]{32}\\.bin");
        assertThat(storedFile).hasBinaryContent(content);

        storage.deleteIfExists(storageKey);
        assertThat(Files.exists(storedFile)).isFalse();
    }
}
