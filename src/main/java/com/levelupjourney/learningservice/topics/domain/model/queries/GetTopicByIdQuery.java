package com.levelupjourney.learningservice.topics.domain.model.queries;

import java.util.UUID;

public record GetTopicByIdQuery(UUID topicId) {
    public GetTopicByIdQuery {
        if (topicId == null) {
            throw new IllegalArgumentException("Topic ID cannot be null");
        }
    }
}
