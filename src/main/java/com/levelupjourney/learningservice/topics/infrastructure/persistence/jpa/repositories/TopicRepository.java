package com.levelupjourney.learningservice.topics.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    Optional<Topic> findByName(String name);
    boolean existsByName(String name);
}
