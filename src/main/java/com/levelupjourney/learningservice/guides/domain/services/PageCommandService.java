package com.levelupjourney.learningservice.guides.domain.services;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import com.levelupjourney.learningservice.guides.domain.model.commands.CreatePageCommand;
import com.levelupjourney.learningservice.guides.domain.model.commands.DeletePageCommand;
import com.levelupjourney.learningservice.guides.domain.model.commands.UpdatePageCommand;

import java.util.Optional;

public interface PageCommandService {
    Optional<Page> handle(CreatePageCommand command);
    Optional<Page> handle(UpdatePageCommand command);
    void handle(DeletePageCommand command);
}
