package com.enrollment.academic.catalog.application;

import com.enrollment.academic.shared.error.ErrorCodes;

import com.enrollment.academic.shared.error.NotFoundException;

import com.enrollment.academic.catalog.domain.Course;
import com.enrollment.academic.catalog.dto.CourseRequest;
import com.enrollment.academic.catalog.dto.CourseResponse;
import com.enrollment.academic.catalog.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = new Course(request.name(), request.description());
        return CourseResponse.from(repository.save(course));
    }

    @Transactional
    public CourseResponse update(UUID id, CourseRequest request) {
        Course course = find(id);
        course.setName(request.name());
        course.setDescription(request.description());
        return CourseResponse.from(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(CourseResponse::from);
    }

    @Transactional(readOnly = true)
    public CourseResponse get(UUID id) {
        return CourseResponse.from(find(id));
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    private Course find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.COURSE_NOT_FOUND, "Course not found"));
    }
}
