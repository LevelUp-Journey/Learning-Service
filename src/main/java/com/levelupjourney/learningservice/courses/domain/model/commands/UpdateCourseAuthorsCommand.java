package com.levelupjourney.learningservice.courses.domain.model.commands;

import java.util.Set;
import java.util.UUID;

public record UpdateCourseAuthorsCommand(
        UUID courseId,
        Set<String> authorIds
) {
}
