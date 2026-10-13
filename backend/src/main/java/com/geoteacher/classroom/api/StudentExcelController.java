package com.geoteacher.classroom.api;

import com.geoteacher.classroom.application.StudentExcelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student-excel")
public class StudentExcelController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final StudentExcelService service;

    public StudentExcelController(StudentExcelService service) {
        this.service = service;
    }

    @GetMapping("/template")
    ResponseEntity<byte[]> template() {
        return excel("geoteacher-student-import-template.xlsx", service.template());
    }

    @GetMapping("/export")
    ResponseEntity<byte[]> export(@RequestParam UUID classGroupId) {
        return excel("geoteacher-class-roster.xlsx", service.exportClassRoster(classGroupId));
    }

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    StudentImportPreviewResponse preview(
            @RequestParam UUID classGroupId,
            @RequestPart("file") MultipartFile file
    ) {
        return service.preview(classGroupId, file);
    }

    @PostMapping(value = "/confirm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    StudentImportResultResponse confirm(
            @RequestParam UUID classGroupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enrolledAt,
            @RequestPart("file") MultipartFile file
    ) {
        return service.confirm(classGroupId, enrolledAt, file);
    }

    private ResponseEntity<byte[]> excel(String filename, byte[] body) {
        var disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(body);
    }
}
