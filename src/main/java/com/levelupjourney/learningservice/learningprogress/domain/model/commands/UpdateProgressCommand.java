package com.levelupjourney.learningservice.learningprogress.domain.model.commands;

import java.util.UUID;

public record UpdateProgressCommand(
        UUID progressId,
        int completedItems,
        long readingTimeSeconds
) {
}
