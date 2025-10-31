package com.levelupjourney.learningservice.courses.domain.model.commands;

import java.util.Set;
import java.util.UUID;

public record UpdateCourseCommand(
        UUID courseId,
        String title,
        String description,
        String coverImage,
        Set<UUID> topicIds
) {
}
