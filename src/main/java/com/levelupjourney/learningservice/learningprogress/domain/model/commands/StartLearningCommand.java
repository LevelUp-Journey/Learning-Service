package com.levelupjourney.learningservice.learningprogress.domain.model.commands;

import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;

import java.util.UUID;

public record StartLearningCommand(
        String userId,
        LearningEntityType entityType,
        UUID entityId
) {
}
