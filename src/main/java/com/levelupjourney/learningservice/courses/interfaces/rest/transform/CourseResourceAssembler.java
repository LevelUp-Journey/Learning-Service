package com.levelupjourney.learningservice.courses.interfaces.rest.transform;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.domain.model.commands.*;
import com.levelupjourney.learningservice.courses.interfaces.rest.resources.*;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.GuideSummaryResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.TopicSummaryResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CourseResourceAssembler {
    
    public CourseResource toResourceFromEntity(Course course) {
        Set<TopicSummaryResource> topicResources = course.getTopics().stream()
                .map(topic -> new TopicSummaryResource(
                        topic.getId(),
                        topic.getName()))
                .collect(Collectors.toSet());
        
        List<GuideSummaryResource> guideResources = course.getGuides().stream()
                .map(guide -> new GuideSummaryResource(
                        guide.getId(),
                        guide.getTitle(),
                        guide.getDescription(),
                        guide.getCoverImage(),
                        guide.getStatus(),
                        guide.getLikesCount(),
                        guide.getPagesCount(),
                        guide.getAuthorIds(),
                        guide.getCreatedAt()))
                .collect(Collectors.toList());
        
        return new CourseResource(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCoverImage(),
                course.getStatus(),
                course.getLikesCount(),
                course.getAuthorIds(),
                topicResources,
                guideResources,
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
    
    public CreateCourseCommand toCommandFromResource(CreateCourseResource resource) {
        return new CreateCourseCommand(
                resource.title(),
                resource.description(),
                resource.coverImage(),
                resource.authorIds(),
                resource.topicIds()
        );
    }
    
    public UpdateCourseCommand toCommandFromResource(UUID courseId, UpdateCourseResource resource) {
        return new UpdateCourseCommand(
                courseId,
                resource.title(),
                resource.description(),
                resource.coverImage(),
                resource.topicIds()
        );
    }
    
    public UpdateCourseStatusCommand toCommandFromResource(UUID courseId, UpdateCourseStatusResource resource) {
        return new UpdateCourseStatusCommand(courseId, resource.status());
    }
    
    public UpdateCourseAuthorsCommand toCommandFromResource(UUID courseId, UpdateCourseAuthorsResource resource) {
        return new UpdateCourseAuthorsCommand(courseId, resource.authorIds());
    }
}
