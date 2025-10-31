package com.levelupjourney.learningservice.courses.domain.model.commands;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;

import java.util.UUID;

public record UpdateCourseStatusCommand(
        UUID courseId,
        EntityStatus status
) {
}
