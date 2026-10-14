package com.geoteacher.document.application;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Trích text cục bộ; PDF scan không có text được báo rõ thay vì âm thầm tạo nguồn rỗng. */
@Component
public class DocumentTextExtractor {

    static final int MAX_CHUNK_CHARS = 2_000;
    static final int CHUNK_OVERLAP = 200;

    public List<ExtractedChunk> extract(byte[] content, String mediaType) {
        try {
            var sections = switch (mediaType) {
                case "application/pdf" -> extractPdf(content);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> extractDocx(content);
                default -> throw invalidType();
            };
            var chunks = new ArrayList<ExtractedChunk>();
            for (var section : sections) chunks.addAll(chunk(section));
            if (chunks.isEmpty()) {
                throw new DocumentException(
                        "DOCUMENT_NO_TEXT", DocumentErrorType.INVALID_STATE,
                        "Không đọc được chữ trong tài liệu. PDF scan cần OCR và chưa được hỗ trợ ở lát cắt này."
                );
            }
            return chunks;
        } catch (DocumentException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new DocumentException(
                    "DOCUMENT_PARSE_FAILED", DocumentErrorType.INVALID_STATE,
                    "Tài liệu bị hỏng, có mật khẩu hoặc không đúng định dạng PDF/DOCX."
            );
        }
    }

    private List<Section> extractPdf(byte[] content) throws IOException {
        try (var document = Loader.loadPDF(content)) {
            if (document.isEncrypted()) {
                throw new DocumentException(
                        "DOCUMENT_PASSWORD_PROTECTED", DocumentErrorType.INVALID_STATE,
                        "PDF có mật khẩu chưa được hỗ trợ."
                );
            }
            var stripper = new PDFTextStripper();
            var sections = new ArrayList<Section>();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                var text = clean(stripper.getText(document));
                if (!text.isBlank()) sections.add(new Section(page, "Trang " + page, text));
            }
            return sections;
        }
    }

    private List<Section> extractDocx(byte[] content) throws IOException {
        try (var document = new XWPFDocument(new ByteArrayInputStream(content));
             var extractor = new XWPFWordExtractor(document)) {
            var text = clean(extractor.getText());
            return text.isBlank() ? List.of() : List.of(new Section(null, "Tài liệu DOCX", text));
        }
    }

    private List<ExtractedChunk> chunk(Section section) {
        var text = section.content();
        var chunks = new ArrayList<ExtractedChunk>();
        var start = 0;
        while (start < text.length()) {
            var end = Math.min(start + MAX_CHUNK_CHARS, text.length());
            if (end < text.length()) {
                var boundary = text.lastIndexOf(' ', end);
                if (boundary > start + MAX_CHUNK_CHARS / 2) end = boundary;
            }
            var value = text.substring(start, end).trim();
            if (!value.isBlank()) chunks.add(new ExtractedChunk(section.pageNumber(), section.label(), value));
            if (end >= text.length()) break;
            start = Math.max(start + 1, end - CHUNK_OVERLAP);
        }
        return chunks;
    }

    private String clean(String value) {
        return value.replace("\r", "")
                .replaceAll("[\\t\\x0B\\f ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private DocumentException invalidType() {
        return new DocumentException(
                "DOCUMENT_TYPE_UNSUPPORTED", DocumentErrorType.INVALID_STATE,
                "Chỉ hỗ trợ PDF hoặc DOCX có thể chọn được chữ."
        );
    }

    private record Section(Integer pageNumber, String label, String content) {
    }

    public record ExtractedChunk(Integer pageNumber, String sourceLabel, String content) {
    }
}
