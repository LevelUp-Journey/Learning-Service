package com.levelupjourney.learningservice.topics.domain.model.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteTopicCommand(
        @NotNull(message = "Topic ID is required")
        UUID topicId
) {
}
