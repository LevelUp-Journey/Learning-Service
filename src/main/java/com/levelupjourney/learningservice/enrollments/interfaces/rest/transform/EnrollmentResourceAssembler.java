package com.levelupjourney.learningservice.enrollments.interfaces.rest.transform;

import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.EnrollUserCommand;
import com.levelupjourney.learningservice.enrollments.interfaces.rest.resources.EnrollUserResource;
import com.levelupjourney.learningservice.enrollments.interfaces.rest.resources.EnrollmentResource;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentResourceAssembler {
    
    public EnrollmentResource toResourceFromEntity(Enrollment enrollment) {
        return new EnrollmentResource(
                enrollment.getId(),
                enrollment.getUserId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt()
        );
    }
    
    public EnrollUserCommand toCommandFromResource(EnrollUserResource resource) {
        return new EnrollUserCommand(resource.userId(), resource.courseId());
    }
}
