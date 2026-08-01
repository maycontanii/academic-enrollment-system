package com.enrollment.academic.enrollment.web;

import com.enrollment.academic.enrollment.application.EnrollmentService;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.dto.EnrollmentResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse create(@Valid @RequestBody EnrollmentRequest request) {
        return service.create(request);
    }

    @GetMapping
    public Page<EnrollmentResponse> list(@RequestParam(required = false) UUID studentId,
                                         @RequestParam(required = false) UUID classId,
                                         @RequestParam(required = false) EnrollmentStatus status,
                                         Pageable pageable) {
        return service.search(studentId, classId, status, pageable);
    }

    @GetMapping("/{id}")
    public EnrollmentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
