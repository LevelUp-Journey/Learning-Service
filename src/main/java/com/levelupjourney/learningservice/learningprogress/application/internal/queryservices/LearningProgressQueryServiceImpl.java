package com.levelupjourney.learningservice.learningprogress.application.internal.queryservices;

import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.queries.GetProgressQuery;
import com.levelupjourney.learningservice.learningprogress.domain.model.queries.GetUserProgressQuery;
import com.levelupjourney.learningservice.learningprogress.domain.services.LearningProgressQueryService;
import com.levelupjourney.learningservice.learningprogress.infrastructure.persistence.jpa.repositories.LearningProgressRepository;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LearningProgressQueryServiceImpl implements LearningProgressQueryService {
    
    private final LearningProgressRepository progressRepository;
    private final SecurityContextHelper securityHelper;
    
    public LearningProgressQueryServiceImpl(
            LearningProgressRepository progressRepository,
            SecurityContextHelper securityHelper) {
        this.progressRepository = progressRepository;
        this.securityHelper = securityHelper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<LearningProgress> handle(GetProgressQuery query) {
        // Users can only view their own progress unless admin
        String currentUserId = securityHelper.getCurrentUserId();
        if (!query.userId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You can only view your own learning progress");
        }
        
        return progressRepository.findByUserIdAndEntityTypeAndEntityId(
                query.userId(),
                query.entityType(),
                query.entityId()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LearningProgress> handle(GetUserProgressQuery query) {
        // Users can only view their own progress unless admin
        String currentUserId = securityHelper.getCurrentUserId();
        if (!query.userId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You can only view your own learning progress");
        }
        
        return progressRepository.findByUserId(query.userId());
    }
}
