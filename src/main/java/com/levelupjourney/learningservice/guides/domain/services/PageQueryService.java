package com.levelupjourney.learningservice.guides.domain.services;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetPagesByGuideIdQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PageQueryService {
    Optional<Page> handle(UUID pageId);
    List<Page> handle(GetPagesByGuideIdQuery query);
}
