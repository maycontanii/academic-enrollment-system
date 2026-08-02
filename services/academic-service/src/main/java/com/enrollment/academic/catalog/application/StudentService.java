package com.enrollment.academic.catalog.application;

import com.enrollment.academic.shared.error.ErrorCodes;

import com.enrollment.academic.shared.error.ConflictException;
import com.enrollment.academic.shared.error.NotFoundException;

import com.enrollment.academic.catalog.domain.Student;
import com.enrollment.academic.catalog.dto.StudentRequest;
import com.enrollment.academic.catalog.dto.StudentResponse;
import com.enrollment.academic.catalog.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflictException(ErrorCodes.STUDENT_EMAIL_DUPLICATE, "Email already registered");
        }
        Student student = new Student(request.name(), request.email(), request.document());
        return StudentResponse.from(repository.save(student));
    }

    @Transactional
    public StudentResponse update(UUID id, StudentRequest request) {
        Student student = find(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException(ErrorCodes.STUDENT_EMAIL_DUPLICATE, "Email already registered");
        }
        student.setName(request.name());
        student.setEmail(request.email());
        student.setDocument(request.document());
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(StudentResponse::from);
    }

    @Transactional(readOnly = true)
    public StudentResponse get(UUID id) {
        return StudentResponse.from(find(id));
    }

    /**
     * Resolves "who am I" for a logged-in user, linking on first login (Keycloak owns identity; the
     * app owns the academic record). If already linked, returns it. Otherwise, for a student, links an
     * existing record by email or materializes one from the verified token. Non-students that aren't
     * linked get a 404 — we never auto-create a student for an admin.
     */
    @Transactional
    public StudentResponse resolveMe(String keycloakId, String email, String name, boolean canProvision) {
        Optional<Student> linked = repository.findByKeycloakId(keycloakId);
        if (linked.isPresent()) {
            return StudentResponse.from(linked.get());
        }
        if (!canProvision || email == null) {
            throw new NotFoundException(ErrorCodes.STUDENT_NOT_FOUND, "No student is linked to this account");
        }
        Student student = repository.findByEmail(email)
                .orElseGet(() -> new Student(name != null ? name : email, email, null));
        student.setKeycloakId(keycloakId);
        return StudentResponse.from(repository.save(student));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private Student find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.STUDENT_NOT_FOUND, "Student not found"));
    }
}
