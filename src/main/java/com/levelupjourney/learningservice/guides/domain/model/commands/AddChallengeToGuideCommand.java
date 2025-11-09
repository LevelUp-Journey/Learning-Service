package com.levelupjourney.learningservice.guides.domain.model.commands;

import java.util.UUID;

/**
 * Command to add a challenge to a guide
 * @param guideId The ID of the guide
 * @param challengeId The ID of the challenge to add
 */
public record AddChallengeToGuideCommand(
        UUID guideId,
        UUID challengeId
) {
}
