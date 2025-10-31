package com.levelupjourney.learningservice.courses.interfaces.rest.resources;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateCourseAuthorsResource(
        @NotEmpty(message = "At least one author is required")
        Set<String> authorIds
) {
}
