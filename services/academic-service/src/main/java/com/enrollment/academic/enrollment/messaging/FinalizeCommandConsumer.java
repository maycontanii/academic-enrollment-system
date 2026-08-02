package com.enrollment.academic.enrollment.messaging;

import com.enrollment.academic.enrollment.application.EnrollmentFinalizer;
import com.enrollment.academic.enrollment.application.EnrollmentOutbox;
import com.enrollment.academic.shared.messaging.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes finalize commands and runs the seat finalization. Competing consumers across instances
 * are what create the last-seat race; the optimistic lock in the finalizer resolves it.
 */
@Component
public class FinalizeCommandConsumer {

    private final EnrollmentFinalizer finalizer;

    public FinalizeCommandConsumer(EnrollmentFinalizer finalizer) {
        this.finalizer = finalizer;
    }

    @RabbitListener(queues = RabbitConfig.FINALIZE_QUEUE)
    public void onFinalize(EnrollmentOutbox.FinalizeCommand command) {
        finalizer.finalizeEnrollment(command.enrollmentId());
    }
}
