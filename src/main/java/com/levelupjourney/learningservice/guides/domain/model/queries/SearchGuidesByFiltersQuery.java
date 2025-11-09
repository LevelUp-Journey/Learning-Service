package com.levelupjourney.learningservice.guides.domain.model.queries;

import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

/**
 * Query for searching guides with multiple optional filters.
 * All parameters are optional - at least one should be provided for effective filtering.
 */
public record SearchGuidesByFiltersQuery(
        String title,
        Set<String> authorIds,
        Integer minLikesCount,
        Set<UUID> topicIds,
        Pageable pageable
) {
    /**
     * Validates that at least one search parameter is provided
     */
    public boolean hasSearchCriteria() {
        return (title != null && !title.isBlank()) ||
               (authorIds != null && !authorIds.isEmpty()) ||
               minLikesCount != null ||
               (topicIds != null && !topicIds.isEmpty());
    }
}
