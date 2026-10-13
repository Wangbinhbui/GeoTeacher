package com.geoteacher.classroom.api;

import com.geoteacher.classroom.application.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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

@Validated
@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    List<StudentResponse> search(@RequestParam(defaultValue = "") @Size(max = 150) String query) {
        return studentService.searchStudents(query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return studentService.createStudent(request);
    }

    @PutMapping("/{id}")
    StudentResponse update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        return studentService.updateStudent(id, request);
    }

    @PostMapping("/{id}/archive")
    StudentResponse archive(@PathVariable UUID id, @Valid @RequestBody ArchiveRequest request) {
        return studentService.archiveStudent(id, request.version());
    }
}
