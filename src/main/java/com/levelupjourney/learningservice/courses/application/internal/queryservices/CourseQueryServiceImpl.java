package com.levelupjourney.learningservice.courses.application.internal.queryservices;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.domain.model.queries.GetCourseByIdQuery;
import com.levelupjourney.learningservice.courses.domain.model.queries.SearchCoursesQuery;
import com.levelupjourney.learningservice.courses.domain.services.CourseQueryService;
import com.levelupjourney.learningservice.courses.infrastructure.persistence.jpa.repositories.CourseRepository;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {
    
    private final CourseRepository courseRepository;
    private final SecurityContextHelper securityHelper;
    
    public CourseQueryServiceImpl(CourseRepository courseRepository, SecurityContextHelper securityHelper) {
        this.courseRepository = courseRepository;
        this.securityHelper = securityHelper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Course> handle(SearchCoursesQuery query) {
        // If searching for non-published, require authentication
        if (query.status() != null && query.status() != EntityStatus.PUBLISHED) {
            securityHelper.requireAuthentication();
        }
        
        return courseRepository.searchCourses(
                query.title(),
                query.topicIds(),
                query.authorIds(),
                query.status()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Course> handle(GetCourseByIdQuery query) {
        Optional<Course> courseOpt = courseRepository.findById(query.courseId());
        
        if (courseOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Course course = courseOpt.get();
        
        // Visibility rules: PUBLISHED is public, DRAFT only for authors/admin, DELETED for no one
        if (course.getStatus() == EntityStatus.DELETED) {
            return Optional.empty();
        }
        
        if (course.getStatus() == EntityStatus.DRAFT) {
            securityHelper.requireAuthentication();
            String currentUserId = securityHelper.getCurrentUserId();
            if (!course.isAuthor(currentUserId) && !securityHelper.isAdmin()) {
                return Optional.empty();
            }
        }
        
        return courseOpt;
    }
}
