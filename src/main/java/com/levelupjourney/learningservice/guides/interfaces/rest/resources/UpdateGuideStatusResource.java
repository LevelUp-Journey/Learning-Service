package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateGuideStatusResource(
        @NotNull(message = "Status is required")
        EntityStatus status
) {
}
