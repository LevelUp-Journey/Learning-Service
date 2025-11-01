package com.levelupjourney.learningservice.topics.domain.model.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTopicCommand(
        @NotNull(message = "Topic ID is required")
        UUID topicId,
        
        @Size(max = 100, message = "Topic name must not exceed 100 characters")
        String name
) {
}
