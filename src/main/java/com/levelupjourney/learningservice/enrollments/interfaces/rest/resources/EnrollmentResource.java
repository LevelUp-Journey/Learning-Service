package com.levelupjourney.learningservice.enrollments.interfaces.rest.resources;

import com.levelupjourney.learningservice.enrollments.domain.model.valueobjects.EnrollmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentResource(
        UUID id,
        String userId,
        UUID courseId,
        String courseTitle,
        EnrollmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
