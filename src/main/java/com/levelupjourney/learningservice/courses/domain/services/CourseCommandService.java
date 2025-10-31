package com.levelupjourney.learningservice.courses.domain.services;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.courses.domain.model.commands.*;

public interface CourseCommandService {
    Course handle(CreateCourseCommand command);
    Course handle(UpdateCourseCommand command);
    Course handle(UpdateCourseStatusCommand command);
    Course handle(UpdateCourseAuthorsCommand command);
    void handle(DeleteCourseCommand command);
    Course handle(AssociateGuideCommand command);
    Course handle(DisassociateGuideCommand command);
}
