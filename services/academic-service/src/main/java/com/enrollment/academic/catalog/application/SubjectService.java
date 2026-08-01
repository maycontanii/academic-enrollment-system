package com.enrollment.academic.catalog.application;

import com.enrollment.academic.shared.error.ErrorCodes;

import com.enrollment.academic.shared.error.NotFoundException;

import com.enrollment.academic.catalog.domain.Subject;
import com.enrollment.academic.catalog.dto.SubjectRequest;
import com.enrollment.academic.catalog.dto.SubjectResponse;
import com.enrollment.academic.catalog.repository.CourseRepository;
import com.enrollment.academic.catalog.repository.SubjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubjectService {

    private final SubjectRepository repository;
    private final CourseRepository courseRepository;

    public SubjectService(SubjectRepository repository, CourseRepository courseRepository) {
        this.repository = repository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        requireCourse(request.courseId());
        Subject subject = new Subject(request.name(), request.courseId(), request.description());
        return SubjectResponse.from(repository.save(subject));
    }

    @Transactional
    public SubjectResponse update(UUID id, SubjectRequest request) {
        Subject subject = find(id);
        requireCourse(request.courseId());
        subject.setName(request.name());
        subject.setCourseId(request.courseId());
        subject.setDescription(request.description());
        return SubjectResponse.from(subject);
    }

    @Transactional(readOnly = true)
    public Page<SubjectResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(SubjectResponse::from);
    }

    @Transactional(readOnly = true)
    public SubjectResponse get(UUID id) {
        return SubjectResponse.from(find(id));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private void requireCourse(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new NotFoundException(ErrorCodes.COURSE_NOT_FOUND, "Course not found");
        }
    }

    private Subject find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.SUBJECT_NOT_FOUND, "Subject not found"));
    }
}
