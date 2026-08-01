package com.enrollment.academic.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The Class aggregate (Turma) — a specific offering of a subject with a seat limit.
 * Named {@code SchoolClass} in code to avoid a collision with {@link java.lang.Class};
 * the table and the ubiquitous language remain "class".
 */
@Entity
@Table(name = "class")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(nullable = false)
    private String label;

    @Column(name = "seat_limit", nullable = false)
    private int seatLimit;

    @Column(name = "seats_used", nullable = false)
    private int seatsUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassStatus status;

    // Optimistic lock — the seat guarantee is enforced against this version.
    @Version
    @Column(nullable = false)
    private long version;

    public SchoolClass(UUID subjectId, String label, int seatLimit) {
        this.subjectId = subjectId;
        this.label = label;
        this.seatLimit = seatLimit;
        this.seatsUsed = 0;
        this.status = ClassStatus.CLOSED;
    }
}
