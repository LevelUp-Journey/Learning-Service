package com.levelupjourney.learningservice.guides.domain.model.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteGuideCommand(
        @NotNull(message = "Guide ID is required")
        UUID guideId
) {
}
