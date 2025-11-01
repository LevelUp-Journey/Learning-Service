package com.levelupjourney.learningservice.guides.interfaces.rest.transform;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import com.levelupjourney.learningservice.guides.domain.model.commands.CreatePageCommand;
import com.levelupjourney.learningservice.guides.domain.model.commands.UpdatePageCommand;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.CreatePageResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.PageResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.UpdatePageResource;

import java.util.UUID;

public class PageResourceAssembler {

    public static CreatePageCommand toCommandFromResource(UUID guideId, CreatePageResource resource) {
        return new CreatePageCommand(
                guideId,
                resource.content(),
                resource.orderNumber()
        );
    }

    public static UpdatePageCommand toCommandFromResource(UUID pageId, UpdatePageResource resource) {
        return new UpdatePageCommand(
                pageId,
                resource.content(),
                resource.orderNumber()
        );
    }

    public static PageResource toResourceFromEntity(Page entity) {
        return new PageResource(
                entity.getId(),
                entity.getContent(),
                entity.getOrderNumber(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
