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
import com.levelupjourney.learningservice.shared.infrastructure.exception.InvalidSearchCriteriaException;
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
    private final com.levelupjourney.learningservice.guides.application.internal.queryservices.GuideLikeQueryService guideLikeQueryService;

    @GetMapping
    @Operation(
            summary = "Get all guides with optional filters",
            description = """
                    Retrieve guides with optional filtering.
                    
                    **Authorization Rules:**
                    - **Students (ROLE_STUDENT)**: Only see PUBLISHED guides
                    - **Teachers (ROLE_TEACHER) without `for=dashboard`**: Only see PUBLISHED guides
                    - **Teachers (ROLE_TEACHER) with `for=dashboard`**: See ALL their own guides (DRAFT and PUBLISHED)
                    - **Unauthenticated users**: Only see PUBLISHED guides
                    
                    **Parameters:**
                    - `for=dashboard`: Special parameter for teachers to see their own guides
                    - `title`, `topicIds`, `authorIds`: Optional filters
                    - Standard pagination: page, size, sort
                    
                    **Examples:**
                    - Public view: `/api/v1/guides`
                    - Teacher dashboard: `/api/v1/guides?for=dashboard`
                    - Filtered search: `/api/v1/guides?title=Java&page=0&size=20`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guides retrieved successfully (paginated)",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<GuideResource>> getAllGuides(
            @Parameter(description = "Special filter: 'dashboard' for teachers to see their own guides")
            @RequestParam(name = "for", required = false) String forParam,

            @Parameter(description = "Filter by title (partial match, case-insensitive)")
            @RequestParam(required = false) String title,

            @Parameter(description = "Filter by topic IDs (comma-separated UUIDs)")
            @RequestParam(required = false) Set<UUID> topicIds,

            @Parameter(description = "Filter by author IDs (comma-separated)")
            @RequestParam(required = false) Set<String> authorIds,

            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        // Default: Everyone sees only PUBLISHED guides
        EntityStatus statusFilter = EntityStatus.PUBLISHED;
        String userIdFilter = null;
        
        // Check if teacher is requesting their dashboard
        boolean isDashboardRequest = "dashboard".equalsIgnoreCase(forParam);
        boolean isAuthenticated = securityHelper.isAuthenticated();
        boolean isTeacher = isAuthenticated && securityHelper.hasRole("ROLE_TEACHER");
        
        // CASE 1: ONLY TEACHER with for=dashboard -> Show ONLY their guides (all statuses)
        // Note: Students or other roles with for=dashboard are IGNORED and treated as public view
        if (isDashboardRequest && isTeacher) {
            userIdFilter = securityHelper.getCurrentUserId();
            statusFilter = null; // Don't filter by status, show DRAFT and PUBLISHED
        }
        // CASE 2: Anyone else (students, teachers without for=dashboard, unauthenticated)
        // -> Show ONLY PUBLISHED guides
        // This includes:
        // - Students (even if they try for=dashboard, it's ignored)
        // - Teachers without for=dashboard
        // - Unauthenticated users
        
        // Build query - ignore other filters for now if it's dashboard
        SearchGuidesQuery query;
        if (userIdFilter != null) {
            // Dashboard: only filter by userId
            query = new SearchGuidesQuery(null, null, null, statusFilter, userIdFilter, pageable);
        } else {
            // Public: filter by status (PUBLISHED)
            query = new SearchGuidesQuery(title, topicIds, authorIds, statusFilter, null, pageable);
        }
        
        var guides = guideQueryService.handle(query);

        // Get current user ID if authenticated
        String currentUserId = securityHelper.isAuthenticated() ? securityHelper.getCurrentUserId() : null;
        
        // Get all guide IDs
        var guideIds = guides.stream().map(com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        // Get liked guides by user
        var likedGuideIds = guideLikeQueryService.getGuidesLikedByUser(guideIds, currentUserId);

        var resources = guides.map(guide ->
                GuideResourceAssembler.toResourceFromEntity(
                        guide, 
                        likedGuideIds.contains(guide.getId()), 
                        false
                )
        );

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search guides by multiple filters",
            description = """
                    Advanced guide search with multiple optional filters.
                    Returns only PUBLISHED guides with basic information (id, title, description, coverImage).
                    
                    **Available Filters (all optional):**
                    - `title`: Partial match search (case-insensitive)
                    - `authorIds`: Filter by one or more author IDs (comma-separated)
                    - `likesCount`: Minimum number of likes required
                    - `topicIds`: Filter by one or more topic IDs (comma-separated UUIDs)
                    
                    **Examples:**
                    - Search by title: `/api/v1/guides/search?title=Java`
                    - Search by author: `/api/v1/guides/search?authorIds=author123`
                    - Search by multiple authors: `/api/v1/guides/search?authorIds=author1,author2`
                    - Search by minimum likes: `/api/v1/guides/search?likesCount=10`
                    - Search by topics: `/api/v1/guides/search?topicIds=uuid1,uuid2`
                    - Combined search: `/api/v1/guides/search?title=Spring&likesCount=5&topicIds=uuid1`
                    
                    **Pagination:**
                    - Use `page`, `size`, and `sort` query parameters
                    - Example: `/api/v1/guides/search?title=Java&page=0&size=20&sort=likesCount,desc`
                    
                    **Note:** At least one filter parameter must be provided.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guides retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria - at least one filter must be provided",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<Page<com.levelupjourney.learningservice.guides.interfaces.rest.resources.GuideSearchResource>> searchGuidesByFilters(
            @Parameter(description = "Filter by title (partial match, case-insensitive)", example = "Java Programming")
            @RequestParam(required = false) String title,
            @Parameter(description = "Filter by author IDs (comma-separated)", example = "author123,author456")
            @RequestParam(required = false) Set<String> authorIds,
            @Parameter(description = "Filter by minimum number of likes", example = "10")
            @RequestParam(required = false) Integer likesCount,
            @Parameter(description = "Filter by topic IDs (comma-separated UUIDs)", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) Set<UUID> topicIds,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        // Create the query
        var query = new com.levelupjourney.learningservice.guides.domain.model.queries.SearchGuidesByFiltersQuery(
                title,
                authorIds,
                likesCount,
                topicIds,
                pageable
        );

        // Validate that at least one search criteria is provided
        if (!query.hasSearchCriteria()) {
            throw new InvalidSearchCriteriaException(
                    "At least one search parameter must be provided (title, authorIds, likesCount, or topicIds)"
            );
        }

        // Execute the query
        var guides = guideQueryService.handle(query);

        // Map to search resources
        var resources = guides.map(GuideResourceAssembler::toSearchResourceFromEntity);

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/teachers/{teacherId}")
    @Operation(
            summary = "Get all guides by teacher ID",
            description = """
                    Retrieves guides by a specific teacher.
                    
                    **Authorization Rules:**
                    - Always returns only PUBLISHED guides (public portfolio view)
                    - For teachers to see their own DRAFT guides, use `/api/v1/guides?for=dashboard`
                    
                    **Use Case:** Public view of a teacher's published content portfolio
                    
                    Example: `/api/v1/guides/teachers/teacher123?page=0&size=20&sort=createdAt,desc`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher's published guides retrieved successfully (paginated)",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<GuideResource>> getGuidesByTeacherId(
            @Parameter(description = "Teacher ID", required = true)
            @PathVariable String teacherId,
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        // Only show PUBLISHED guides (public portfolio)
        var query = new SearchGuidesQuery(null, null, Set.of(teacherId), EntityStatus.PUBLISHED, null, pageable);
        var guides = guideQueryService.handle(query);

        // Get current user ID if authenticated
        String currentUserId = securityHelper.isAuthenticated() ? securityHelper.getCurrentUserId() : null;
        
        // Get all guide IDs
        var guideIds = guides.stream().map(com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide::getId)
                .collect(java.util.stream.Collectors.toSet());
        
        // Get liked guides by user
        var likedGuideIds = guideLikeQueryService.getGuidesLikedByUser(guideIds, currentUserId);

        var resources = guides.map(guide -> 
                GuideResourceAssembler.toResourceFromEntity(
                        guide, 
                        likedGuideIds.contains(guide.getId()), 
                        false
                )
        );

        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{guideId}")
    @Operation(
            summary = "Get guide by ID with pages",
            description = """
                    Retrieves a guide with all its pages.
                    
                    **Authorization Rules:**
                    - **PUBLISHED guides**: Any authenticated user can view
                    - **DRAFT guides**: Only the guide authors can view
                    - **Unauthenticated users**: Can only view PUBLISHED guides
                    
                    Returns 404 if guide doesn't exist or user doesn't have permission to view it.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guide found",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "404", description = "Guide not found or not accessible")
    })
    public ResponseEntity<GuideResource> getGuideById(
            @Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        var guide = guideQueryService.handle(new GetGuideByIdQuery(guideId))
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        // Authorization check
        if (guide.getStatus() == EntityStatus.PUBLISHED) {
            // PUBLISHED guides: anyone can view (just needs authentication)
            // No additional checks needed
        } else {
            // DRAFT or other statuses: only authors can view
            if (!securityHelper.isAuthenticated()) {
                throw new ResourceNotFoundException("Guide not found");
            }
            
            String currentUserId = securityHelper.getCurrentUserId();
            boolean isAuthor = currentUserId != null && guide.isAuthor(currentUserId);
            
            if (!isAuthor) {
                throw new ResourceNotFoundException("Guide not found");
            }
        }

        // Check if current user has liked this guide
        String currentUserId = securityHelper.isAuthenticated() ? securityHelper.getCurrentUserId() : null;
        boolean hasLiked = guideLikeQueryService.hasUserLikedGuide(guideId, currentUserId);

        var resource = GuideResourceAssembler.toResourceFromEntity(guide, hasLiked, true);
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
            @Parameter(description = "Guide creation data", required = true)
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
            @Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @Parameter(description = "Updated guide data", required = true)
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
            @Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @Parameter(description = "New status data", required = true)
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
            @Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @Parameter(description = "Set of author user IDs (max 5)", required = true)
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
                    
                    **Authorization Rules:**
                    - **PUBLISHED guides**: Any authenticated user can view pages
                    - **DRAFT guides**: Only guide authors can view pages
                    - **Unauthenticated users**: Can only view pages of PUBLISHED guides
                    
                    Pages are returned sorted by `order` field.
                    Returns 404 if guide not found or user doesn't have permission to view it.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Guide not found or not accessible")
    })
    public ResponseEntity<List<PageResource>> getGuidePages(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        // First, verify guide access using the same logic as getGuideById
        var guide = guideQueryService.handle(new GetGuideByIdQuery(guideId))
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        // Authorization check
        if (guide.getStatus() == EntityStatus.PUBLISHED) {
            // PUBLISHED guides: anyone can view pages
        } else {
            // DRAFT or other statuses: only authors can view
            if (!securityHelper.isAuthenticated()) {
                throw new ResourceNotFoundException("Guide not found");
            }
            
            String currentUserId = securityHelper.getCurrentUserId();
            boolean isAuthor = currentUserId != null && guide.isAuthor(currentUserId);
            
            if (!isAuthor) {
                throw new ResourceNotFoundException("Guide not found");
            }
        }

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
                    
                    **Authorization Rules:**
                    - **PUBLISHED guides**: Any authenticated user can view the page
                    - **DRAFT guides**: Only guide authors can view the page
                    - **Unauthenticated users**: Can only view pages of PUBLISHED guides
                    
                    Verifies page belongs to the specified guide.
                    Returns 404 if page not found, wrong guide, or guide not accessible.
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

        // Get the guide to check authorization
        var guide = page.getGuide();

        // Authorization check
        if (guide.getStatus() == EntityStatus.PUBLISHED) {
            // PUBLISHED guides: anyone can view pages
        } else {
            // DRAFT or other statuses: only authors can view
            if (!securityHelper.isAuthenticated()) {
                throw new ResourceNotFoundException("Guide not found");
            }
            
            String currentUserId = securityHelper.getCurrentUserId();
            boolean isAuthor = currentUserId != null && guide.isAuthor(currentUserId);
            
            if (!isAuthor) {
                throw new ResourceNotFoundException("Guide not found");
            }
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
            @Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @Parameter(description = "Page creation data", required = true)
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

    // ==================== CHALLENGE MANAGEMENT ====================

    @PostMapping("/{guideId}/challenges/{challengeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Add challenge to guide",
            description = """
                    Adds a challenge reference to a guide. This allows students to practice and 
                    reinforce their learning with related challenges.
                    - Only guide authors or ADMIN can add challenges
                    - Publishes event to Kafka topic: guides.challenge.added.v1
                    - Returns HTTP 201 (Created) on success
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Challenge added successfully",
                    content = @Content(schema = @Schema(implementation = GuideResource.class))),
            @ApiResponse(responseCode = "400", description = "Challenge already added to guide"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<GuideResource> addChallengeToGuide(
            @Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @Parameter(description = "Challenge UUID", required = true)
            @PathVariable UUID challengeId
    ) {
        var command = new AddChallengeToGuideCommand(guideId, challengeId);
        var guide = guideCommandService.handle(command)
                .orElseThrow(() -> new ResourceNotFoundException("Guide not found"));

        var resource = GuideResourceAssembler.toResourceFromEntity(guide, false, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @DeleteMapping("/{guideId}/challenges/{challengeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Remove challenge from guide",
            description = """
                    Removes a challenge reference from a guide.
                    - Only guide authors or ADMIN can remove challenges
                    - Returns HTTP 204 (No Content) on success
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Challenge removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is not an author or admin"),
            @ApiResponse(responseCode = "404", description = "Guide or challenge not found")
    })
    public ResponseEntity<Void> removeChallengeFromGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Challenge UUID", required = true)
            @PathVariable UUID challengeId
    ) {
        var command = new RemoveChallengeFromGuideCommand(guideId, challengeId);
        guideCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    // ==================== GUIDE LIKES ====================

    @PostMapping("/{guideId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Like a guide",
            description = """
                    Add a like to a guide. Users can only like a guide once.
                    - Requires authentication
                    - Returns 400 if user already liked this guide
                    - Increments the guide's likes count
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Guide liked successfully"),
            @ApiResponse(responseCode = "400", description = "User already liked this guide"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<Void> likeGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        String userId = securityHelper.getCurrentUserId();
        var command = new LikeGuideCommand(guideId, userId);
        guideCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{guideId}/likes")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Unlike a guide",
            description = """
                    Remove a like from a guide.
                    - Requires authentication
                    - Returns 400 if user hasn't liked this guide
                    - Decrements the guide's likes count
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Guide unliked successfully"),
            @ApiResponse(responseCode = "400", description = "User hasn't liked this guide"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - no valid JWT token"),
            @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<Void> unlikeGuide(
            @io.swagger.v3.oas.annotations.Parameter(description = "Guide UUID", required = true)
            @PathVariable UUID guideId
    ) {
        String userId = securityHelper.getCurrentUserId();
        var command = new UnlikeGuideCommand(guideId, userId);
        guideCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }

    // ==================== HELPER METHODS ====================

}
