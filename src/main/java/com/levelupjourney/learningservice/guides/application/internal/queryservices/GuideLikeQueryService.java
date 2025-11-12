package com.levelupjourney.learningservice.guides.application.internal.queryservices;

import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Service to check if a user has liked guides
 */
@Service
@RequiredArgsConstructor
public class GuideLikeQueryService {
    
    private final GuideLikeRepository guideLikeRepository;
    
    /**
     * Check if a user has liked a specific guide
     */
    public boolean hasUserLikedGuide(UUID guideId, String userId) {
        if (userId == null) {
            return false;
        }
        return guideLikeRepository.existsByGuideIdAndUserId(guideId, userId);
    }
    
    /**
     * Get all guide IDs that a user has liked from a given set
     */
    public Set<UUID> getGuidesLikedByUser(Set<UUID> guideIds, String userId) {
        if (userId == null || guideIds.isEmpty()) {
            return Set.of();
        }
        return guideLikeRepository.findGuideIdsLikedByUser(guideIds, userId);
    }
}
