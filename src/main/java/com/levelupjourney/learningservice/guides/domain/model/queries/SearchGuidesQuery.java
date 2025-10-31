package com.levelupjourney.learningservice.guides.domain.model.queries;

import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public record SearchGuidesQuery(
        String title,
        Set<UUID> topicIds,
        Set<String> authorIds,
        EntityStatus status,
        Pageable pageable
) {
}
