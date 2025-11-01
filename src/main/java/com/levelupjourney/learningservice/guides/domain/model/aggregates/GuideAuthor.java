package com.levelupjourney.learningservice.guides.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "guide_authors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuideAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;

    @Column(name = "author_id", nullable = false)
    private String authorId;

    public GuideAuthor(Guide guide, String authorId) {
        this.guide = guide;
        this.authorId = authorId;
    }
}