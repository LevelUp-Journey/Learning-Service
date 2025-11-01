package com.levelupjourney.learningservice.guides.application.internal.queryservices;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetGuideByIdQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesQuery;
import com.levelupjourney.learningservice.guides.domain.services.GuideQueryService;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuideQueryServiceImpl implements GuideQueryService {

    private final GuideRepository guideRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Guide> handle(GetGuideByIdQuery query) {
        return guideRepository.findById(query.guideId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Guide> handle(SearchGuidesQuery query) {
        return guideRepository.searchGuides(
                query.title(),
                query.authorIds(),
                query.topicIds(),
                query.status(),
                query.userId(),
                query.pageable()
        );
    }
}
