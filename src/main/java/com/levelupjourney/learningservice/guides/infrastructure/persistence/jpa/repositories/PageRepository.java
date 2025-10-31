package com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends JpaRepository<Page, UUID> {
    
    List<Page> findByGuideIdOrderByOrderAsc(UUID guideId);
    
    Optional<Page> findByGuideIdAndOrder(UUID guideId, Integer order);
    
    @Query("SELECT p FROM Page p WHERE p.guide.id = :guideId ORDER BY p.order ASC")
    List<Page> findPagesByGuideId(UUID guideId);
    
    boolean existsByGuideIdAndOrder(UUID guideId, Integer order);
}
