package com.geoteacher.classroom.api;

import com.geoteacher.classroom.application.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/class-enrollments")
public class ClassEnrollmentController {

    private final StudentService studentService;

    public ClassEnrollmentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    List<EnrollmentResponse> list(
            @RequestParam(required = false) UUID classGroupId,
            @RequestParam(required = false) UUID studentId
    ) {
        return studentService.listEnrollments(classGroupId, studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EnrollmentResponse enroll(@Valid @RequestBody EnrollmentRequest request) {
        return studentService.enroll(request);
    }

    @PostMapping("/{id}/leave")
    EnrollmentResponse leave(
            @PathVariable UUID id,
            @Valid @RequestBody LeaveEnrollmentRequest request
    ) {
        return studentService.leaveEnrollment(id, request.leftAt(), request.version());
    }
}
