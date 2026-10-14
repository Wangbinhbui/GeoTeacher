package com.geoteacher.document.api;

import com.geoteacher.document.domain.DocumentStatus;
import com.geoteacher.document.domain.SourceDocument;

import java.util.UUID;

public record DocumentSummaryResponse(
        UUID id,
        String title,
        Integer gradeLevel,
        DocumentStatus status,
        String originalName,
        String mediaType,
        long sizeBytes,
        String sha256,
        int extractedCharCount,
        int chunkCount,
        long version
) {
    public static DocumentSummaryResponse from(SourceDocument document) {
        var file = document.getFileAsset();
        return new DocumentSummaryResponse(
                document.getId(), document.getTitle(), document.getGradeLevel(), document.getStatus(),
                file.getOriginalName(), file.getMediaType(), file.getSizeBytes(), file.getSha256(),
                document.getExtractedCharCount(), document.getChunkCount(), document.getVersion()
        );
    }
}
