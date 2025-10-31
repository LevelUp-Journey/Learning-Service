package com.levelupjourney.learningservice.guides.interfaces.rest;

import com.levelupjourney.learningservice.guides.domain.model.commands.*;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetGuideByIdQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.GetPagesByGuideIdQuery;
import com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesQuery;
import com.levelupjourney.learningservice.guides.domain.services.GuideCommandService;
import com.levelupjourney.learningservice.guides.domain.services.GuideQueryService;
import com.levelupjourney.learningservice.guides.domain.services.PageCommandService;
import com.levelupjourney.learningservice.guides.domain.services.PageQueryService;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.*;
import com.levelupjourney.learningservice.guides.interfaces.rest.transform.GuideResourceAssembler;
import com.levelupjourney.learningservice.guides.interfaces.rest.transform.PageResourceAssembler;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.shared.infrastructure.exception.ResourceNotFoundException;
import com.levelupjourney.learningservice.shared.infrastructure.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/guides")
@RequiredArgsConstructor
@Tag(name = "Guides", description = "Learning guides management")
public class GuidesController {

    private final GuideCommandService guideCommandService;
    private final GuideQueryService guideQueryService;
    private final PageCommandService pageCommandService;
    private final PageQueryService pageQueryService;
    private final SecurityContextHelper securityHelper;

    @GetMapping
    @Operation(
            summary = "Search guides",
            description = "Search guides with optional filters. Public guides are visible to all users."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guides retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<GuideResource>> searchGuides(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Set<UUID> topicIds,
            @RequestParam(required = false) Set<String> authorIds,
            Pageable pageable
    ) {
        // For non-authenticated users, only show PUBLISHED
        EntityStatus status = securityHelper.isAuthenticated() ? null : EntityStatus.PUBLISHED;

        var query = new SearchGuidesQuery(title, topicIds, authorIds, status, pageable);
        var guides = guideQueryService.handle(query);

        var resources = guides.map(guide ->
                GuideResourceAssembler.toResourceFromEntity(guide, false, false)
        );

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{guideId}")
    @Operation(
            summary = "Get guide by ID",
            description = "Retrieves a guide with its pages. Visibility depends on status and user permissions."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide found",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "404", description = "Guide not found or not accessible")
    })
    public ResponseEntity<GuideResource> getGuideById(@PathVariable UUID guideId) {
        var guide = guideQueryService.handle(new GetGuideByIdQuery(guideId))
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        // Check visibility
        if (!isGuideVisibleToUser(guide)) {
            throw new ResourceNotFoundException("Guide not found");
        }

        var resource = GuideResourceAssembler.toResourceFromEntity(guide, false, true);
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(
            summary = "Create guide",
            description = "Creates a new guide. Requires ADMIN or TEACHER role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Guide created successfully",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<GuideResource> createGuide(@Valid @RequestBody CreateGuideResource resource) {
        var command = GuideResourceAssembler.toCommandFromResource(resource);
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Failed to create guide"));

        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return new ResponseEntity<>(guideResource, HttpStatus.CREATED);
    }

    @PutMapping("/{guideId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update guide",
            description = "Updates a guide. Only authors and admins can update.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<GuideResource> updateGuide(
            @PathVariable UUID guideId,
            @Valid @RequestBody UpdateGuideResource resource
    ) {
        var command = GuideResourceAssembler.toCommandFromResource(guideId, resource);
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return ResponseEntity.ok(guideResource);
    }

    @PutMapping("/{guideId}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update guide status",
            description = "Updates guide status (DRAFT, PUBLISHED, ARCHIVED, etc.). Only authors and admins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<GuideResource> updateGuideStatus(
            @PathVariable UUID guideId,
            @RequestParam EntityStatus status
    ) {
        var command = new UpdateGuideStatusCommand(guideId, status);
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return ResponseEntity.ok(guideResource);
    }

    @PutMapping("/{guideId}/authors")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update guide authors",
            description = "Updates the list of guide authors. Only current authors and admins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<GuideResource> updateGuideAuthors(
            @PathVariable UUID guideId,
            @RequestBody Set<String> authorIds
    ) {
        var command = new UpdateGuideAuthorsCommand(guideId, authorIds);
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return ResponseEntity.ok(guideResource);
    }

    @DeleteMapping("/{guideId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Delete guide",
            description = "Soft deletes a guide. Only authors and admins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Guide deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<Void> deleteGuide(@PathVariable UUID guideId) {
        guideCommandService.handle(new DeleteGuideCommand(guideId));
        return ResponseEntity.noContent().build();
    }

    // ==================== PAGES ENDPOINTS ====================

    @GetMapping("/{guideId}/pages")
    @Operation(summary = "Get all pages of a guide", description = "Lists all pages in order")
    public ResponseEntity<List<PageResource>> getGuidePages(@PathVariable UUID guideId) {
        // Verify guide exists and is accessible
        var guide = guideQueryService.handle(new GetGuideByIdQuery(guideId))
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        if (!isGuideVisibleToUser(guide)) {
            throw new ResourceNotFoundException("Guide not found");
        }

        var pages = pageQueryService.handle(new GetPagesByGuideIdQuery(guideId));
        var resources = pages.stream()
                .map(PageResourceAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{guideId}/pages/{pageId}")
    @Operation(summary = "Get specific page", description = "Retrieves a specific page by ID")
    public ResponseEntity<PageResource> getPage(
            @PathVariable UUID guideId,
            @PathVariable UUID pageId
    ) {
        var page = pageQueryService.handle(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        // Verify page belongs to guide
        if (!page.getGuide().getId().equals(guideId)) {
            throw new ResourceNotFoundException("Page not found in this guide");
        }

        // Check guide visibility
        if (!isGuideVisibleToUser(page.getGuide())) {
            throw new ResourceNotFoundException("Guide not found");
        }

        var resource = PageResourceAssembler.toResourceFromEntity(page);
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{guideId}/pages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create page",
            description = "Adds a new page to the guide. Only authors and admins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Page created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Page with same order already exists")
    })
    public ResponseEntity<PageResource> createPage(
            @PathVariable UUID guideId,
            @Valid @RequestBody CreatePageResource resource
    ) {
        var command = PageResourceAssembler.toCommandFromResource(guideId, resource);
        var page = pageCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Failed to create page"));

        var pageResource = PageResourceAssembler.toResourceFromEntity(page);
        return new ResponseEntity<>(pageResource, HttpStatus.CREATED);
    }

    @PutMapping("/{guideId}/pages/{pageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update page",
            description = "Updates page content or order. Only authors and admins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<PageResource> updatePage(
            @PathVariable UUID guideId,
            @PathVariable UUID pageId,
            @Valid @RequestBody UpdatePageResource resource
    ) {
        var command = PageResourceAssembler.toCommandFromResource(pageId, resource);
        var page = pageCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        var pageResource = PageResourceAssembler.toResourceFromEntity(page);
        return ResponseEntity.ok(pageResource);
    }

    @DeleteMapping("/{guideId}/pages/{pageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Delete page",
            description = "Deletes a page from the guide. Only authors and admins.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Page deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Page not found")
    })
    public ResponseEntity<Void> deletePage(
            @PathVariable UUID guideId,
            @PathVariable UUID pageId
    ) {
        pageCommandService.handle(new DeletePageCommand(pageId));
        return ResponseEntity.noContent().build();
    }

    // ==================== HELPER METHODS ====================

    private boolean isGuideVisibleToUser(com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide guide) {
        // PUBLISHED guides are visible to everyone
        if (guide.getStatus() == EntityStatus.PUBLISHED) {
            return true;
        }

        // Non-authenticated users can only see PUBLISHED
        if (!securityHelper.isAuthenticated()) {
            return false;
        }

        String userId = securityHelper.getCurrentUserId();

        // Authors and admins can see their guides
        if (guide.isAuthor(userId) || securityHelper.isAdmin()) {
            return true;
        }

        // ASSOCIATED_WITH_COURSE requires enrollment check (TODO: implement when enrollments are ready)
        if (guide.getStatus() == EntityStatus.ASSOCIATED_WITH_COURSE) {
            // For now, not visible unless author/admin
            return false;
        }

        return false;
    }
}
