package com.levelupjourney.learningservice.topics.domain.model.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTopicCommand(
        @NotBlank(message = "Topic name is required")
        @Size(max = 100, message = "Topic name must not exceed 100 characters")
        String name
) {
}
