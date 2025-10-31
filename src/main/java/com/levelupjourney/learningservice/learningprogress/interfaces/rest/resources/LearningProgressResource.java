package com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources;

import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.ProgressStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LearningProgressResource(
        UUID id,
        String userId,
        LearningEntityType entityType,
        UUID entityId,
        ProgressStatus status,
        Integer progressPercentage,
        Integer totalItems,
        Integer completedItems,
        Long totalReadingTimeSeconds,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
