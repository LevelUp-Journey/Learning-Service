package com.levelupjourney.learningservice.guides.domain.model.queries;

import java.util.UUID;

public record GetPagesByGuideIdQuery(UUID guideId) {
    public GetPagesByGuideIdQuery {
        if (guideId == null) {
            throw new IllegalArgumentException("Guide ID cannot be null");
        }
    }
}
