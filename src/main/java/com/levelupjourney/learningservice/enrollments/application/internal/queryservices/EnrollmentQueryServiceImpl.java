package com.levelupjourney.learningservice.enrollments.application.internal.queryservices;

import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetCourseEnrollmentsQuery;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetEnrollmentByUserAndCourseQuery;
import com.levelupjourney.learningservice.enrollments.domain.model.queries.GetUserEnrollmentsQuery;
import com.levelupjourney.learningservice.enrollments.domain.services.EnrollmentQueryService;
import com.levelupjourney.learningservice.enrollments.infrastructure.persistence.jpa.repositories.EnrollmentRepository;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentQueryServiceImpl implements EnrollmentQueryService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityContextHelper securityHelper;
    
    public EnrollmentQueryServiceImpl(
            EnrollmentRepository enrollmentRepository,
            SecurityContextHelper securityHelper) {
        this.enrollmentRepository = enrollmentRepository;
        this.securityHelper = securityHelper;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Enrollment> handle(GetEnrollmentByUserAndCourseQuery query) {
        // Users can only check their own enrollments unless admin
        String currentUserId = securityHelper.getCurrentUserId();
        if (!query.userId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You can only view your own enrollments");
        }
        
        return enrollmentRepository.findByUserIdAndCourseId(query.userId(), query.courseId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> handle(GetUserEnrollmentsQuery query) {
        // Users can only view their own enrollments unless admin
        String currentUserId = securityHelper.getCurrentUserId();
        if (!query.userId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You can only view your own enrollments");
        }
        
        return enrollmentRepository.findByUserId(query.userId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> handle(GetCourseEnrollmentsQuery query) {
        // Only admin or course authors can view enrollments
        securityHelper.requireAuthentication();
        // Additional authorization could check if user is course author
        
        return enrollmentRepository.findByCourseId(query.courseId());
    }
}
