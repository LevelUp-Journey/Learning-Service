package com.levelupjourney.learningservice.guides.domain.model.aggregates;

import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Table(name = "guides")
@Getter
@NoArgsConstructor
public class Guide extends AuditableModel {
    
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
    
    @Column(name = "pages_count")
    private Integer pagesCount = 0;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "guide_authors",
        joinColumns = @JoinColumn(name = "guide_id")
    )
    @Column(name = "author_id", nullable = false)
    private Set<String> authorIds = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "guide_topics",
            joinColumns = @JoinColumn(name = "guide_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<Topic> topics = new HashSet<>();
    
    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderNumber ASC")
    private List<Page> pages = new ArrayList<>();
    
    @Column(name = "course_id")
    private UUID courseId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "guide_challenges",
        joinColumns = @JoinColumn(name = "guide_id")
    )
    @Column(name = "challenge_id", nullable = false)
    private Set<UUID> relatedChallenges = new HashSet<>();
    
    // Constructor
    public Guide(String title, String description, String coverImage, Set<String> authorIds, Set<Topic> topics) {
        validateTitle(title);
        validateAuthorIds(authorIds);
        
        this.title = title;
        this.description = description;
        this.coverImage = coverImage;
        this.authorIds = authorIds != null ? new HashSet<>(authorIds) : new HashSet<>();
        this.topics = topics != null ? new HashSet<>(topics) : new HashSet<>();
        this.status = EntityStatus.DRAFT;
        this.likesCount = 0;
        this.pagesCount = 0;
    }
    
    // Business methods - Basic info
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
    
    // Author management
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
            throw new IllegalArgumentException("Guide must have at least one author");
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
    
    // Topic management
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
    
    // Page management
    public void addPage(Page page) {
        if (page == null) {
            throw new IllegalArgumentException("Page cannot be null");
        }
        page.setGuide(this);
        this.pages.add(page);
        this.pagesCount = this.pages.size();
    }
    
    public void removePage(Page page) {
        this.pages.remove(page);
        this.pagesCount = this.pages.size();
        // Reorder remaining pages
        reorderPages();
    }
    
    public void updatePage(UUID pageId, String content) {
        Page page = this.pages.stream()
                .filter(p -> p.getId().equals(pageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Page not found"));
        page.updateContent(content);
    }
    
    public void reorderPages() {
        for (int i = 0; i < this.pages.size(); i++) {
            this.pages.get(i).updateOrder(i + 1);
        }
    }
    
    // Likes management
    public void incrementLikes() {
        this.likesCount++;
    }
    
    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }
    
    // Course association
    public void associateWithCourse(UUID courseId) {
        this.courseId = courseId;
        this.status = EntityStatus.ASSOCIATED_WITH_COURSE;
    }
    
    public void disassociateFromCourse() {
        this.courseId = null;
        this.status = EntityStatus.PUBLISHED;
    }
    
    // Challenge management
    public void addChallenge(UUID challengeId) {
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge ID cannot be null");
        }
        this.relatedChallenges.add(challengeId);
    }
    
    public void removeChallenge(UUID challengeId) {
        if (challengeId == null) {
            throw new IllegalArgumentException("Challenge ID cannot be null");
        }
        this.relatedChallenges.remove(challengeId);
    }
    
    public boolean hasChallenge(UUID challengeId) {
        return this.relatedChallenges.contains(challengeId);
    }
    
    // Query methods
    public boolean isAuthor(String userId) {
        return this.authorIds.contains(userId);
    }
    
    // Validation
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Guide title cannot be null or empty");
        }
    }
    
    private void validateAuthorIds(Set<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new IllegalArgumentException("Guide must have at least one author");
        }
    }
}
