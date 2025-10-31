package com.levelupjourney.learningservice.learningprogress.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, UUID> {
    Optional<LearningProgress> findByUserIdAndEntityTypeAndEntityId(
            String userId, 
            LearningEntityType entityType, 
            UUID entityId
    );
    
    List<LearningProgress> findByUserId(String userId);
    
    boolean existsByUserIdAndEntityTypeAndEntityId(
            String userId,
            LearningEntityType entityType,
            UUID entityId
    );
}
