package com.enrollment.academic.catalog.web;

import com.enrollment.academic.catalog.application.SubjectService;
import com.enrollment.academic.catalog.dto.SubjectRequest;
import com.enrollment.academic.catalog.dto.SubjectResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService service;

    public SubjectController(SubjectService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('adm_create_subject')")
    public SubjectResponse create(@Valid @RequestBody SubjectRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('adm_update_subject')")
    public SubjectResponse update(@PathVariable UUID id, @Valid @RequestBody SubjectRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('adm_read_subject', 'student_browse_class')")
    public Page<SubjectResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('adm_read_subject', 'student_browse_class')")
    public SubjectResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('adm_delete_subject')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
