package com.levelupjourney.learningservice.courses.application.internal.commandservices;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.domain.model.commands.*;
import com.levelupjourney.learningservice.courses.domain.services.CourseCommandService;
import com.levelupjourney.learningservice.courses.infrastructure.persistence.jpa.repositories.CourseRepository;
import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.infrastructure.persistence.jpa.repositories.TopicRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class CourseCommandServiceImpl implements CourseCommandService {
    
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final GuideRepository guideRepository;
    private final SecurityContextHelper securityHelper;
    
    @Value("${application.max-authors:5}")
    private int maxAuthors;
    
    public CourseCommandServiceImpl(
            CourseRepository courseRepository,
            TopicRepository topicRepository,
            GuideRepository guideRepository,
            SecurityContextHelper securityHelper) {
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.guideRepository = guideRepository;
        this.securityHelper = securityHelper;
    }
    
    @Override
    @Transactional
    public Course handle(CreateCourseCommand command) {
        // Only ADMIN and TEACHER can create courses
        securityHelper.requireAnyRole("ROLE_ADMIN", "ROLE_TEACHER");
        
        // Get current user as one of the authors
        String currentUserId = securityHelper.getCurrentUserId();
        Set<String> authorIds = new HashSet<>(command.authorIds() != null ? command.authorIds() : Set.of());
        authorIds.add(currentUserId);
        
        // Validate topics
        Set<Topic> topics = new HashSet<>();
        if (command.topicIds() != null && !command.topicIds().isEmpty()) {
            topics = new HashSet<>(topicRepository.findAllById(command.topicIds()));
            if (topics.size() != command.topicIds().size()) {
                throw new ResourceNotFoundException("One or more topics not found");
            }
        }
        
        Course course = new Course(
                command.title(),
                command.description(),
                command.coverImage(),
                authorIds,
                topics
        );
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course handle(UpdateCourseCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Only authors or admins can update
        if (!course.isAuthor(securityHelper.getCurrentUserId()) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to update this course");
        }
        
        if (command.title() != null) {
            course.updateTitle(command.title());
        }
        
        if (command.description() != null) {
            course.updateDescription(command.description());
        }
        
        if (command.coverImage() != null) {
            course.updateCoverImage(command.coverImage());
        }
        
        // Update topics if provided
        if (command.topicIds() != null) {
            Set<Topic> topics = new HashSet<>(topicRepository.findAllById(command.topicIds()));
            if (topics.size() != command.topicIds().size()) {
                throw new ResourceNotFoundException("One or more topics not found");
            }
            course.setTopics(topics);
        }
        
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course handle(UpdateCourseStatusCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Only authors or admins can change status
        if (!course.isAuthor(securityHelper.getCurrentUserId()) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to change course status");
        }
        
        course.updateStatus(command.status());
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course handle(UpdateCourseAuthorsCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Only admins can update authors
        securityHelper.requireRole("ROLE_ADMIN");
        
        course.setAuthors(command.authorIds(), maxAuthors);
        return courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public void handle(DeleteCourseCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Only authors or admins can delete
        if (!course.isAuthor(securityHelper.getCurrentUserId()) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to delete this course");
        }
        
        // Soft delete
        course.updateStatus(EntityStatus.DELETED);
        courseRepository.save(course);
    }
    
    @Override
    @Transactional
    public Course handle(AssociateGuideCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        Guide guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));
        
        // Only authors or admins can associate guides
        if (!course.isAuthor(securityHelper.getCurrentUserId()) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to modify this course");
        }
        
        // Associate guide with course
        course.addGuide(guide);
        courseRepository.save(course);
        
        return course;
    }
    
    @Override
    @Transactional
    public Course handle(DisassociateGuideCommand command) {
        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        Guide guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));
        
        // Only authors or admins can disassociate guides
        if (!course.isAuthor(securityHelper.getCurrentUserId()) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to modify this course");
        }
        
        // Disassociate guide from course
        course.removeGuide(guide);
        courseRepository.save(course);
        
        return course;
    }
}
