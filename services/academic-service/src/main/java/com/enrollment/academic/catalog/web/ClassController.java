package com.enrollment.academic.catalog.web;

import com.enrollment.academic.catalog.application.ClassService;
import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.dto.ClassRequest;
import com.enrollment.academic.catalog.dto.ClassResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final ClassService service;

    public ClassController(ClassService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('adm_create_class')")
    public ClassResponse create(@Valid @RequestBody ClassRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('adm_update_class')")
    public ClassResponse update(@PathVariable UUID id, @Valid @RequestBody ClassRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('adm_read_class', 'student_browse_class')")
    public Page<ClassResponse> list(@RequestParam(required = false) UUID subjectId,
                                    @RequestParam(required = false) ClassStatus status,
                                    Pageable pageable) {
        return service.list(subjectId, status, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('adm_read_class', 'student_browse_class')")
    public ClassResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('adm_delete_class')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/open")
    @PreAuthorize("hasAuthority('adm_open_class')")
    public ClassResponse open(@PathVariable UUID id) {
        return service.open(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('adm_close_class')")
    public ClassResponse close(@PathVariable UUID id) {
        return service.close(id);
    }
}
