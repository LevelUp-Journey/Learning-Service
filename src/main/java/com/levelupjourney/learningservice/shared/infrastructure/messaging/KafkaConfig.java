package com.levelupjourney.learningservice.shared.infrastructure.messaging;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Kafka con soporte condicional para Azure Event Hubs
 * 
 * Esta configuración permite usar:
 * - Azure Event Hubs (cuando IS_AZURE=true): Usa SASL_SSL para conexión segura
 * - Kafka estándar (cuando IS_AZURE=false): Conexión sin autenticación
 * 
 * Características:
 * - Serialización JSON para mensajes
 * - Configuración de timeouts optimizada
 * - Modo condicional basado en variable de entorno IS_AZURE
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;
    
    @Value("${kafka.is-azure:false}")
    private boolean isAzure;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 2000);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 3000);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        
        // Configuración condicional para Azure Event Hubs
        if (isAzure) {
            configProps.put("security.protocol", "SASL_SSL");
            configProps.put("sasl.mechanism", "PLAIN");
            configProps.put("sasl.jaas.config", saslJaasConfig);
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

