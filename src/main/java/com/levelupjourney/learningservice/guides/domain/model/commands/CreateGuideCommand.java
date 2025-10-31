package com.levelupjourney.learningservice.guides.domain.model.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateGuideCommand(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        
        String coverImage,
        
        Set<String> authorIds,
        
        @NotNull(message = "At least one topic must be specified")
        Set<UUID> topicIds
) {
}
