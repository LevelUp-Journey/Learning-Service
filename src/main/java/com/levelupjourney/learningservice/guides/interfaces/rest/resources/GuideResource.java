package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Guide resource representation")
public record GuideResource(
        @Schema(description = "Unique identifier of the guide")
        UUID id,
        
        @Schema(description = "Guide title")
        String title,
        
        @Schema(description = "Guide description")
        String description,
        
        @Schema(description = "Cover image URL")
        String coverImage,
        
        @Schema(description = "Guide status")
        String status,
        
        @Schema(description = "Number of likes")
        Integer likesCount,
        
        @Schema(description = "Whether current user has liked this guide")
        Boolean likedByRequester,
        
        @Schema(description = "Number of pages")
        Integer pagesCount,
        
        @Schema(description = "Author IDs")
        Set<String> authorIds,
        
        @Schema(description = "Topics")
        List<TopicSummaryResource> topics,
        
        @Schema(description = "Pages")
        List<PageResource> pages,
        
        @Schema(description = "Related challenge IDs for practice and reinforcement")
        Set<UUID> relatedChallenges,
        
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,
        
        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
}
