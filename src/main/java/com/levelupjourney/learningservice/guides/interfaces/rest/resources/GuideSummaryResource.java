package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record GuideSummaryResource(
        UUID id,
        String title,
        String description,
        String coverImage,
        EntityStatus status,
        Integer likesCount,
        Integer pagesCount,
        Set<String> authorIds,
        LocalDateTime createdAt
) {
}
