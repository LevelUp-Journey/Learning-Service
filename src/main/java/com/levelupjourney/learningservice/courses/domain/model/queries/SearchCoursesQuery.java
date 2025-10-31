package com.levelupjourney.learningservice.courses.domain.model.queries;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;

import java.util.List;
import java.util.UUID;

public record SearchCoursesQuery(
        String title,
        List<UUID> topicIds,
        List<String> authorIds,
        EntityStatus status
) {
}
