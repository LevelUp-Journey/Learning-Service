package com.levelupjourney.learningservice.enrollments.domain.model.aggregates;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.enrollments.domain.model.valueobjects.EnrollmentStatus;
import com.levelupjourney.learningservice.shared.domain.model.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
@Getter
@NoArgsConstructor
public class Enrollment extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;
    
    public Enrollment(String userId, Course course) {
        validateUserId(userId);
        validateCourse(course);
        
        this.userId = userId;
        this.course = course;
        this.status = EnrollmentStatus.ACTIVE;
    }
    
    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
    }
    
    public void reactivate() {
        this.status = EnrollmentStatus.ACTIVE;
    }
    
    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVE;
    }
    
    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }
    
    private void validateCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null");
        }
    }
}
