package com.levelupjourney.learningservice.courses.interfaces.rest.resources;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCourseStatusResource(
        @NotNull(message = "Status is required")
        EntityStatus status
) {
}
