package com.levelupjourney.learningservice.guides.domain.model.commands;

import java.util.UUID;

/**
 * Command to remove a challenge from a guide
 * @param guideId The ID of the guide
 * @param challengeId The ID of the challenge to remove
 */
public record RemoveChallengeFromGuideCommand(
        UUID guideId,
        UUID challengeId
) {
}
