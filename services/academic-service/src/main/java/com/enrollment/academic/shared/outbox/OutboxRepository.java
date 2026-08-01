package com.enrollment.academic.shared.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {

    /** Unpublished messages, oldest first — consumed by the relay (T9). */
    List<OutboxMessage> findByPublishedAtIsNullOrderByCreatedAtAsc();
}
