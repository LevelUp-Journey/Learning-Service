package com.levelupjourney.learningservice.courses.domain.model.commands;

import java.util.UUID;

public record DeleteCourseCommand(
        UUID courseId
) {
}
