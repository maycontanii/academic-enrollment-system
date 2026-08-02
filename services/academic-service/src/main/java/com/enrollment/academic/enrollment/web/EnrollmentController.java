package com.enrollment.academic.enrollment.web;

import com.enrollment.academic.enrollment.application.EnrollmentService;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.dto.EnrollmentResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyAuthority('adm_create_enrollment', 'student_create_enrollment')")
    public EnrollmentResponse create(@Valid @RequestBody EnrollmentRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('adm_read_enrollment', 'student_read_enrollment')")
    public Page<EnrollmentResponse> list(@RequestParam(required = false) UUID studentId,
                                         @RequestParam(required = false) UUID classId,
                                         @RequestParam(required = false) EnrollmentStatus status,
                                         Pageable pageable) {
        return service.search(studentId, classId, status, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('adm_read_enrollment', 'student_read_enrollment')")
    public EnrollmentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyAuthority('adm_confirm_enrollment', 'student_confirm_enrollment')")
    public EnrollmentResponse confirm(@PathVariable UUID id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('adm_cancel_enrollment', 'student_cancel_enrollment')")
    public EnrollmentResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }
}
