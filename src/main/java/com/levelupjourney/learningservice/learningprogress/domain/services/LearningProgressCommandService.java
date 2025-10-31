package com.levelupjourney.learningservice.learningprogress.domain.services;

import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.CompleteProgressCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.StartLearningCommand;
import com.levelupjourney.learningservice.learningprogress.domain.model.commands.UpdateProgressCommand;

public interface LearningProgressCommandService {
    LearningProgress handle(StartLearningCommand command);
    LearningProgress handle(UpdateProgressCommand command);
    LearningProgress handle(CompleteProgressCommand command);
}
