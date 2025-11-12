package com.levelupjourney.learningservice.guides.domain.model.entities;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents a like given by a user to a guide
 */
@Entity
@Table(
    name = "guide_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"guide_id", "user_id"})
)
@Getter
@NoArgsConstructor
public class GuideLike extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    public GuideLike(Guide guide, String userId) {
        this.guide = guide;
        this.userId = userId;
    }
}
