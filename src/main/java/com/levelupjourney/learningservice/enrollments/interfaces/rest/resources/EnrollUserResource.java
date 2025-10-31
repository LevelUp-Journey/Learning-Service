package com.levelupjourney.learningservice.enrollments.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollUserResource(
        @NotBlank(message = "User ID is required")
        String userId,
        
        @NotNull(message = "Course ID is required")
        UUID courseId
) {
}
