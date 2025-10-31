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
    
    boolean existsByIdAndAuthorIdsContaining(UUID id, String userId);
    
    @Query("SELECT g FROM Guide g WHERE " +
           "(:status IS NULL OR g.status = :status) AND " +
           "(:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:authorIds IS NULL OR EXISTS (SELECT 1 FROM g.authorIds a WHERE a IN :authorIds)) AND " +
           "(:topicIds IS NULL OR EXISTS (SELECT t FROM g.topics t WHERE t.id IN :topicIds))")
    Page<Guide> searchGuides(
            @Param("title") String title,
            @Param("authorIds") Set<String> authorIds,
            @Param("topicIds") Set<UUID> topicIds,
            @Param("status") EntityStatus status,
            Pageable pageable
    );
    
    Optional<Guide> findByCourseId(UUID courseId);
}
