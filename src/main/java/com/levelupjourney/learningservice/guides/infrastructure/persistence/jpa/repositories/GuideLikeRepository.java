package com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.guides.domain.model.entities.GuideLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GuideLikeRepository extends JpaRepository<GuideLike, UUID> {
    
    boolean existsByGuideIdAndUserId(UUID guideId, String userId);
    
    Optional<GuideLike> findByGuideIdAndUserId(UUID guideId, String userId);
    
    void deleteByGuideIdAndUserId(UUID guideId, String userId);
    
    long countByGuideId(UUID guideId);
    
    @Query("SELECT gl.guide.id FROM GuideLike gl WHERE gl.guide.id IN :guideIds AND gl.userId = :userId")
    Set<UUID> findGuideIdsLikedByUser(@Param("guideIds") Set<UUID> guideIds, @Param("userId") String userId);
}
