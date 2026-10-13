package com.geoteacher.question.api;

import com.geoteacher.question.application.QuestionService;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping
    List<QuestionSummaryResponse> search(
            @RequestParam(defaultValue = "") @Size(max = 150) String query,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) QuestionStatus status,
            @RequestParam(required = false) @Min(1) @Max(12) Integer gradeLevel
    ) {
        return service.search(query, type, status, gradeLevel);
    }

    @GetMapping("/{id}")
    QuestionDetailResponse detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    @PostMapping
    QuestionDetailResponse create(@Valid @RequestBody QuestionRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    QuestionDetailResponse update(@PathVariable UUID id, @Valid @RequestBody QuestionRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/archive")
    QuestionDetailResponse archive(@PathVariable UUID id, @Valid @RequestBody QuestionArchiveRequest request) {
        return service.archive(id, request.version());
    }
}
