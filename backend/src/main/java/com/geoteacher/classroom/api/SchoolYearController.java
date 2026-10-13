package com.geoteacher.classroom.api;

import com.geoteacher.classroom.application.ClassroomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school-years")
public class SchoolYearController {

    private final ClassroomService classroomService;

    public SchoolYearController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    List<SchoolYearResponse> list() {
        return classroomService.listSchoolYears();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SchoolYearResponse create(@Valid @RequestBody SchoolYearRequest request) {
        return classroomService.createSchoolYear(request);
    }

    @PutMapping("/{id}")
    SchoolYearResponse update(@PathVariable UUID id, @Valid @RequestBody SchoolYearRequest request) {
        return classroomService.updateSchoolYear(id, request);
    }

    @PostMapping("/{id}/archive")
    SchoolYearResponse archive(@PathVariable UUID id, @Valid @RequestBody ArchiveRequest request) {
        return classroomService.archiveSchoolYear(id, request.version());
    }
}
