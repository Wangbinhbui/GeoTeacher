package com.geoteacher.document.api;

import com.geoteacher.document.domain.DocumentChunk;

import java.util.UUID;

public record DocumentChunkResponse(
        UUID id,
        int chunkIndex,
        Integer pageNumber,
        String sourceLabel,
        String content,
        int charCount
) {
    static DocumentChunkResponse from(DocumentChunk chunk) {
        return new DocumentChunkResponse(
                chunk.getId(), chunk.getChunkIndex(), chunk.getPageNumber(), chunk.getSourceLabel(),
                chunk.getContent(), chunk.getCharCount()
        );
    }
}
