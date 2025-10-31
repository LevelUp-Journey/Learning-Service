package com.levelupjourney.learningservice.guides.domain.model.aggregates;

import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "pages", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"guide_id", "order_number"})
})
@Getter
@NoArgsConstructor
public class Page extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "order_number", nullable = false)
    private Integer order;
    
    public Page(Guide guide, String content, Integer order) {
        validateGuide(guide);
        validateContent(content);
        validateOrder(order);
        
        this.guide = guide;
        this.content = content;
        this.order = order;
    }
    
    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }
    
    public void updateOrder(Integer order) {
        validateOrder(order);
        this.order = order;
    }
    
    private void validateGuide(Guide guide) {
        if (guide == null) {
            throw new IllegalArgumentException("Guide cannot be null");
        }
    }
    
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Page content cannot be null or empty");
        }
    }
    
    private void validateOrder(Integer order) {
        if (order == null || order < 0) {
            throw new IllegalArgumentException("Page order must be a non-negative number");
        }
    }
}
