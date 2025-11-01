package com.levelupjourney.learningservice.courses.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_authors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    public CourseAuthor(Course course, String authorId) {
        this.course = course;
        this.authorId = authorId;
    }
}