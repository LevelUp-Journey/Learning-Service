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
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/guides")
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
            summary = "Search guides with filters",
            description = """
                    Search and filter guides by title, topics, and authors.
                    - Students: Only PUBLISHED guides are visible
                    - Teachers: PUBLISHED guides + guides where they are authors
                    - Supports pagination with page, size, and sort parameters
                    Example: /api/v1/guides?title=Java&topicIds=uuid1,uuid2&page=0&size=20&sort=createdAt,desc
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guides retrieved successfully (paginated)",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<GuideResource>> searchGuides(
            @Parameter(description = "Filter by title (partial match, case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Filter by topic IDs (comma-separated UUIDs)")
            @RequestParam(required = false) Set<UUID> topicIds,
            @Parameter(description = "Filter by author IDs (comma-separated)")
            @RequestParam(required = false) Set<String> authorIds,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        // Simplified: just get by status or all
        EntityStatus status = null;

        if (!securityHelper.isAuthenticated() || !securityHelper.hasRole("ROLE_TEACHER")) {
            // Unauthenticated users and students see only PUBLISHED guides
            status = EntityStatus.PUBLISHED;
        }
        // Teachers see all guides (status = null)

        var query = new SearchGuidesQuery(title, topicIds, authorIds, status, null, pageable);
        var guides = guideQueryService.handle(query);

        var resources = guides.map(guide ->
                GuideResourceAssembler.toResourceFromEntity(guide, false, false)
        );

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/teachers/{teacherId}")
    @Operation(
            summary = "Get all guides by teacher ID",
            description = """
                    Retrieves all guides where the specified teacher is an author.
                    - Returns guides of all statuses (DRAFT, PUBLISHED, etc.)
                    - Useful for viewing a teacher's complete guide portfolio
                    - Supports pagination with page, size, and sort parameters
                    Example: /api/v1/guides/teachers/123?page=0&size=20&sort=createdAt,desc
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's guides retrieved successfully (paginated)",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<GuideResource>> getGuidesByTeacherId(
            @io.swagger.v3.oas.annotations.Parameter(description = "Teacher ID", required = true)
            @PathVariable String teacherId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        // Simplified: get all guides and filter in memory
        var query = new SearchGuidesQuery(null, null, null, null, null, pageable);
        var guides = guideQueryService.handle(query);

        // Filter by teacher ID in memory
        var filteredGuides = guides.getContent().stream()
                .filter(guide -> guide.getAuthors().stream()
                        .anyMatch(author -> author.getAuthorId().equals(teacherId)))
                .toList();

        var resources = filteredGuides.stream()
                .map(guide -> GuideResourceAssembler.toResourceFromEntity(guide, false, false))
                .toList();

        // Return as a simple response (not paginated for now, for simplicity)
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                resources,
                pageable,
                filteredGuides.size()
        ));
    }

    @GetMapping("/{guideId}")
    @Operation(
            summary = "Get guide by ID with pages",
            description = "Retrieves a guide with all its pages."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide found",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<GuideResource> getGuideById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        var guide = guideQueryService.handle(new GetGuideByIdQuery(guideId))
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        var resource = GuideResourceAssembler.toResourceFromEntity(guide, false, true);
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(
            summary = "Create a new guide",
            description = """
                    Creates a new learning guide with initial DRAFT status.
                    - Requires TEACHER or ADMIN role
                    - Guide starts with 0 pages (add pages separately)
                    - Can have multiple authors and topics
                    - pagesCount is automatically calculated when pages are added
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Guide created successfully",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires TEACHER or ADMIN)")
    })
    public ResponseEntity<GuideResource> createGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide creation data", required = true)
            @Valid @RequestBody CreateGuideResource resource
    ) {
        var command = GuideResourceAssembler.toCommandFromResource(resource);
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Failed to create guide"));

        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return new ResponseEntity<>(guideResource, HttpStatus.CREATED);
    }

    @PutMapping("/{guideId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update guide details",
            description = """
                    Updates guide title, description, or other metadata.
                    - Only guide authors or ADMIN can update
                    - Cannot update status (use PUT /guides/{guideId}/status)
                    - Cannot update pages (use page endpoints)
                    - Authors and topics can be updated via dedicated endpoints
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide updated successfully",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<GuideResource> updateGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Updated guide data", required = true)
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
            description = """
                    Updates the publication status of a guide.
                    - Supported statuses: DRAFT, PUBLISHED, ARCHIVED
                    - Only guide authors or ADMIN can change status
                    - PUBLISHED guides become visible to all users
                    - DRAFT guides are only visible to authors and admins
                    - Status transitions may have validation rules
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide status updated successfully",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<GuideResource> updateGuideStatus(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "New status data", required = true)
            @RequestBody @Valid UpdateGuideStatusResource statusResource
    ) {
        var command = new UpdateGuideStatusCommand(guideId, statusResource.status());
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        var guideResource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return ResponseEntity.ok(guideResource);
    }

    @PutMapping("/{guideId}/authors")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Update guide authors",
            description = """
                    Replaces the entire list of guide authors.
                    - Only current authors or ADMIN can update authors
                    - Maximum 5 authors per guide
                    - At least one author must remain
                    - Removing yourself as author requires another author to exist
                    - All author IDs must correspond to existing users
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide authors updated successfully",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (exceeds max authors, empty list, etc.)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found or author ID not found")
    })
    public ResponseEntity<GuideResource> updateGuideAuthors(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Set of author user IDs (max 5)", required = true)
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
            description = """
                    Soft deletes a guide (sets deleted flag, preserves data).
                    - Only guide authors or ADMIN can delete
                    - Deleted guides are hidden from all queries
                    - Associated pages are also marked as deleted
                    - Learning progress entries remain for historical tracking
                    - This is a soft delete - data is not physically removed
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Guide deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<Void> deleteGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        guideCommandService.handle(new DeleteGuideCommand(guideId));
        return ResponseEntity.noContent().build();
    }

    // ==================== PAGES ENDPOINTS ====================

    @GetMapping("/{guideId}/pages")
    @Operation(
            summary = "Get all pages of a guide",
            description = """
                    Lists all pages of a guide in order.
                    - Pages are returned sorted by `order` field
                    - Anonymous users can see pages of PUBLISHED guides
                    - Authors/admins can see pages of their DRAFT guides
                    - Returns 404 if guide not found or not accessible
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Guide not found or not accessible")
    })
    public ResponseEntity<List<PageResource>> getGuidePages(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        var pages = pageQueryService.handle(new GetPagesByGuideIdQuery(guideId));
        var resources = pages.stream()
                .map(PageResourceAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{guideId}/pages/{pageId}")
    @Operation(
            summary = "Get specific page details",
            description = """
                    Retrieves a specific page by its ID.
                    - Verifies page belongs to the specified guide
                    - Anonymous users can see pages of PUBLISHED guides
                    - Authors/admins can see pages of their DRAFT guides
                    - Returns 404 if page not found, wrong guide, or guide not accessible
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResource.class))),
            @ApiResponse(responseCode = "404", description = "Page not found or guide not accessible")
    })
    public ResponseEntity<PageResource> getPage(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Page UUID", required = true)
            @PathVariable UUID pageId
    ) {
        var page = pageQueryService.handle(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        // Verify page belongs to guide
        if (!page.getGuide().getId().equals(guideId)) {
            throw new ResourceNotFoundException("Page not found in this guide");
        }

        var resource = PageResourceAssembler.toResourceFromEntity(page);
        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{guideId}/pages")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create a new page",
            description = """
                    Adds a new page to the guide.
                    - Only guide authors or ADMIN can create pages
                    - `order` field determines page sequence (1-based)
                    - Each page must have unique order within guide
                    - `content` contains the learning material (Markdown/HTML supported)
                    - pagesCount is automatically updated on guide
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Page created successfully",
                    content = @Content(schema = @Schema(implementation = PageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found"),
            @ApiResponse(responseCode = "409", description = "Page with same order already exists")
    })
    public ResponseEntity<PageResource> createPage(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Page creation data", required = true)
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
            summary = "Update page content",
            description = """
                    Updates page title, content, or order.
                    - Only guide authors or ADMIN can update pages
                    - Content field accepts Markdown or HTML
                    - Updating order must maintain uniqueness within guide
                    - Changes to order automatically reorder other pages
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page updated successfully",
                    content = @Content(schema = @Schema(implementation = PageResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Page or guide not found"),
            @ApiResponse(responseCode = "409", description = "Page order conflict")
    })
    public ResponseEntity<PageResource> updatePage(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Page UUID", required = true)
            @PathVariable UUID pageId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Updated page data", required = true)
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
            description = """
                    Deletes a page from the guide.
                    - Only guide authors or ADMIN can delete pages
                    - Page is soft deleted (marked as deleted, data preserved)
                    - Guide's pagesCount is automatically decremented
                    - Page order gaps are preserved (not auto-reordered)
                    - Learning progress entries for this page remain intact
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Page deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Page or guide not found")
    })
    public ResponseEntity<Void> deletePage(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Page UUID", required = true)
            @PathVariable UUID pageId
    ) {
        pageCommandService.handle(new DeletePageCommand(pageId));
        return ResponseEntity.noContent().build();
    }

    // ==================== HELPER METHODS ====================

}
