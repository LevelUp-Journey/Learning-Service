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
import java.util.UUID;

@Repository
public interface GuideRepository extends JpaRepository<Guide, UUID> {
    
    Optional<Guide> findByIdAndStatus(UUID id, EntityStatus status);
    
    Page<Guide> findByStatus(EntityStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(g) > 0 FROM Guide g JOIN g.authors a WHERE g.id = :id AND a.authorId = :userId")
    boolean existsByIdAndAuthorIdsContaining(@Param("id") UUID id, @Param("userId") String userId);
    
    @Query("SELECT DISTINCT g FROM Guide g LEFT JOIN FETCH g.authors LEFT JOIN FETCH g.topics LEFT JOIN FETCH g.pages")
    Page<Guide> findAllWithAuthorsAndTopics(Pageable pageable);
    
    @Query("SELECT DISTINCT g FROM Guide g LEFT JOIN FETCH g.authors LEFT JOIN FETCH g.topics LEFT JOIN FETCH g.pages WHERE g.status = :status")
    Page<Guide> findByStatusWithAuthorsAndTopics(@Param("status") EntityStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT g FROM Guide g LEFT JOIN FETCH g.authors LEFT JOIN FETCH g.topics LEFT JOIN FETCH g.pages WHERE g.id = :id")
    Optional<Guide> findByIdWithAuthorsAndTopics(@Param("id") UUID id);
    
    Optional<Guide> findByCourseId(UUID courseId);
}
