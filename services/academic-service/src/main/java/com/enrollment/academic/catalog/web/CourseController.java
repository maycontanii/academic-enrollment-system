package com.enrollment.academic.catalog.web;

import com.enrollment.academic.catalog.application.CourseService;
import com.enrollment.academic.catalog.dto.CourseRequest;
import com.enrollment.academic.catalog.dto.CourseResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('adm_create_course')")
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('adm_update_course')")
    public CourseResponse update(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('adm_read_course')")
    public Page<CourseResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('adm_read_course')")
    public CourseResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('adm_delete_course')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
