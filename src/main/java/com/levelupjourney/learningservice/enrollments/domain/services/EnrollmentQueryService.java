package com.levelupjourney.learningservice.enrollments.domain.services;

import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetCourseEnrollmentsQuery;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetEnrollmentByUserAndCourseQuery;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetUserEnrollmentsQuery;

import java.util.List;
import java.util.Optional;

public interface EnrollmentQueryService {
    Optional<Enrollment> handle(GetEnrollmentByUserAndCourseQuery query);
    List<Enrollment> handle(GetUserEnrollmentsQuery query);
    List<Enrollment> handle(GetCourseEnrollmentsQuery query);
}
