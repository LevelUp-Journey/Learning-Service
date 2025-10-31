package com.levelupjourney.learningservice.courses.interfaces.rest.resources;

import com.levelupjourney.learningservice.courses.domain.model.valueobjects.DifficultyLevel;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.GuideSummaryResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.TopicSummaryResource;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CourseResource(
        UUID id,
        String title,
        String description,
        String coverImage,
        EntityStatus status,
        DifficultyLevel difficultyLevel,
        Integer likesCount,
        Set<String> authorIds,
        Set<TopicSummaryResource> topics,
        List<GuideSummaryResource> guides,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
