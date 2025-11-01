package com.levelupjourney.learningservice.topics.domain.model.queries;

public record GetTopicByNameQuery(String name) {
    public GetTopicByNameQuery {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
    }
}
