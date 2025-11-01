package com.levelupjourney.learningservice.guides.domain.model.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePageCommand(
        @NotNull(message = "Guide ID is required")
        UUID guideId,
        
        @NotBlank(message = "Content is required")
        String content,
        
        @NotNull(message = "Order is required")
        @Min(value = 1, message = "Order must be positive (starting from 1)")
        Integer orderNumber
) {
}
