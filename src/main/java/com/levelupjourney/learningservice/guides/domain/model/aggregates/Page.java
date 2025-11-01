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
    private Integer orderNumber;
    
    public Page(Guide guide, String content, Integer orderNumber) {
        validateGuide(guide);
        validateContent(content);
        validateOrder(orderNumber);
        
        this.guide = guide;
        this.content = content;
        this.orderNumber = orderNumber;
    }
    
    public Page(String content, Integer orderNumber) {
        validateContent(content);
        validateOrder(orderNumber);
        
        this.content = content;
        this.orderNumber = orderNumber;
    }
    
    public void setGuide(Guide guide) {
        validateGuide(guide);
        this.guide = guide;
    }
    
    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }
    
    public void updateOrder(Integer orderNumber) {
        validateOrder(orderNumber);
        this.orderNumber = orderNumber;
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
    
    private void validateOrder(Integer orderNumber) {
        if (orderNumber == null || orderNumber < 1) {
            throw new IllegalArgumentException("Page order must be a positive number");
        }
    }
}
