package com.levelupjourney.learningservice.guides.domain.services;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.commands.*;

import java.util.Optional;

public interface GuideCommandService {
    Optional<Guide> handle(CreateGuideCommand command);
    Optional<Guide> handle(UpdateGuideCommand command);
    Optional<Guide> handle(UpdateGuideStatusCommand command);
    Optional<Guide> handle(UpdateGuideAuthorsCommand command);
    Optional<Guide> handle(AddChallengeToGuideCommand command);
    Optional<Guide> handle(RemoveChallengeFromGuideCommand command);
    void handle(DeleteGuideCommand command);
}
