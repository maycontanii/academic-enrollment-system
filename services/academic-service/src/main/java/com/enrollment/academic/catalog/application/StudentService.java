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

    /** The student linked to the given Keycloak identity — lets the caller resolve "who am I". */
    @Transactional(readOnly = true)
    public StudentResponse getByKeycloakId(String keycloakId) {
        return repository.findByKeycloakId(keycloakId)
                .map(StudentResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.STUDENT_NOT_FOUND, "No student is linked to this account"));
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
