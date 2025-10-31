package com.levelupjourney.learningservice.courses.domain.model.aggregates;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor
public class Course extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "cover_image")
    private String coverImage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status = EntityStatus.DRAFT;
    
    @Column(name = "likes_count")
    private Integer likesCount = 0;
    
    @ElementCollection
    @CollectionTable(name = "course_authors", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "author_id")
    private Set<String> authorIds = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
            name = "course_topics",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<Topic> topics = new HashSet<>();
    
    @OneToMany
    @JoinTable(
            name = "course_guides",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "guide_id")
    )
    private List<Guide> guides = new ArrayList<>();
    
    public Course(String title, String description, String coverImage, Set<String> authorIds, Set<Topic> topics) {
        validateTitle(title);
        validateAuthorIds(authorIds);
        
        this.title = title;
        this.description = description;
        this.coverImage = coverImage;
        this.authorIds = authorIds != null ? new HashSet<>(authorIds) : new HashSet<>();
        this.topics = topics != null ? new HashSet<>(topics) : new HashSet<>();
        this.status = EntityStatus.DRAFT;
    }
    
    public void updateTitle(String title) {
        validateTitle(title);
        this.title = title;
    }
    
    public void updateDescription(String description) {
        this.description = description;
    }
    
    public void updateCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public void updateStatus(EntityStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }
    
    public void addAuthor(String authorId, int maxAuthors) {
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("Author ID cannot be null or empty");
        }
        if (this.authorIds.size() >= maxAuthors) {
            throw new IllegalArgumentException("Maximum number of authors (" + maxAuthors + ") reached");
        }
        this.authorIds.add(authorId);
    }
    
    public void removeAuthor(String authorId) {
        if (this.authorIds.size() <= 1) {
            throw new IllegalArgumentException("Course must have at least one author");
        }
        this.authorIds.remove(authorId);
    }
    
    public void setAuthors(Set<String> authorIds, int maxAuthors) {
        validateAuthorIds(authorIds);
        if (authorIds.size() > maxAuthors) {
            throw new IllegalArgumentException("Maximum number of authors (" + maxAuthors + ") exceeded");
        }
        this.authorIds = new HashSet<>(authorIds);
    }
    
    public void addTopic(Topic topic) {
        if (topic == null) {
            throw new IllegalArgumentException("Topic cannot be null");
        }
        this.topics.add(topic);
    }
    
    public void removeTopic(Topic topic) {
        this.topics.remove(topic);
    }
    
    public void setTopics(Set<Topic> topics) {
        this.topics = topics != null ? new HashSet<>(topics) : new HashSet<>();
    }
    
    public void addGuide(Guide guide) {
        if (guide == null) {
            throw new IllegalArgumentException("Guide cannot be null");
        }
        if (!this.guides.contains(guide)) {
            this.guides.add(guide);
            guide.associateWithCourse(this.id);
        }
    }
    
    public void removeGuide(Guide guide) {
        if (this.guides.remove(guide)) {
            guide.disassociateFromCourse();
        }
    }
    
    public void incrementLikes() {
        this.likesCount++;
    }
    
    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }
    
    public boolean isAuthor(String userId) {
        return this.authorIds.contains(userId);
    }
    
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title cannot be null or empty");
        }
    }
    
    private void validateAuthorIds(Set<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one author");
        }
    }
}
