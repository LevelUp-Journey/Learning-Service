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
    
    List<Page> findByGuideIdOrderByOrderNumberAsc(UUID guideId);
    
    Optional<Page> findByGuideIdAndOrderNumber(UUID guideId, Integer orderNumber);
    
    @Query("SELECT p FROM Page p WHERE p.guide.id = :guideId ORDER BY p.orderNumber ASC")
    List<Page> findPagesByGuideId(UUID guideId);
    
    boolean existsByGuideIdAndOrderNumber(UUID guideId, Integer orderNumber);
}
