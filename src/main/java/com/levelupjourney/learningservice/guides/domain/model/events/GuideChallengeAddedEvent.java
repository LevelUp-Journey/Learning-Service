package com.levelupjourney.learningservice.guides.domain.model.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a challenge is added to a guide.
 * This event is sent to Kafka topic "guides.challenge.added.v1"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuideChallengeAddedEvent {
    
    private UUID guideId;
    private UUID challengeId;
    private Instant occurredAt;
    
    public GuideChallengeAddedEvent(UUID guideId, UUID challengeId) {
        this.guideId = guideId;
        this.challengeId = challengeId;
        this.occurredAt = Instant.now();
    }
}
