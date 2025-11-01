package com.levelupjourney.learningservice.topics.domain.services;

import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetAllTopicsQuery;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetTopicByIdQuery;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetTopicByNameQuery;

import java.util.List;
import java.util.Optional;

public interface TopicQueryService {
    Optional<Topic> handle(GetTopicByIdQuery query);
    Optional<Topic> handle(GetTopicByNameQuery query);
    List<Topic> handle(GetAllTopicsQuery query);
}
