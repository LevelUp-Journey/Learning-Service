# Configuración de Kafka / Azure Event Hubs

## Resumen

El Learning Service está configurado para integrarse con **Apache Kafka** o **Azure Event Hubs** (que es compatible con el protocolo de Kafka). Esta configuración permite la comunicación asíncrona entre microservicios mediante eventos.

La configuración es **condicional** basada en la variable de entorno `IS_AZURE`:
- `IS_AZURE=false`: Usa Kafka estándar sin autenticación (desarrollo local)
- `IS_AZURE=true`: Usa Azure Event Hubs con protocolo SASL_SSL (producción)

## Estado Actual

✅ **Kafka está configurado y listo para usar**
✅ **Configuración condicional implementada**
✅ **Soporte para Kafka local y Azure Event Hubs**

## Configuración en `application.yml`

```yaml
spring:
  kafka:
    # Bootstrap servers con valor por defecto para desarrollo local
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
    properties:
      # Propiedades condicionales (solo activas cuando IS_AZURE=true)
      security.protocol: ${KAFKA_SECURITY_PROTOCOL:PLAINTEXT}
      sasl.mechanism: ${KAFKA_SASL_MECHANISM:PLAIN}
      sasl.jaas.config: ${KAFKA_SASL_JAAS_CONFIG:}

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        max.block.ms: 2000
        request.timeout.ms: 2000
        delivery.timeout.ms: 3000

    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: true

    admin:
      auto-create: false
      fail-fast: false

kafka:
  is-azure: ${IS_AZURE:false}  # Control de configuración condicional
  topics:
    guide-challenge-added: guides.challenge.added.v1
```

## Variables de Entorno

### Opción 1: Kafka Estándar (Desarrollo Local)

```bash
# Activar modo Kafka estándar
IS_AZURE=false

# Servidor Kafka local
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Opción 2: Azure Event Hubs (Producción)

```bash
# Activar modo Azure Event Hubs
IS_AZURE=true

# Configuración de Azure
KAFKA_BOOTSTRAP_SERVERS=<tu-namespace>.servicebus.windows.net:9093
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="Endpoint=sb://<namespace>.servicebus.windows.net/;SharedAccessKeyName=<key-name>;SharedAccessKey=<key>";
```

### Para Kafka Local (Desarrollo)

```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONNECTION_STRING=not-required-for-local
```

## Cómo Usar Azure Event Hubs

### 1. Crear Event Hub en Azure Portal

1. Ve a Azure Portal
2. Crea un **Event Hubs Namespace** (si no existe)
3. Dentro del namespace, crea uno o más **Event Hubs** (equivalente a tópicos de Kafka)

### 2. Obtener Cadena de Conexión

1. Ve a tu Event Hubs Namespace
2. En "Settings" → "Shared access policies"
3. Selecciona o crea una política (ej: `RootManageSharedAccessKey`)
4. Copia la "Connection string–primary key"

### 3. Configurar Variables de Entorno

```bash
# El namespace de Event Hubs
KAFKA_BOOTSTRAP_SERVERS=mi-namespace.servicebus.windows.net:9093

# La cadena de conexión completa
KAFKA_CONNECTION_STRING=Endpoint=sb://mi-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xxxxx
```

## Tópicos Pendientes de Asignar

Cuando se definan los tópicos/colas para este microservicio, se agregarán aquí:

```yaml
kafka:
  topics:
    # Ejemplos de tópicos que podrían necesitarse:
    # guide-published: guide.published
    # guide-updated: guide.updated
    # course-created: course.created
    # course-enrolled: course.enrolled
    # learning-progress-updated: learning.progress.updated
```

## Ejemplo de Uso

### Producer (Enviar Evento)

```java
@Service
@RequiredArgsConstructor
public class GuideEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.guide-published}")
    private String guidePublishedTopic;
    
    public void publishGuidePublished(GuidePublishedEvent event) {
        kafkaTemplate.send(guidePublishedTopic, event.getGuideId().toString(), event);
    }
}
```

### Consumer (Recibir Evento)

```java
@Service
@Slf4j
public class EnrollmentEventConsumer {
    
    @KafkaListener(
        topics = "${kafka.topics.course-enrolled}",
        groupId = "learning-service"
    )
    public void handleCourseEnrolled(CourseEnrolledEvent event) {
        log.info("Received course enrolled event: {}", event);
        // Procesar el evento
    }
}
```

## Compatibilidad

Esta configuración es compatible con:
- ✅ Apache Kafka (local/self-hosted)
- ✅ Azure Event Hubs
- ✅ Confluent Cloud
- ✅ Amazon MSK
- ✅ Cualquier servicio compatible con protocolo Kafka

## Próximos Pasos

1. **Definir tópicos necesarios** para el Learning Service
2. **Crear Event Hubs** en Azure (o tópicos en Kafka local)
3. **Implementar producers** para eventos salientes
4. **Implementar consumers** para eventos entrantes
5. **Agregar configuración de tópicos** en `application.yml`

## Notas Importantes

- ⚠️ **Auto-creación deshabilitada:** Los tópicos deben crearse manualmente en Azure Event Hubs
- ⚠️ **Serialización JSON:** Los mensajes se serializan/deserializan automáticamente a JSON
- ⚠️ **Timeouts optimizados:** Configurados para Azure Event Hubs (2-3 segundos)
- ✅ **Fail-fast deshabilitado:** La aplicación arranca aunque Kafka no esté disponible
