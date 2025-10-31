package com.levelupjourney.learningservice.guides.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Topic summary for guides and courses")
public record TopicSummaryResource(
        @Schema(description = "Topic ID")
        UUID id,
        
        @Schema(description = "Topic name")
        String name
) {
}
