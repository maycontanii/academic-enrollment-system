package com.enrollment.academic.catalog.web;

import com.enrollment.academic.catalog.application.StudentService;
import com.enrollment.academic.catalog.dto.StudentRequest;
import com.enrollment.academic.catalog.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    /**
     * The caller's own student profile, resolved from the JWT. On a student's first login this links
     * an existing record by email or materializes one from the token (Keycloak stays the identity
     * source of truth); admins without a linked record get a 404.
     */
    @GetMapping("/me")
    public StudentResponse me(@AuthenticationPrincipal Jwt jwt) {
        String name = jwt.getClaimAsString("name");
        if (name == null) {
            name = jwt.getClaimAsString("preferred_username");
        }
        return service.resolveMe(jwt.getSubject(), jwt.getClaimAsString("email"), name, hasRealmRole(jwt, "STUDENT"));
    }

    private boolean hasRealmRole(Jwt jwt, String role) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> roles) {
            return roles.stream().map(Object::toString).anyMatch(role::equals);
        }
        return false;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('adm_create_student')")
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('adm_update_student')")
    public StudentResponse update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('adm_read_student')")
    public Page<StudentResponse> list(Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('adm_read_student')")
    public StudentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('adm_delete_student')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
