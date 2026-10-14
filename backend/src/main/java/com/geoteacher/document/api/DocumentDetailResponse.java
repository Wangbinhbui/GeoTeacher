package com.geoteacher.document.api;

import com.geoteacher.document.domain.SourceDocument;
import com.geoteacher.document.domain.DocumentChunk;

import java.util.Comparator;
import java.util.List;

public record DocumentDetailResponse(
        DocumentSummaryResponse document,
        List<DocumentChunkResponse> chunks
) {
    public static DocumentDetailResponse from(SourceDocument document) {
        return new DocumentDetailResponse(
                DocumentSummaryResponse.from(document),
                document.getChunks().stream()
                        .sorted(Comparator.comparingInt(DocumentChunk::getChunkIndex))
                        .map(DocumentChunkResponse::from)
                        .toList()
        );
    }
}
