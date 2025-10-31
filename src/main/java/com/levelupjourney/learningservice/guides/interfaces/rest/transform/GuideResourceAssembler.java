package com.levelupjourney.learningservice.guides.interfaces.rest.transform;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.commands.CreateGuideCommand;
import com.levelupjourney.learningservice.guides.domain.model.commands.UpdateGuideCommand;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.CreateGuideResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.GuideResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.TopicSummaryResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.UpdateGuideResource;

import java.util.UUID;
import java.util.stream.Collectors;

public class GuideResourceAssembler {

    public static CreateGuideCommand toCommandFromResource(CreateGuideResource resource) {
        return new CreateGuideCommand(
                resource.title(),
                resource.description(),
                resource.coverImage(),
                resource.authorIds(),
                resource.topicIds()
        );
    }

    public static UpdateGuideCommand toCommandFromResource(UUID guideId, UpdateGuideResource resource) {
        return new UpdateGuideCommand(
                guideId,
                resource.title(),
                resource.description(),
                resource.coverImage(),
                resource.topicIds()
        );
    }

    public static GuideResource toResourceFromEntity(Guide entity, boolean likedByRequester, boolean includePages) {
        return new GuideResource(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCoverImage(),
                entity.getStatus().name(),
                entity.getLikesCount(),
                likedByRequester,
                entity.getPagesCount(),
                entity.getAuthorIds(),
                entity.getTopics().stream()
                        .map(topic -> new TopicSummaryResource(topic.getId(), topic.getName()))
                        .collect(Collectors.toList()),
                includePages ? entity.getPages().stream()
                        .map(PageResourceAssembler::toResourceFromEntity)
                        .collect(Collectors.toList()) : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
