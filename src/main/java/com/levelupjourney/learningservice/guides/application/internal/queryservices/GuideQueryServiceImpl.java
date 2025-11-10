package com.levelupjourney.learningservice.guides.application.internal.queryservices;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetGuideByIdQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesByFiltersQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesQuery;
import com.levelupjourney.learningservice.guides.domain.services.GuideQueryService;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
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
        return guideRepository.findByIdWithDetails(query.guideId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Guide> handle(SearchGuidesQuery query) {
        // Determine filtering based on the query parameters
        EntityStatus status = query.status();
        String userId = query.userId();
        
        // If userId is provided (teacher dashboard), filter by author and ignore status filter
        if (userId != null) {
            // Teacher dashboard: ALL guides belonging to this user (DRAFT and PUBLISHED)
            return guideRepository.findByStatusAndAuthorId(null, userId, query.pageable());
        }
        
        // If status is provided, filter by status only (usually PUBLISHED)
        if (status != null) {
            // Public view: Only guides with specific status (usually PUBLISHED)
            return guideRepository.findByStatusWithDetails(status, query.pageable());
        }
        
        // Fallback: return all guides (should not happen in normal flow)
        return guideRepository.findAllWithDetails(query.pageable());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Guide> handle(SearchGuidesByFiltersQuery query) {
        return guideRepository.searchGuidesByFilters(
                query.title(),
                query.authorIds(),
                query.minLikesCount(),
                query.topicIds(),
                query.pageable()
        );
    }
}
