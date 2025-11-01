package com.levelupjourney.learningservice.topics.domain.model.aggregates;

import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "topics")
@Getter
@NoArgsConstructor
public class Topic extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    public Topic(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        this.name = name.trim();
    }
    
    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        this.name = name.trim();
    }
}
