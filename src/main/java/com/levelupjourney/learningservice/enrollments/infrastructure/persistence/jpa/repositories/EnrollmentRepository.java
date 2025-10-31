package com.levelupjourney.learningservice.enrollments.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.valueobjects.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByUserIdAndCourseId(String userId, UUID courseId);
    List<Enrollment> findByUserId(String userId);
    List<Enrollment> findByCourseId(UUID courseId);
    boolean existsByUserIdAndCourseIdAndStatus(String userId, UUID courseId, EnrollmentStatus status);
}
