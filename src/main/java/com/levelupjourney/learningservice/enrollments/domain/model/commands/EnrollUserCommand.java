package com.levelupjourney.learningservice.enrollments.domain.model.commands;

import java.util.UUID;

public record EnrollUserCommand(
        String userId,
        UUID courseId
) {
}
