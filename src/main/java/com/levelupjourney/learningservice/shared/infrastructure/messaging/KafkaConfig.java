package com.levelupjourney.learningservice.shared.infrastructure.messaging;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Configuración de Kafka para Azure Event Hubs
 * 
 * Esta configuración permite la comunicación del Learning Service con Azure Event Hubs
 * a través del protocolo de Kafka. La configuración principal se encuentra en application.yml
 * 
 * Características:
 * - Protocolo SASL_SSL para conexión segura
 * - Serialización JSON para mensajes
 * - Auto-creación de tópicos deshabilitada (gestión manual en Azure)
 * - Configuración de timeouts optimizada para Azure Event Hubs
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    
    // La configuración se gestiona principalmente a través de application.yml
    // Esta clase solo habilita Kafka en la aplicación
    
    // Cuando se necesite configuración adicional (producers, consumers, topics específicos)
    // se pueden agregar @Bean aquí
}
