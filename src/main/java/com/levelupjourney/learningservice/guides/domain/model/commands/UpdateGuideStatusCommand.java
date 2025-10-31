package com.levelupjourney.learningservice.guides.domain.model.commands;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateGuideStatusCommand(
        @NotNull(message = "Guide ID is required")
        UUID guideId,
        
        @NotNull(message = "Status is required")
        EntityStatus status
) {
}
