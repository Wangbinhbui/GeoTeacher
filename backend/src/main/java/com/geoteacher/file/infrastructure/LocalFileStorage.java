package com.geoteacher.file.infrastructure;

import com.geoteacher.file.application.FileStorage;
import com.geoteacher.file.application.FileStorageProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/** Local adapter chỉ nhận bytes và tự sinh storage key; tên/path người dùng không bao giờ đi vào filesystem. */
@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(FileStorageProperties properties) {
        this.root = properties.root().toAbsolutePath().normalize();
    }

    @Override
    public String store(byte[] content) {
        var id = UUID.randomUUID().toString().replace("-", "");
        var storageKey = id.substring(0, 2) + "/" + id + ".bin";
        var target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return storageKey;
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể lưu tệp vào storage cục bộ.", exception);
        }
    }

    @Override
    public void deleteIfExists(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException ignored) {
            // Cleanup bù trừ chỉ là best effort; orphan sẽ được job vận hành xử lý sau.
        }
    }

    private Path resolve(String storageKey) {
        var target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Storage key không hợp lệ.");
        return target;
    }
}
