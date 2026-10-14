package com.geoteacher.document.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private SourceDocument document;
    @Column(name = "chunk_index", nullable = false) private int chunkIndex;
    @Column(name = "page_number") private Integer pageNumber;
    @Column(name = "source_label", nullable = false, length = 120) private String sourceLabel;
    @Column(nullable = false, columnDefinition = "text") private String content;
    @Column(name = "char_count", nullable = false) private int charCount;

    protected DocumentChunk() {
    }

    DocumentChunk(SourceDocument document, int chunkIndex, Integer pageNumber, String sourceLabel, String content) {
        this.id = UUID.randomUUID();
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.pageNumber = pageNumber;
        this.sourceLabel = sourceLabel;
        this.content = content;
        this.charCount = content.length();
    }

    public UUID getId() { return id; }
    public int getChunkIndex() { return chunkIndex; }
    public Integer getPageNumber() { return pageNumber; }
    public String getSourceLabel() { return sourceLabel; }
    public String getContent() { return content; }
    public int getCharCount() { return charCount; }
}
