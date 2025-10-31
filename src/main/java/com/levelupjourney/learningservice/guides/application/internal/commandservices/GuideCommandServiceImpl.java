package com.levelupjourney.learningservice.guides.application.internal.commandservices;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.commands.*;
import com.levelupjourney.learningservice.guides.domain.services.GuideCommandService;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.shared.infrastructure.exception.BusinessException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import com.levelupjourney.learningservice.topics.infrastructure.persistence.jpa.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideCommandServiceImpl implements GuideCommandService {

    private final GuideRepository guideRepository;
    private final TopicRepository topicRepository;
    private final SecurityContextHelper securityHelper;

    @Value("${application.guides.max-authors}")
    private int maxAuthors;

    @Override
    @Transactional
    public Optional<Guide> handle(CreateGuideCommand command) {
        // Validate topics exist
        var topics = topicRepository.findAllById(command.topicIds())
                .stream().collect(Collectors.toSet());

        if (topics.size() != command.topicIds().size()) {
            throw new BusinessException("Some topics not found", HttpStatus.BAD_REQUEST);
        }

        // Determine authors
        Set<String> authors = command.authorIds() != null && !command.authorIds().isEmpty()
                ? command.authorIds()
                : Set.of(securityHelper.getCurrentUserId());

        if (authors.size() > maxAuthors) {
            throw new BusinessException("Maximum number of authors (" + maxAuthors + ") exceeded", 
                    HttpStatus.BAD_REQUEST);
        }

        // Create and save guide
        var guide = new Guide(
                command.title(),
                command.description(),
                command.coverImage(),
                authors,
                topics
        );

        return Optional.of(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public Optional<Guide> handle(UpdateGuideCommand command) {
        var guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        // Check authorization
        checkAuthorization(guide);

        // Update fields
        if (command.title() != null && !command.title().isBlank()) {
            guide.updateTitle(command.title());
        }
        if (command.description() != null) {
            guide.updateDescription(command.description());
        }
        if (command.coverImage() != null) {
            guide.updateCoverImage(command.coverImage());
        }

        if (command.topicIds() != null && !command.topicIds().isEmpty()) {
            var topics = topicRepository.findAllById(command.topicIds())
                    .stream().collect(Collectors.toSet());
            if (topics.size() != command.topicIds().size()) {
                throw new BusinessException("Some topics not found", HttpStatus.BAD_REQUEST);
            }
            guide.setTopics(topics);
        }

        return Optional.of(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public Optional<Guide> handle(UpdateGuideStatusCommand command) {
        var guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        checkAuthorization(guide);

        guide.updateStatus(command.status());
        return Optional.of(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public Optional<Guide> handle(UpdateGuideAuthorsCommand command) {
        var guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        checkAuthorization(guide);

        if (command.authorIds() == null || command.authorIds().isEmpty()) {
            throw new BusinessException("At least one author is required", HttpStatus.BAD_REQUEST);
        }

        guide.setAuthors(command.authorIds(), maxAuthors);
        return Optional.of(guideRepository.save(guide));
    }

    @Override
    @Transactional
    public void handle(DeleteGuideCommand command) {
        var guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        checkAuthorization(guide);

        // Soft delete
        guide.updateStatus(EntityStatus.DELETED);
        guideRepository.save(guide);
    }

    private void checkAuthorization(Guide guide) {
        String userId = securityHelper.getCurrentUserId();
        if (!guide.isAuthor(userId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to modify this guide");
        }
    }
}
