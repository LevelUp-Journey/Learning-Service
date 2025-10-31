package com.levelupjourney.learningservice.learningprogress.domain.model.queries;

import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;

import java.util.UUID;

public record GetProgressQuery(
        String userId,
        LearningEntityType entityType,
        UUID entityId
) {
}
