package com.levelupjourney.learningservice.guides.domain.model.commands;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeletePageCommand(
        @NotNull(message = "Page ID is required")
        UUID pageId
) {
}
