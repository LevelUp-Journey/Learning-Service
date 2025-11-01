package com.levelupjourney.learningservice.topics.application.internal.commandservices;

import com.levelupjourney.learningservice.shared.infrastructure.exception.DuplicateResourceException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.domain.model.commands.CreateTopicCommand;
import com.levelupjourney.learningservice.topics.domain.model.commands.DeleteTopicCommand;
import com.levelupjourney.learningservice.topics.domain.model.commands.UpdateTopicCommand;
import com.levelupjourney.learningservice.topics.domain.services.TopicCommandService;
import com.levelupjourney.learningservice.topics.infrastructure.persistence.jpa.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TopicCommandServiceImpl implements TopicCommandService {

    private final TopicRepository topicRepository;

    @Override
    @Transactional
    public Optional<Topic> handle(CreateTopicCommand command) {
        if (topicRepository.existsByName(command.name())) {
            throw new DuplicateResourceException("Topic with name '" + command.name() + "' already exists");
        }

        var topic = new Topic(command.name());
        var savedTopic = topicRepository.save(topic);
        return Optional.of(savedTopic);
    }

    @Override
    @Transactional
    public Optional<Topic> handle(UpdateTopicCommand command) {
        var topic = topicRepository.findById(command.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + command.topicId()));

        if (command.name() != null && !command.name().isBlank()) {
            if (!topic.getName().equals(command.name()) && topicRepository.existsByName(command.name())) {
                throw new DuplicateResourceException("Topic with name '" + command.name() + "' already exists");
            }
            topic.updateName(command.name());
        }

        var updatedTopic = topicRepository.save(topic);
        return Optional.of(updatedTopic);
    }

    @Override
    @Transactional
    public void handle(DeleteTopicCommand command) {
        if (!topicRepository.existsById(command.topicId())) {
            throw new ResourceNotFoundException("Topic not found with id: " + command.topicId());
        }
        topicRepository.deleteById(command.topicId());
    }
}
