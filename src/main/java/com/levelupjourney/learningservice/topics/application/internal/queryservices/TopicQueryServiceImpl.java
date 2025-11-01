package com.levelupjourney.learningservice.topics.application.internal.queryservices;

import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetAllTopicsQuery;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetTopicByIdQuery;
import com.levelupjourney.learningservice.topics.domain.model.queries.GetTopicByNameQuery;
import com.levelupjourney.learningservice.topics.domain.services.TopicQueryService;
import com.levelupjourney.learningservice.topics.infrastructure.persistence.jpa.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TopicQueryServiceImpl implements TopicQueryService {

    private final TopicRepository topicRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Topic> handle(GetTopicByIdQuery query) {
        return topicRepository.findById(query.topicId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Topic> handle(GetTopicByNameQuery query) {
        return topicRepository.findByName(query.name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> handle(GetAllTopicsQuery query) {
        return topicRepository.findAll();
    }
}
