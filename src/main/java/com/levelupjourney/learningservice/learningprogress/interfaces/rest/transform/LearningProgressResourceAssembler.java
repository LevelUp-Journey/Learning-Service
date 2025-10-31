package com.levelupjourney.learningservice.learningprogress.interfaces.rest.transform;

import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.CompleteProgressCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.StartLearningCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.UpdateProgressCommand;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.LearningProgressResource;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.StartLearningResource;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.UpdateProgressResource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LearningProgressResourceAssembler {
    
    public LearningProgressResource toResourceFromEntity(LearningProgress progress) {
        return new LearningProgressResource(
                progress.getId(),
                progress.getUserId(),
                progress.getEntityType(),
                progress.getEntityId(),
                progress.getStatus(),
                progress.getProgressPercentage(),
                progress.getTotalItems(),
                progress.getCompletedItems(),
                progress.getTotalReadingTimeSeconds(),
                progress.getStartedAt(),
                progress.getCompletedAt(),
                progress.getCreatedAt(),
                progress.getUpdatedAt()
        );
    }
    
    public StartLearningCommand toCommandFromResource(StartLearningResource resource) {
        return new StartLearningCommand(
                resource.userId(),
                resource.entityType(),
                resource.entityId()
        );
    }
    
    public UpdateProgressCommand toCommandFromResource(UUID progressId, UpdateProgressResource resource) {
        return new UpdateProgressCommand(
                progressId,
                resource.completedItems(),
                resource.readingTimeSeconds()
        );
    }
    
    public CompleteProgressCommand toCompleteCommand(UUID progressId) {
        return new CompleteProgressCommand(progressId);
    }
}
