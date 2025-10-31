package com.levelupjourney.learningservice.courses.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateCourseResource(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        
        String coverImage,
        
        @NotEmpty(message = "At least one author is required")
        Set<String> authorIds,
        
        Set<UUID> topicIds
) {
}
