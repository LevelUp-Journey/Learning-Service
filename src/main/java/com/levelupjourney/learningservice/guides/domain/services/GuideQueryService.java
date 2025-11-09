package com.levelupjourney.learningservice.guides.domain.services;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetGuideByIdQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesByFiltersQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesQuery;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface GuideQueryService {
    Optional<Guide> handle(GetGuideByIdQuery query);
    Page<Guide> handle(SearchGuidesQuery query);
    Page<Guide> handle(SearchGuidesByFiltersQuery query);
}
