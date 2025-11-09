package com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface GuideRepository extends JpaRepository<Guide, UUID> {
    
    Optional<Guide> findByIdAndStatus(UUID id, EntityStatus status);
    
    Page<Guide> findByStatus(EntityStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(g) > 0 FROM Guide g WHERE g.id = :id AND :userId MEMBER OF g.authorIds")
    boolean existsByIdAndAuthorIdsContaining(@Param("id") UUID id, @Param("userId") String userId);
    
    @Query("SELECT DISTINCT g FROM Guide g LEFT JOIN FETCH g.topics LEFT JOIN FETCH g.pages")
    Page<Guide> findAllWithDetails(Pageable pageable);
    
    @Query("SELECT DISTINCT g FROM Guide g LEFT JOIN FETCH g.topics LEFT JOIN FETCH g.pages WHERE g.status = :status")
    Page<Guide> findByStatusWithDetails(@Param("status") EntityStatus status, Pageable pageable);
    
    @Query("SELECT g FROM Guide g LEFT JOIN FETCH g.topics LEFT JOIN FETCH g.pages WHERE g.id = :id")
    Optional<Guide> findByIdWithDetails(@Param("id") UUID id);
    
    Optional<Guide> findByCourseId(UUID courseId);
    
    @Query("""
            SELECT DISTINCT g FROM Guide g
            LEFT JOIN g.topics t
            WHERE g.status = com.levelupjourney.learningservice.shared.domain.model.EntityStatus.PUBLISHED
            AND (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%')))
            AND (:minLikesCount IS NULL OR g.likesCount >= :minLikesCount)
            AND (COALESCE(:authorIds, NULL) IS NULL OR EXISTS (
                SELECT 1 FROM g.authorIds a WHERE a IN :authorIds
            ))
            AND (COALESCE(:topicIds, NULL) IS NULL OR t.id IN :topicIds)
            """)
    Page<Guide> searchGuidesByFilters(
            @Param("title") String title,
            @Param("authorIds") Set<String> authorIds,
            @Param("minLikesCount") Integer minLikesCount,
            @Param("topicIds") Set<UUID> topicIds,
            Pageable pageable
    );
}
