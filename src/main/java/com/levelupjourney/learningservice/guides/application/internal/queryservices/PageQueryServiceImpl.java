package com.levelupjourney.learningservice.guides.application.internal.queryservices;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetPagesByGuideIdQuery;
import com.levelupjourney.learningservice.guides.domain.services.PageQueryService;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageQueryServiceImpl implements PageQueryService {

    private final PageRepository pageRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Page> handle(UUID pageId) {
        return pageRepository.findById(pageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Page> handle(GetPagesByGuideIdQuery query) {
        return pageRepository.findByGuideIdOrderByOrderAsc(query.guideId());
    }
}
