package com.geoteacher.ai.api;

import com.geoteacher.ai.application.AiExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/exam-drafts")
public class AiExamController {
    private final AiExamService service;

    public AiExamController(AiExamService service) {
        this.service = service;
    }

    @GetMapping("/status")
    AiStatusResponse status() { return service.status(); }

    @GetMapping
    List<AiExamDraftResponse> list() { return service.list(); }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    AiExamDraftResponse generate(@Valid @RequestBody GenerateAiExamRequest request) {
        return service.generate(request);
    }
}
