package com.enrollment.academic.catalog.application;

import com.enrollment.academic.shared.error.NotFoundException;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.dto.ClassRequest;
import com.enrollment.academic.catalog.dto.ClassResponse;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.catalog.repository.SubjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClassService {

    private final SchoolClassRepository repository;
    private final SubjectRepository subjectRepository;

    public ClassService(SchoolClassRepository repository, SubjectRepository subjectRepository) {
        this.repository = repository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public ClassResponse create(ClassRequest request) {
        requireSubject(request.subjectId());
        SchoolClass clazz = new SchoolClass(request.subjectId(), request.label(), request.seatLimit());
        return ClassResponse.from(repository.save(clazz));
    }

    @Transactional
    public ClassResponse update(UUID id, ClassRequest request) {
        SchoolClass clazz = find(id);
        requireSubject(request.subjectId());
        clazz.setSubjectId(request.subjectId());
        clazz.setLabel(request.label());
        clazz.setSeatLimit(request.seatLimit());
        return ClassResponse.from(clazz);
    }

    @Transactional(readOnly = true)
    public Page<ClassResponse> list(UUID subjectId, ClassStatus status, Pageable pageable) {
        Page<SchoolClass> page;
        if (subjectId != null && status != null) {
            page = repository.findBySubjectIdAndStatus(subjectId, status, pageable);
        } else if (subjectId != null) {
            page = repository.findBySubjectId(subjectId, pageable);
        } else if (status != null) {
            page = repository.findByStatus(status, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        return page.map(ClassResponse::from);
    }

    @Transactional(readOnly = true)
    public ClassResponse get(UUID id) {
        return ClassResponse.from(find(id));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    @Transactional
    public ClassResponse open(UUID id) {
        SchoolClass clazz = find(id);
        clazz.setStatus(ClassStatus.OPEN);
        return ClassResponse.from(clazz);
    }

    @Transactional
    public ClassResponse close(UUID id) {
        SchoolClass clazz = find(id);
        clazz.setStatus(ClassStatus.CLOSED);
        return ClassResponse.from(clazz);
    }

    private void requireSubject(UUID subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new NotFoundException("subject.not_found", "Subject not found");
        }
    }

    private SchoolClass find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("class.not_found", "Class not found"));
    }
}
