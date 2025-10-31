package com.levelupjourney.learningservice.learningprogress.domain.model.commands;

import java.util.UUID;

public record CompleteProgressCommand(
        UUID progressId
) {
}
