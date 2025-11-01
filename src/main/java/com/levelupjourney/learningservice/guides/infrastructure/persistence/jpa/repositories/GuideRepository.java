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
    
    @Query("SELECT COUNT(g) > 0 FROM Guide g JOIN g.authors a WHERE g.id = :id AND a.authorId = :userId")
    boolean existsByIdAndAuthorIdsContaining(@Param("id") UUID id, @Param("userId") String userId);
    
    @Query("SELECT DISTINCT g FROM Guide g LEFT JOIN FETCH g.authors LEFT JOIN FETCH g.topics WHERE " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:title IS NULL OR LOWER(CAST(g.title AS string)) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) AND " +
           "(:authorIds IS NULL OR EXISTS (SELECT 1 FROM g.authors a WHERE a.authorId IN :authorIds)) AND " +
           "(:topicIds IS NULL OR EXISTS (SELECT t FROM g.topics t WHERE t.id IN :topicIds)) AND " +
           "(:userId IS NULL OR g.status = 'PUBLISHED' OR EXISTS (SELECT 1 FROM g.authors a WHERE a.authorId = :userId))")
    Page<Guide> searchGuides(
            @Param("title") String title,
            @Param("authorIds") Set<String> authorIds,
            @Param("topicIds") Set<UUID> topicIds,
            @Param("status") EntityStatus status,
            @Param("userId") String userId,
            Pageable pageable
    );
    
    Optional<Guide> findByCourseId(UUID courseId);
}
