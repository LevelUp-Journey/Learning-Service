package com.levelupjourney.learningservice.topics.domain.services;

import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.domain.model.commands.CreateTopicCommand;
import com.levelupjourney.learningservice.topics.domain.model.commands.DeleteTopicCommand;
import com.levelupjourney.learningservice.topics.domain.model.commands.UpdateTopicCommand;

import java.util.Optional;

public interface TopicCommandService {
    Optional<Topic> handle(CreateTopicCommand command);
    Optional<Topic> handle(UpdateTopicCommand command);
    void handle(DeleteTopicCommand command);
}
