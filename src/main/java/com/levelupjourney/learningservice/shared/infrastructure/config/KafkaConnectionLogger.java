package com.levelupjourney.learningservice.shared.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Logger component to monitor Kafka connection status
 */
@Component
@Slf4j
public class KafkaConnectionLogger {

    @Autowired(required = false)
    private KafkaAdmin kafkaAdmin;

    @Value("${spring.kafka.bootstrap-servers:N/A}")
    private String bootstrapServers;

    @EventListener(ApplicationReadyEvent.class)
    public void logKafkaConnection() {
        try {
            if (kafkaAdmin == null) {
                log.warn("=".repeat(80));
                log.warn("⚠️ KAFKA ADMIN NOT CONFIGURED");
                log.warn("=".repeat(80));
                return;
            }

            Map<String, Object> configs = kafkaAdmin.getConfigurationProperties();

            log.info("=".repeat(80));
            log.info("✅ KAFKA CONNECTION CONFIGURED");
            log.info("=".repeat(80));
            log.info("🔗 Bootstrap Servers: {}", bootstrapServers);
            log.info("🔒 Security Protocol: {}", configs.getOrDefault("security.protocol", "N/A"));
            log.info("🔑 SASL Mechanism: {}", configs.getOrDefault("sasl.mechanism", "N/A"));
            log.info("📤 Producer Key Serializer: {}", configs.getOrDefault("key.serializer", "N/A"));
            log.info("📥 Consumer Key Deserializer: {}", configs.getOrDefault("key.deserializer", "N/A"));
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("❌ KAFKA CONNECTION CHECK FAILED");
            log.error("=".repeat(80));
            log.error("⚠️ Error: {}", e.getMessage());
            log.error("💡 Note: This may be normal if Kafka is not required for startup");
            log.error("=".repeat(80));
        }
    }
}

