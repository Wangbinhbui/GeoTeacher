package com.geoteacher.document.domain;

import com.geoteacher.file.domain.FileAsset;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tài liệu nguồn sở hữu các chunk trích xuất; file binary nằm sau FileStorage và chỉ được tham chiếu bằng metadata. */
@Entity
@Table(name = "source_documents")
public class SourceDocument {

    @Id private UUID id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_asset_id", nullable = false, unique = true)
    private FileAsset fileAsset;
    @Column(nullable = false, length = 255) private String title;
    @Column(name = "grade_level") private Integer gradeLevel;
    @Enumerated(EnumType.STRING) @Column(name = "document_kind", nullable = false, length = 30)
    private DocumentKind documentKind;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DocumentStatus status;
    @Column(name = "extracted_char_count", nullable = false) private int extractedCharCount;
    @Column(name = "chunk_count", nullable = false) private int chunkCount;
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    @OrderBy("chunkIndex asc")
    private List<DocumentChunk> chunks = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;

    protected SourceDocument() {
    }

    public static SourceDocument create(
            FileAsset fileAsset, String title, Integer gradeLevel, DocumentKind documentKind,
            List<ChunkInput> inputs, Instant now
    ) {
        if (gradeLevel != null && (gradeLevel < 1 || gradeLevel > 12)) {
            throw new IllegalArgumentException("Khối lớp phải từ 1 đến 12.");
        }
        if (inputs.isEmpty()) throw new IllegalArgumentException("Tài liệu không có nội dung có thể lập chỉ mục.");
        var document = new SourceDocument();
        document.id = UUID.randomUUID();
        document.fileAsset = fileAsset;
        document.title = title.trim();
        document.gradeLevel = gradeLevel;
        document.documentKind = documentKind;
        document.status = DocumentStatus.READY;
        document.createdAt = now;
        document.updatedAt = now;
        var index = 0;
        for (var input : inputs) {
            document.chunks.add(new DocumentChunk(document, index++, input.pageNumber(), input.sourceLabel(), input.content()));
        }
        document.chunkCount = document.chunks.size();
        document.extractedCharCount = document.chunks.stream().mapToInt(DocumentChunk::getCharCount).sum();
        return document;
    }

    public void archive(Instant now) {
        status = DocumentStatus.ARCHIVED;
        fileAsset.archive(now);
        updatedAt = now;
    }

    public UUID getId() { return id; }
    public FileAsset getFileAsset() { return fileAsset; }
    public String getTitle() { return title; }
    public Integer getGradeLevel() { return gradeLevel; }
    public DocumentKind getDocumentKind() { return documentKind; }
    public DocumentStatus getStatus() { return status; }
    public int getExtractedCharCount() { return extractedCharCount; }
    public int getChunkCount() { return chunkCount; }
    public List<DocumentChunk> getChunks() { return List.copyOf(chunks); }
    public long getVersion() { return version; }

    public record ChunkInput(Integer pageNumber, String sourceLabel, String content) {
    }
}
