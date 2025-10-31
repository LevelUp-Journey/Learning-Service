package com.levelupjourney.learningservice.enrollments.domain.services;

import com.levelupjourney.learningservice.enrollments.domain.model.aggregates.Enrollment;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.CancelEnrollmentCommand;
import com.levelupjourney.learningservice.enrollments.domain.model.commands.EnrollUserCommand;

public interface EnrollmentCommandService {
    Enrollment handle(EnrollUserCommand command);
    Enrollment handle(CancelEnrollmentCommand command);
}
