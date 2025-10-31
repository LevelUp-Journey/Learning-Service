package com.levelupjourney.learningservice.learningprogress.domain.model.aggregates;

import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.ProgressStatus;
import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "learning_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "entity_type", "entity_id"})
})
@Getter
@NoArgsConstructor
public class LearningProgress extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private LearningEntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status;
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;
    
    @Column(name = "total_items")
    private Integer totalItems = 0;
    
    @Column(name = "completed_items")
    private Integer completedItems = 0;
    
    @Column(name = "total_reading_time_seconds")
    private Long totalReadingTimeSeconds = 0L;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public LearningProgress(String userId, LearningEntityType entityType, UUID entityId, int totalItems) {
        validateUserId(userId);
        validateEntityType(entityType);
        validateEntityId(entityId);
        
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.totalItems = totalItems;
        this.status = ProgressStatus.NOT_STARTED;
        this.progressPercentage = 0;
        this.completedItems = 0;
        this.totalReadingTimeSeconds = 0L;
    }
    
    public void start() {
        if (this.status == ProgressStatus.NOT_STARTED) {
            this.status = ProgressStatus.IN_PROGRESS;
            this.startedAt = LocalDateTime.now();
        }
    }
    
    public void updateProgress(int completedItems, long readingTimeSeconds) {
        if (completedItems < 0 || completedItems > this.totalItems) {
            throw new IllegalArgumentException("Invalid completed items count");
        }
        
        this.completedItems = completedItems;
        this.totalReadingTimeSeconds += readingTimeSeconds;
        this.progressPercentage = this.totalItems > 0 
                ? (int) ((completedItems * 100.0) / this.totalItems)
                : 0;
        
        if (this.status == ProgressStatus.NOT_STARTED) {
            start();
        }
        
        if (completedItems >= this.totalItems && this.status != ProgressStatus.COMPLETED) {
            complete();
        }
    }
    
    public void complete() {
        this.status = ProgressStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.progressPercentage = 100;
        this.completedItems = this.totalItems;
    }
    
    public boolean isCompleted() {
        return this.status == ProgressStatus.COMPLETED;
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }
    
    private void validateEntityType(LearningEntityType entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Entity type cannot be null");
        }
    }
    
    private void validateEntityId(UUID entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID cannot be null");
        }
    }
}
