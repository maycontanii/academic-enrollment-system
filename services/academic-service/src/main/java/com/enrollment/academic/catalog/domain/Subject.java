package com.enrollment.academic.catalog.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "subject")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    // Reference to the Course aggregate by identity (DDD: aggregates reference by id).
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(length = 1000)
    private String description;

    public Subject(String name, UUID courseId, String description) {
        this.name = name;
        this.courseId = courseId;
        this.description = description;
    }
}
