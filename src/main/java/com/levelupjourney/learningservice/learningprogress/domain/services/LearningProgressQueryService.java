package com.levelupjourney.learningservice.learningprogress.domain.services;

import com.levelupjourney.learningservice.learningprogress.domain.model.aggregates.LearningProgress;
import com.levelupjourney.learningservice.learningprogress.domain.model.queries.GetProgressQuery;
import com.levelupjourney.learningservice.learningprogress.domain.model.queries.GetUserProgressQuery;

import java.util.List;
import java.util.Optional;

public interface LearningProgressQueryService {
    Optional<LearningProgress> handle(GetProgressQuery query);
    List<LearningProgress> handle(GetUserProgressQuery query);
}
