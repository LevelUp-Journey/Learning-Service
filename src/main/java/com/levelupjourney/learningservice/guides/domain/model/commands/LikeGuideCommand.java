package com.levelupjourney.learningservice.guides.domain.model.commands;

import java.util.UUID;

public record LikeGuideCommand(UUID guideId, String userId) {
}
