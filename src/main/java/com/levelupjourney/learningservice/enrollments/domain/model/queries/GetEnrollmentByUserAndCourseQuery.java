package com.levelupjourney.learningservice.enrollments.domain.model.queries;

import java.util.UUID;

public record GetEnrollmentByUserAndCourseQuery(
        String userId,
        UUID courseId
) {
}
