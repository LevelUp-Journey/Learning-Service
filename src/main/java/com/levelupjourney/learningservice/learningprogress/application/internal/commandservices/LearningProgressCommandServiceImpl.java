package com.levelupjourney.learningservice.learningprogress.application.internal.commandservices;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.infrastructure.persistence.jpa.repositories.CourseRepository;
import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.CompleteProgressCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.StartLearningCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.UpdateProgressCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import com.levelupjourney.learningservice.learningprogress.domain.services.LearningProgressCommandService;
import com.levelupjourney.learningservice.learningprogress.infrastructure.persistence.jpa.repositories.LearningProgressRepository;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ConflictException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LearningProgressCommandServiceImpl implements LearningProgressCommandService {
    
    private final LearningProgressRepository progressRepository;
    private final GuideRepository guideRepository;
    private final CourseRepository courseRepository;
    private final SecurityContextHelper securityHelper;
    
    public LearningProgressCommandServiceImpl(
            LearningProgressRepository progressRepository,
            GuideRepository guideRepository,
            CourseRepository courseRepository,
            SecurityContextHelper securityHelper) {
        this.progressRepository = progressRepository;
        this.guideRepository = guideRepository;
        this.courseRepository = courseRepository;
        this.securityHelper = securityHelper;
    }
    
    @Override
    @Transactional
    public LearningProgress handle(StartLearningCommand command) {
        // Users can only start learning for themselves
        String currentUserId = securityHelper.getCurrentUserId();
        if (!command.userId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only start learning for yourself");
        }
        
        // Check if already exists
        if (progressRepository.existsByUserIdAndEntityTypeAndEntityId(
                command.userId(), command.entityType(), command.entityId())) {
            throw new ConflictException("Learning progress already exists for this user and entity");
        }
        
        // Get total items based on entity type
        int totalItems = getTotalItems(command.entityType(), command.entityId());
        
        // Create progress
        LearningProgress progress = new LearningProgress(
                command.userId(),
                command.entityType(),
                command.entityId(),
                totalItems
        );
        progress.start();
        
        return progressRepository.save(progress);
    }
    
    @Override
    @Transactional
    public LearningProgress handle(UpdateProgressCommand command) {
        LearningProgress progress = progressRepository.findById(command.progressId())
                .orElseThrow(() -> new ResourceNotFoundException("Learning progress not found"));
        
        // Users can only update their own progress
        String currentUserId = securityHelper.getCurrentUserId();
        if (!progress.getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only update your own learning progress");
        }
        
        progress.updateProgress(command.completedItems(), command.readingTimeSeconds());
        return progressRepository.save(progress);
    }
    
    @Override
    @Transactional
    public LearningProgress handle(CompleteProgressCommand command) {
        LearningProgress progress = progressRepository.findById(command.progressId())
                .orElseThrow(() -> new ResourceNotFoundException("Learning progress not found"));
        
        // Users can only complete their own progress
        String currentUserId = securityHelper.getCurrentUserId();
        if (!progress.getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only complete your own learning progress");
        }
        
        progress.complete();
        return progressRepository.save(progress);
    }
    
    private int getTotalItems(LearningEntityType entityType, UUID entityId) {
        if (entityType == LearningEntityType.GUIDE) {
            Guide guide = guideRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));
            return guide.getPagesCount();
        } else if (entityType == LearningEntityType.COURSE) {
            Course course = courseRepository.findById(entityId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            return course.getGuides().size();
        }
        throw new IllegalArgumentException("Unknown entity type");
    }
}
