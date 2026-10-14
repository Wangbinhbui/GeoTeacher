package com.geoteacher.document.api;

import com.geoteacher.document.application.DocumentService;
import com.geoteacher.document.domain.DocumentKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @GetMapping
    List<DocumentSummaryResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    DocumentDetailResponse detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    DocumentDetailResponse upload(
            @RequestParam(required = false) @Size(max = 255) String title,
            @RequestParam(required = false) @Min(1) @Max(12) Integer gradeLevel,
            @RequestParam(defaultValue = "KNOWLEDGE") DocumentKind documentKind,
            @RequestPart("file") MultipartFile file
    ) {
        return service.upload(title, gradeLevel, documentKind, file);
    }

    @PostMapping("/{id}/archive")
    DocumentDetailResponse archive(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentArchiveRequest request
    ) {
        return service.archive(id, request.version());
    }
}
