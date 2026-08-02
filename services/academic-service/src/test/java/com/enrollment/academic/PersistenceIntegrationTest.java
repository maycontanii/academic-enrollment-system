package com.enrollment.academic;

import com.enrollment.academic.catalog.domain.*;
import com.enrollment.academic.enrollment.domain.*;
import com.enrollment.academic.catalog.repository.*;
import com.enrollment.academic.enrollment.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Context load validates every entity mapping against the Flyway schema
 * (spring.jpa.hibernate.ddl-auto=validate). The body exercises CRUD across the aggregates.
 */
@SpringBootTest
class PersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired StudentRepository students;
    @Autowired CourseRepository courses;
    @Autowired SubjectRepository subjects;
    @Autowired SchoolClassRepository classes;
    @Autowired EnrollmentRepository enrollments;

    @Test
    void persistsAndReadsBackTheAggregates() {
        Course course = courses.save(new Course("Computer Science", "CS program"));
        Subject subject = subjects.save(new Subject("Algorithms", course.getId(), "Intro to algorithms"));
        SchoolClass clazz = classes.save(new SchoolClass(subject.getId(), "2026.1 - A", 30));
        Student student = students.save(new Student("Ana Silva", "ana@x.com", "123"));
        Enrollment enrollment = enrollments.save(new Enrollment(student.getId(), clazz.getId()));

        // ids generated
        assertThat(course.getId()).isNotNull();
        assertThat(clazz.getId()).isNotNull();

        // defaults set by the domain
        assertThat(clazz.getStatus()).isEqualTo(ClassStatus.CLOSED);
        assertThat(clazz.getSeatsUsed()).isZero();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(enrollment.getCreatedAt()).isNotNull();

        // read back
        assertThat(classes.findById(clazz.getId())).isPresent();
        assertThat(enrollments.findById(enrollment.getId()))
                .get()
                .satisfies(e -> {
                    assertThat(e.getStudentId()).isEqualTo(student.getId());
                    assertThat(e.getClassId()).isEqualTo(clazz.getId());
                });
        assertThat(students.count()).isEqualTo(1);
    }
}
