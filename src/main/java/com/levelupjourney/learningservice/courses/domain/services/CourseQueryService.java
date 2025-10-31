package com.levelupjourney.learningservice.courses.domain.services;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.domain.model.queries.GetCourseByIdQuery;
import com.levelupjourney.learningservice.courses.domain.model.queries.SearchCoursesQuery;

import java.util.List;
import java.util.Optional;

public interface CourseQueryService {
    List<Course> handle(SearchCoursesQuery query);
    Optional<Course> handle(GetCourseByIdQuery query);
}
