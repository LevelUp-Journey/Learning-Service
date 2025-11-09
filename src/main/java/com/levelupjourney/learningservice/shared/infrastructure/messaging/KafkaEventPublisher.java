package com.levelupjourney.learningservice.shared.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service to publish events to Kafka topics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.guide-challenge-added:guides.challenge.added.v1}")
    private String guideChallengeAddedTopic;
    
    /**
     * Publishes an event to a Kafka topic
     * @param topic The topic to publish to
     * @param key The message key (used for partitioning)
     * @param event The event object to publish
     */
    public void publishEvent(String topic, String key, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                    kafkaTemplate.send(topic, key, event);
            
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to topic {}: {}", topic, ex.getMessage(), ex);
                } else {
                    log.info("Successfully published event to topic {}, partition {}, offset {}", 
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Error publishing event to topic {}: {}", topic, e.getMessage(), e);
        }
    }
    
    /**
     * Publishes an event to a Kafka topic without a key
     * @param topic The topic to publish to
     * @param event The event object to publish
     */
    public void publishEvent(String topic, Object event) {
        publishEvent(topic, null, event);
    }
    
    /**
     * Gets the configured topic name for guide challenge added events
     * @return The topic name
     */
    public String getGuideChallengeAddedTopic() {
        return guideChallengeAddedTopic;
    }
}
