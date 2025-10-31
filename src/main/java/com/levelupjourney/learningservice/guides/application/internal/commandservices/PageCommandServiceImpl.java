package com.levelupjourney.learningservice.guides.application.internal.commandservices;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import com.levelupjourney.learningservice.guides.domain.model.commands.CreatePageCommand;
import com.levelupjourney.learningservice.guides.domain.model.commands.DeletePageCommand;
import com.levelupjourney.learningservice.guides.domain.model.commands.UpdatePageCommand;
import com.levelupjourney.learningservice.guides.domain.services.PageCommandService;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.PageRepository;
import com.levelupjourney.learningservice.shared.infrastructure.exception.BusinessException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.shared.infrastructure.exception.UnauthorizedException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageCommandServiceImpl implements PageCommandService {

    private final PageRepository pageRepository;
    private final GuideRepository guideRepository;
    private final SecurityContextHelper securityHelper;

    @Override
    @Transactional
    public Optional<Page> handle(CreatePageCommand command) {
        var guide = guideRepository.findById(command.guideId())
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        // Check authorization
        String userId = securityHelper.getCurrentUserId();
        if (!guide.isAuthor(userId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to add pages to this guide");
        }

        // Check if order already exists
        if (pageRepository.existsByGuideIdAndOrder(command.guideId(), command.order())) {
            throw new BusinessException("A page with order " + command.order() + " already exists", 
                    HttpStatus.CONFLICT);
        }

        var page = new Page(guide, command.content(), command.order());
        var savedPage = pageRepository.save(page);
        
        // Update guide's page count
        guide.addPage(savedPage);
        guideRepository.save(guide);

        return Optional.of(savedPage);
    }

    @Override
    @Transactional
    public Optional<Page> handle(UpdatePageCommand command) {
        var page = pageRepository.findById(command.pageId())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        var guide = page.getGuide();
        
        // Check authorization
        String userId = securityHelper.getCurrentUserId();
        if (!guide.isAuthor(userId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to update this page");
        }

        if (command.content() != null && !command.content().isBlank()) {
            page.updateContent(command.content());
        }

        if (command.order() != null) {
            // Check if new order conflicts with existing page
            if (!page.getOrder().equals(command.order()) && 
                pageRepository.existsByGuideIdAndOrder(guide.getId(), command.order())) {
                throw new BusinessException("A page with order " + command.order() + " already exists", 
                        HttpStatus.CONFLICT);
            }
            page.updateOrder(command.order());
        }

        return Optional.of(pageRepository.save(page));
    }

    @Override
    @Transactional
    public void handle(DeletePageCommand command) {
        var page = pageRepository.findById(command.pageId())
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        var guide = page.getGuide();
        
        // Check authorization
        String userId = securityHelper.getCurrentUserId();
        if (!guide.isAuthor(userId) && !securityHelper.isAdmin()) {
            throw new UnauthorizedException("You don't have permission to delete this page");
        }

        guide.removePage(page);
        pageRepository.delete(page);
        guideRepository.save(guide);
    }
}
