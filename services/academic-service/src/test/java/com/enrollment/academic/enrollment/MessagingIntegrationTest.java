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
import com.enrollment.academic.enrollment.application.EnrollmentOutbox;
import com.enrollment.academic.enrollment.application.EnrollmentService;
import com.enrollment.academic.enrollment.domain.Enrollment;
import com.enrollment.academic.enrollment.domain.EnrollmentStatus;
import com.enrollment.academic.enrollment.dto.EnrollmentRequest;
import com.enrollment.academic.enrollment.repository.EnrollmentRepository;
import com.enrollment.academic.shared.messaging.EventEnvelope;
import com.enrollment.academic.shared.messaging.RabbitConfig;
import com.enrollment.academic.shared.outbox.OutboxRelay;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MessagingIntegrationTest extends AbstractIntegrationTest {

    @Autowired CourseRepository courses;
    @Autowired SubjectRepository subjects;
    @Autowired SchoolClassRepository classes;
    @Autowired StudentRepository students;
    @Autowired EnrollmentRepository enrollments;
    @Autowired EnrollmentService service;
    @Autowired OutboxRelay relay;
    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired AmqpAdmin amqpAdmin;

    @Test
    void relayPublishesDomainEventToFanout() {
        Queue probe = new AnonymousQueue();
        amqpAdmin.declareQueue(probe);
        amqpAdmin.declareBinding(BindingBuilder.bind(probe).to(new FanoutExchange(RabbitConfig.EVENTS_EXCHANGE)));

        UUID classId = openClass();
        UUID studentId = students.save(new Student("Ana Silva", "ana@x.com", null)).getId();
        service.create(new EnrollmentRequest(studentId, classId)); // writes enrollment.created to the outbox

        relay.relay();

        Object received = rabbitTemplate.receiveAndConvert(probe.getName(), 5000);
        assertThat(received).isInstanceOf(EventEnvelope.class);
        EventEnvelope envelope = (EventEnvelope) received;
        assertThat(envelope.type()).isEqualTo(EnrollmentOutbox.CREATED);
        assertThat(envelope.eventId()).isNotBlank();
        assertThat(envelope.payload().get("classId").asText()).isEqualTo(classId.toString());
    }

    @Test
    void confirmFinalizesThroughTheLoop() throws Exception {
        UUID classId = openClass();
        UUID studentId = students.save(new Student("Ana Silva", "ana@x.com", null)).getId();
        UUID enrollmentId = service.create(new EnrollmentRequest(studentId, classId)).id();

        service.confirm(enrollmentId);   // -> PROCESSING + finalize command in the outbox
        relay.relay();                   // publish the command; the listener finalizes asynchronously

        awaitStatus(enrollmentId, EnrollmentStatus.CONFIRMED);
        assertThat(classes.findById(classId).orElseThrow().getSeatsUsed()).isEqualTo(1);
    }

    private UUID openClass() {
        Course course = courses.save(new Course("Computer Science", null));
        Subject subject = subjects.save(new Subject("Algorithms", course.getId(), null));
        SchoolClass clazz = new SchoolClass(subject.getId(), "2026.1 - A", 30);
        clazz.setStatus(ClassStatus.OPEN);
        return classes.save(clazz).getId();
    }

    private void awaitStatus(UUID id, EnrollmentStatus expected) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (enrollments.findById(id).map(Enrollment::getStatus).filter(s -> s == expected).isPresent()) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError("timed out waiting for status " + expected);
    }
}
