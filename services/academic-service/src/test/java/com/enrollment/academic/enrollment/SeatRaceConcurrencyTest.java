package com.enrollment.academic.enrollment;

import com.enrollment.academic.AbstractIntegrationTest;

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
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The last-seat race, for real: N threads finalize enrollments on the same class at once.
 * The optimistic lock on the seat counter must let through exactly the seat limit — no oversell.
 */
@SpringBootTest
class SeatRaceConcurrencyTest extends AbstractIntegrationTest {

    @Autowired CourseRepository courses;
    @Autowired SubjectRepository subjects;
    @Autowired SchoolClassRepository classes;
    @Autowired StudentRepository students;
    @Autowired EnrollmentRepository enrollments;
    @Autowired EnrollmentFinalizer finalizer;

    @Test
    void lastSeat_exactlyOneConfirmed() throws Exception {
        runRace(1, 8);
    }

    @Test
    void multipleSeats_exactlySeatLimitConfirmed() throws Exception {
        runRace(3, 8);
    }

    private void runRace(int seatLimit, int contenders) throws Exception {
        Course course = courses.save(new Course("Computer Science", null));
        Subject subject = subjects.save(new Subject("Algorithms", course.getId(), null));
        SchoolClass clazz = new SchoolClass(subject.getId(), "2026.1 - A", seatLimit);
        clazz.setStatus(ClassStatus.OPEN);
        UUID classId = classes.save(clazz).getId();

        List<UUID> enrollmentIds = new ArrayList<>();
        for (int i = 0; i < contenders; i++) {
            Student student = students.save(new Student("S" + i, "s-" + UUID.randomUUID() + "@x.com", null));
            Enrollment e = new Enrollment(student.getId(), classId);
            e.startProcessing();
            enrollmentIds.add(enrollments.save(e).getId());
        }

        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<EnrollmentStatus>> futures = new ArrayList<>();
        for (UUID id : enrollmentIds) {
            futures.add(pool.submit(() -> {
                start.await();                       // all threads block, then fire together
                return finalizer.finalizeEnrollment(id);
            }));
        }
        start.countDown();

        List<EnrollmentStatus> outcomes = new ArrayList<>();
        for (Future<EnrollmentStatus> f : futures) {
            outcomes.add(f.get(30, TimeUnit.SECONDS));
        }
        pool.shutdownNow();

        long confirmed = outcomes.stream().filter(s -> s == EnrollmentStatus.CONFIRMED).count();
        long rejected = outcomes.stream().filter(s -> s == EnrollmentStatus.REJECTED).count();

        assertThat(confirmed).as("confirmed").isEqualTo(seatLimit);
        assertThat(rejected).as("rejected").isEqualTo(contenders - seatLimit);
        assertThat(classes.findById(classId).orElseThrow().getSeatsUsed())
                .as("seats consumed").isEqualTo(seatLimit);
    }
}
