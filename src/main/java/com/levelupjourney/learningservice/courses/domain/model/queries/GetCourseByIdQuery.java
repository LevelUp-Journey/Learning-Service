package com.levelupjourney.learningservice.courses.domain.model.queries;

import java.util.UUID;

public record GetCourseByIdQuery(
        UUID courseId
) {
}
