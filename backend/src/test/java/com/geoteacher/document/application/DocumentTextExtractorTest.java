package com.geoteacher.document.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

class DocumentTextExtractorTest {

    private final DocumentTextExtractor extractor = new DocumentTextExtractor();

    @Test
    void extractsTextAndCitationLabelFromDocx() throws Exception {
        byte[] content;
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Khí hậu nhiệt đới gió mùa Việt Nam");
            document.createParagraph().createRun().setText("Lượng mưa phân bố không đều theo mùa.");
            document.write(output);
            content = output.toByteArray();
        }

        var chunks = extractor.extract(
                content,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.getFirst().sourceLabel()).isEqualTo("Tài liệu DOCX");
        assertThat(chunks.getFirst().content())
                .contains("Khí hậu nhiệt đới gió mùa Việt Nam")
                .contains("Lượng mưa phân bố không đều theo mùa");
    }

    @Test
    void splitsLongDocxIntoBoundedOverlappingChunks() throws Exception {
        byte[] content;
        String paragraph = "Đặc điểm địa hình Việt Nam gồm đồi núi, đồng bằng và bờ biển. ".repeat(90);
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(paragraph);
            document.write(output);
            content = output.toByteArray();
        }

        var chunks = extractor.extract(
                content,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.content().length()).isLessThanOrEqualTo(2000));
    }
}
