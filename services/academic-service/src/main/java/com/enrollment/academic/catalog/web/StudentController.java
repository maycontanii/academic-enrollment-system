package com.enrollment.academic.catalog.web;

import com.enrollment.academic.catalog.application.StudentService;
import com.enrollment.academic.catalog.dto.StudentRequest;
import com.enrollment.academic.catalog.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    public Page<StudentResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
