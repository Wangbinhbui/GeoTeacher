package com.geoteacher.file.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_assets")
public class FileAsset {

    @Id private UUID id;
    @Column(name = "storage_provider", nullable = false, length = 30) private String storageProvider;
    @Column(name = "storage_key", nullable = false, length = 255) private String storageKey;
    @Column(name = "original_name", nullable = false, length = 255) private String originalName;
    @Column(name = "media_type", nullable = false, length = 120) private String mediaType;
    @Column(name = "size_bytes", nullable = false) private long sizeBytes;
    @Column(nullable = false, length = 64) private String sha256;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private FileAssetStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;

    protected FileAsset() {
    }

    public static FileAsset create(
            String storageKey, String originalName, String mediaType, long sizeBytes, String sha256, Instant now
    ) {
        var asset = new FileAsset();
        asset.id = UUID.randomUUID();
        asset.storageProvider = "LOCAL";
        asset.storageKey = storageKey;
        asset.originalName = originalName;
        asset.mediaType = mediaType;
        asset.sizeBytes = sizeBytes;
        asset.sha256 = sha256;
        asset.status = FileAssetStatus.STORED;
        asset.createdAt = now;
        asset.updatedAt = now;
        return asset;
    }

    public void archive(Instant now) {
        status = FileAssetStatus.ARCHIVED;
        updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getStorageKey() { return storageKey; }
    public String getOriginalName() { return originalName; }
    public String getMediaType() { return mediaType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
    public FileAssetStatus getStatus() { return status; }
}
