package com.levelupjourney.learningservice.topics.interfaces.rest.transform;

import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.domain.model.commands.CreateTopicCommand;
import com.levelupjourney.learningservice.topics.domain.model.commands.UpdateTopicCommand;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.CreateTopicResource;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.TopicResource;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.UpdateTopicResource;

import java.util.UUID;

public class TopicResourceAssembler {

    public static CreateTopicCommand toCommandFromResource(CreateTopicResource resource) {
        return new CreateTopicCommand(resource.name());
    }

    public static UpdateTopicCommand toCommandFromResource(UUID topicId, UpdateTopicResource resource) {
        return new UpdateTopicCommand(topicId, resource.name());
    }

    public static TopicResource toResourceFromEntity(Topic entity) {
        return new TopicResource(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
