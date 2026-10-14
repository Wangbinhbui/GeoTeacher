package com.geoteacher.file.infrastructure;

import com.geoteacher.file.domain.FileAsset;
import com.geoteacher.file.domain.FileAssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileAssetRepository extends JpaRepository<FileAsset, UUID> {
    boolean existsBySha256AndStatus(String sha256, FileAssetStatus status);
}
