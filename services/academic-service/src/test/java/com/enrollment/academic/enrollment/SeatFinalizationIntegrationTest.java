package com.enrollment.academic.enrollment;

import com.enrollment.academic.catalog.domain.ClassStatus;
import com.enrollment.academic.catalog.domain.Course;
import com.enrollment.academic.catalog.domain.SchoolClass;
import com.enrollment.academic.catalog.domain.Student;
import com.enrollment.academic.catalog.domain.Subject;
import com.enrollment.academic.catalog.repository.CourseRepository;
import com.enrollment.academic.catalog.repository.SchoolClassRepository;
import com.enrollment.academic.catalog.repository.StudentRepository;
import com.enrollment.academic.catalog.repository.SubjectRepository;
import com.enrollment.academic.enrollment.application.EnrollmentFinalizer;
import com.enrollment.academic.enrollment.application.EnrollmentService;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SeatFinalizationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired CourseRepository courses;
    @Autowired SubjectRepository subjects;
    @Autowired SchoolClassRepository classes;
    @Autowired StudentRepository students;
    @Autowired EnrollmentRepository enrollments;
    @Autowired EnrollmentFinalizer finalizer;
    @Autowired EnrollmentService service;

    @Test
    void confirmsOneRejectsWhenFullAndCancelReleases() {
        Course course = courses.save(new Course("Computer Science", null));
        Subject subject = subjects.save(new Subject("Algorithms", course.getId(), null));
        SchoolClass clazz = new SchoolClass(subject.getId(), "2026.1 - A", 1); // one seat
        clazz.setStatus(ClassStatus.OPEN);
        clazz = classes.save(clazz);
        Student s1 = students.save(new Student("A", "a@x.com", null));
        Student s2 = students.save(new Student("B", "b@x.com", null));

        Enrollment e1 = processing(s1.getId(), clazz.getId());
        Enrollment e2 = processing(s2.getId(), clazz.getId());

        // first takes the only seat, second is rejected
        assertThat(finalizer.finalizeEnrollment(e1.getId())).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(finalizer.finalizeEnrollment(e2.getId())).isEqualTo(EnrollmentStatus.REJECTED);
        assertThat(classes.findById(clazz.getId()).orElseThrow().getSeatsUsed()).isEqualTo(1);

        // cancelling the confirmed one releases the seat
        service.cancel(e1.getId());
        assertThat(classes.findById(clazz.getId()).orElseThrow().getSeatsUsed()).isZero();
    }

    private Enrollment processing(UUID studentId, UUID classId) {
        Enrollment e = new Enrollment(studentId, classId);
        e.startProcessing();
        return enrollments.save(e);
    }
}
