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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/class-groups")
public class ClassGroupController {

    private final ClassroomService classroomService;

    public ClassGroupController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    List<ClassGroupResponse> list(@RequestParam(required = false) UUID schoolYearId) {
        return classroomService.listClassGroups(schoolYearId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ClassGroupResponse create(@Valid @RequestBody ClassGroupRequest request) {
        return classroomService.createClassGroup(request);
    }

    @PutMapping("/{id}")
    ClassGroupResponse update(@PathVariable UUID id, @Valid @RequestBody ClassGroupRequest request) {
        return classroomService.updateClassGroup(id, request);
    }

    @PostMapping("/{id}/archive")
    ClassGroupResponse archive(@PathVariable UUID id, @Valid @RequestBody ArchiveRequest request) {
        return classroomService.archiveClassGroup(id, request.version());
    }
}
