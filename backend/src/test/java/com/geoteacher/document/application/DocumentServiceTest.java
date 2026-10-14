package com.geoteacher.document.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.geoteacher.document.infrastructure.SourceDocumentRepository;
import com.geoteacher.file.application.FileStorage;
import com.geoteacher.file.domain.FileAssetStatus;
import com.geoteacher.file.infrastructure.FileAssetRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock FileStorage storage;
    @Mock FileAssetRepository fileAssetRepository;
    @Mock SourceDocumentRepository documentRepository;
    @Mock DocumentTextExtractor extractor;

    private DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService(
                storage, fileAssetRepository, documentRepository, extractor, Clock.systemUTC());
    }

    @Test
    void rejectsFileWhoseBytesAreNotPdfOrDocx() {
        var file = new MockMultipartFile(
                "file", "giao-trinh.pdf", "application/pdf", "not a pdf".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.upload("Địa lý 10", 10, file))
                .isInstanceOf(DocumentException.class)
                .extracting(exception -> ((DocumentException) exception).getCode())
                .isEqualTo("DOCUMENT_TYPE_UNSUPPORTED");

        verify(storage, never()).store(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsDuplicateBeforeExtractionAndStorage() {
        var file = new MockMultipartFile(
                "file", "giao-trinh.pdf", "application/pdf", "%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        when(fileAssetRepository.existsBySha256AndStatus(anyString(), eq(FileAssetStatus.STORED)))
                .thenReturn(true);

        assertThatThrownBy(() -> service.upload(null, null, file))
                .isInstanceOf(DocumentException.class)
                .extracting(exception -> ((DocumentException) exception).getCode())
                .isEqualTo("DOCUMENT_DUPLICATE");

        verify(extractor, never()).extract(org.mockito.ArgumentMatchers.any(), anyString());
        verify(storage, never()).store(org.mockito.ArgumentMatchers.any());
    }
}
