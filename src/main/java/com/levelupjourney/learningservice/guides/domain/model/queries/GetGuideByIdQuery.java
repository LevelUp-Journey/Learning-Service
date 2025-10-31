package com.levelupjourney.learningservice.guides.domain.model.queries;

import java.util.UUID;

public record GetGuideByIdQuery(UUID guideId) {
    public GetGuideByIdQuery {
        if (guideId == null) {
            throw new IllegalArgumentException("Guide ID cannot be null");
        }
    }
}
