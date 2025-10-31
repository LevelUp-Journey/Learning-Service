package com.levelupjourney.learningservice.enrollments.application.internal.commandservices;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.infrastructure.persistence.jpa.repositories.CourseRepository;
import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.CancelEnrollmentCommand;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.EnrollUserCommand;
import com.levelupjourney.learningservice.enrollments.domain.model.valueobjects.EnrollmentStatus;
import com.levelupjourney.learningservice.enrollments.domain.services.EnrollmentCommandService;
import com.levelupjourney.learningservice.enrollments.infrastructure.persistence.jpa.repositories.EnrollmentRepository;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ConflictException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentCommandServiceImpl implements EnrollmentCommandService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SecurityContextHelper securityHelper;
    
    public EnrollmentCommandServiceImpl(
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            SecurityContextHelper securityHelper) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.securityHelper = securityHelper;
    }
    
    @Override
    @Transactional
    public Enrollment handle(EnrollUserCommand command) {
        // Users can only enroll themselves unless admin
        String currentUserId = securityHelper.getCurrentUserId();
        if (!command.userId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You can only enroll yourself");
        }
        
        // Check if course exists
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
                command.userId(), command.courseId(), EnrollmentStatus.ACTIVE)) {
            throw new ConflictException("User is already enrolled in this course");
        }
        
        // Create enrollment
        Enrollment enrollment = new Enrollment(command.userId(), course);
        return enrollmentRepository.save(enrollment);
    }
    
    @Override
    @Transactional
    public Enrollment handle(CancelEnrollmentCommand command) {
        Enrollment enrollment = enrollmentRepository.findById(command.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        // Users can only cancel their own enrollments unless admin
        String currentUserId = securityHelper.getCurrentUserId();
        if (!enrollment.getUserId().equals(currentUserId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You can only cancel your own enrollment");
        }
        
        enrollment.cancel();
        return enrollmentRepository.save(enrollment);
    }
}
