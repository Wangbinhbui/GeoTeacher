package com.geoteacher.document.application;

import com.geoteacher.document.api.DocumentDetailResponse;
import com.geoteacher.document.api.DocumentSummaryResponse;
import com.geoteacher.document.domain.SourceDocument;
import com.geoteacher.document.infrastructure.SourceDocumentRepository;
import com.geoteacher.file.application.FileStorage;
import com.geoteacher.file.domain.FileAsset;
import com.geoteacher.file.domain.FileAssetStatus;
import com.geoteacher.file.infrastructure.FileAssetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipInputStream;

/** Upload đồng bộ: kiểm tra → trích text → lưu binary → lưu metadata/chunk trong một transaction. */
@Service
public class DocumentService {

    private static final long MAX_FILE_BYTES = 20L * 1024L * 1024L;

    private final FileStorage storage;
    private final FileAssetRepository fileAssetRepository;
    private final SourceDocumentRepository documentRepository;
    private final DocumentTextExtractor extractor;
    private final Clock clock;

    public DocumentService(
            FileStorage storage,
            FileAssetRepository fileAssetRepository,
            SourceDocumentRepository documentRepository,
            DocumentTextExtractor extractor,
            Clock clock
    ) {
        this.storage = storage;
        this.fileAssetRepository = fileAssetRepository;
        this.documentRepository = documentRepository;
        this.extractor = extractor;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<DocumentSummaryResponse> list() {
        return documentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DocumentSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse detail(UUID id) {
        return DocumentDetailResponse.from(get(id));
    }

    @Transactional
    public DocumentDetailResponse upload(String title, Integer gradeLevel, MultipartFile file) {
        var bytes = bytes(file);
        var mediaType = detectType(bytes);
        var originalName = safeOriginalName(file.getOriginalFilename());
        verifyExtension(originalName, mediaType);
        var sha256 = sha256(bytes);
        if (fileAssetRepository.existsBySha256AndStatus(sha256, FileAssetStatus.STORED)) {
            throw new DocumentException(
                    "DOCUMENT_DUPLICATE", DocumentErrorType.CONFLICT,
                    "Tài liệu này đã được tải lên trước đó."
            );
        }
        var extracted = extractor.extract(bytes, mediaType);
        var resolvedTitle = title == null || title.isBlank() ? withoutExtension(originalName) : title.trim();
        if (resolvedTitle.length() > 255) {
            throw invalid("DOCUMENT_TITLE_TOO_LONG", "Tiêu đề tối đa 255 ký tự.");
        }

        String storageKey = null;
        try {
            storageKey = storage.store(bytes);
            var asset = fileAssetRepository.saveAndFlush(FileAsset.create(
                    storageKey, originalName, mediaType, bytes.length, sha256, now()
            ));
            var chunkInputs = extracted.stream()
                    .map(chunk -> new SourceDocument.ChunkInput(
                            chunk.pageNumber(), chunk.sourceLabel(), chunk.content()
                    ))
                    .toList();
            return DocumentDetailResponse.from(documentRepository.saveAndFlush(SourceDocument.create(
                    asset, resolvedTitle, gradeLevel, chunkInputs, now()
            )));
        } catch (RuntimeException exception) {
            if (storageKey != null) storage.deleteIfExists(storageKey);
            throw exception;
        }
    }

    @Transactional
    public DocumentDetailResponse archive(UUID id, long version) {
        var document = get(id);
        if (document.getVersion() != version) {
            throw new DocumentException(
                    "OPTIMISTIC_CONFLICT", DocumentErrorType.CONFLICT,
                    "Dữ liệu đã thay đổi. Hãy tải lại trước khi lưu trữ."
            );
        }
        document.archive(now());
        return DocumentDetailResponse.from(documentRepository.saveAndFlush(document));
    }

    private SourceDocument get(UUID id) {
        return documentRepository.findDetailById(id).orElseThrow(() -> new DocumentException(
                "DOCUMENT_NOT_FOUND", DocumentErrorType.NOT_FOUND, "Không tìm thấy tài liệu."
        ));
    }

    private byte[] bytes(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("DOCUMENT_FILE_EMPTY", "Hãy chọn một file PDF hoặc DOCX.");
        if (file.getSize() > MAX_FILE_BYTES) throw invalid("DOCUMENT_FILE_TOO_LARGE", "File tối đa 20 MB.");
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw invalid("DOCUMENT_READ_FAILED", "Không thể đọc file tải lên.");
        }
    }

    private String detectType(byte[] bytes) {
        if (bytes.length >= 5
                && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-') {
            return "application/pdf";
        }
        if (isDocx(bytes)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        throw invalid("DOCUMENT_TYPE_UNSUPPORTED", "Nội dung file không phải PDF hoặc DOCX hợp lệ.");
    }

    private boolean isDocx(byte[] bytes) {
        if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K') return false;
        var hasContentTypes = false;
        var hasDocument = false;
        try (var zip = new ZipInputStream(new java.io.ByteArrayInputStream(bytes))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if ("[Content_Types].xml".equals(entry.getName())) hasContentTypes = true;
                if ("word/document.xml".equals(entry.getName())) hasDocument = true;
                if (hasContentTypes && hasDocument) return true;
            }
            return false;
        } catch (IOException exception) {
            return false;
        }
    }

    private void verifyExtension(String name, String mediaType) {
        var lower = name.toLowerCase(Locale.ROOT);
        var valid = (mediaType.equals("application/pdf") && lower.endsWith(".pdf"))
                || (mediaType.contains("wordprocessingml") && lower.endsWith(".docx"));
        if (!valid) throw invalid("DOCUMENT_EXTENSION_MISMATCH", "Phần mở rộng file không khớp nội dung thực tế.");
    }

    private String safeOriginalName(String value) {
        var name = value == null ? "document" : value.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();
        if (name.isBlank()) name = "document";
        if (name.length() > 255) name = name.substring(name.length() - 255);
        return name;
    }

    private String withoutExtension(String name) {
        var dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM không hỗ trợ SHA-256.", exception);
        }
    }

    private DocumentException invalid(String code, String message) {
        return new DocumentException(code, DocumentErrorType.INVALID_STATE, message);
    }

    private Instant now() { return Instant.now(clock); }
}
