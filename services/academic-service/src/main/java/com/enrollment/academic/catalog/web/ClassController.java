package com.enrollment.academic.catalog.web;

import com.enrollment.academic.catalog.application.ClassService;
import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.dto.ClassRequest;
import com.enrollment.academic.catalog.dto.ClassResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    public ClassResponse create(@Valid @RequestBody ClassRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ClassResponse update(@PathVariable UUID id, @Valid @RequestBody ClassRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    public Page<ClassResponse> list(@RequestParam(required = false) UUID subjectId,
                                    @RequestParam(required = false) ClassStatus status,
                                    Pageable pageable) {
        return service.list(subjectId, status, pageable);
    }

    @GetMapping("/{id}")
    public ClassResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PostMapping("/{id}/open")
    public ClassResponse open(@PathVariable UUID id) {
        return service.open(id);
    }

    @PostMapping("/{id}/close")
    public ClassResponse close(@PathVariable UUID id) {
        return service.close(id);
    }
}
