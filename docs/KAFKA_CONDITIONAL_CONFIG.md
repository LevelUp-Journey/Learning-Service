# Configuración Condicional de Kafka

## Resumen de Cambios

Se ha implementado una configuración condicional de Kafka que permite usar tanto **Kafka estándar** (sin autenticación) como **Azure Event Hubs** (con SASL_SSL) dependiendo del valor de la variable de entorno `IS_AZURE`.

## Variable de Control

```bash
IS_AZURE=false  # Usa Kafka estándar (desarrollo local)
IS_AZURE=true   # Usa Azure Event Hubs con SASL_SSL (producción)
```

## Archivos Modificados

### 1. `KafkaConfig.java`

**Cambios:**
- Agregado campo `@Value("${kafka.is-azure:false}") private boolean isAzure;`
- Lógica condicional en `producerFactory()`:
  - Si `isAzure = true`: Configura SASL_SSL, PLAIN mechanism y JAAS config
  - Si `isAzure = false`: Solo configuración básica de serialización

```java
// Configuración condicional para Azure Event Hubs
if (isAzure) {
    configProps.put("security.protocol", "SASL_SSL");
    configProps.put("sasl.mechanism", "PLAIN");
    configProps.put("sasl.jaas.config", saslJaasConfig);
}
```

### 2. `application.yml`

**Cambios:**
- Agregado `kafka.is-azure: ${IS_AZURE:false}` para control condicional
- Modificado `spring.kafka.bootstrap-servers` con valor por defecto: `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`
- Propiedades de seguridad ahora son opcionales:
  - `security.protocol: ${KAFKA_SECURITY_PROTOCOL:PLAINTEXT}`
  - `sasl.mechanism: ${KAFKA_SASL_MECHANISM:PLAIN}`
  - `sasl.jaas.config: ${KAFKA_SASL_JAAS_CONFIG:}`

### 3. `.env`

**Estructura actualizada:**

```bash
# Kafka Configuration
IS_AZURE=false

# For IS_AZURE=false (Standard Kafka)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# For IS_AZURE=true (Azure Event Hubs) - Uncomment when needed:
# KAFKA_BOOTSTRAP_SERVERS=your-namespace.servicebus.windows.net:9093
# KAFKA_SECURITY_PROTOCOL=SASL_SSL
# KAFKA_SASL_MECHANISM=PLAIN
# KAFKA_SASL_JAAS_CONFIG=...
```

### 4. `.env.example`

**Nuevo archivo** con documentación completa de:
- Configuración para Kafka estándar
- Configuración para Azure Event Hubs
- Notas sobre cuándo usar cada opción

### 5. `KAFKA_CONFIGURATION.md`

**Actualizado** con:
- Explicación de configuración condicional
- Ejemplos para ambos escenarios
- Variables de entorno necesarias

## Escenarios de Uso

### Desarrollo Local (Kafka estándar)

**Variables de entorno:**
```bash
IS_AZURE=false
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Resultado:**
- Conexión sin autenticación
- Protocolo PLAINTEXT
- Ideal para desarrollo local con Kafka en Docker

### Producción (Azure Event Hubs)

**Variables de entorno:**
```bash
IS_AZURE=true
KAFKA_BOOTSTRAP_SERVERS=levelup-message-broker.servicebus.windows.net:9093
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="Endpoint=sb://...";
```

**Resultado:**
- Conexión segura con SASL_SSL
- Autenticación con Azure Event Hubs
- Protocolo compatible con Kafka

## Ventajas

✅ **Flexibilidad**: Un solo código funciona en desarrollo y producción
✅ **Seguridad**: SASL_SSL solo cuando es necesario (Azure)
✅ **Simplicidad**: Cambio mediante una variable de entorno
✅ **Mantenibilidad**: Configuración clara y documentada
✅ **Sin duplicación**: No necesitas perfiles separados

## Testing

### Test con Kafka Local

1. Asegúrate de tener Kafka corriendo localmente:
   ```bash
   docker run -d --name kafka -p 9092:9092 apache/kafka:latest
   ```

2. Configura `.env`:
   ```bash
   IS_AZURE=false
   KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   ```

3. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

### Test con Azure Event Hubs

1. Configura `.env`:
   ```bash
   IS_AZURE=true
   KAFKA_BOOTSTRAP_SERVERS=tu-namespace.servicebus.windows.net:9093
   KAFKA_SECURITY_PROTOCOL=SASL_SSL
   KAFKA_SASL_MECHANISM=PLAIN
   KAFKA_SASL_JAAS_CONFIG=...
   ```

2. Ejecuta la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```

## Eventos Publicados

Independientemente de la configuración (Kafka local o Azure Event Hubs), los siguientes eventos se publican:

- **`guides.challenge.added.v1`**: Cuando se agrega un challenge a un guide
  ```json
  {
    "guideId": "uuid",
    "challengeId": "uuid",
    "occurredAt": "2025-11-09T10:30:00Z"
  }
  ```

## Notas Importantes

- ⚠️ Cuando `IS_AZURE=true`, las variables `KAFKA_SECURITY_PROTOCOL`, `KAFKA_SASL_MECHANISM` y `KAFKA_SASL_JAAS_CONFIG` son **obligatorias**
- ⚠️ Cuando `IS_AZURE=false`, estas variables son **ignoradas**
- ✅ La aplicación compilará correctamente sin las variables de Azure cuando `IS_AZURE=false`
- ✅ Los valores por defecto permiten arrancar la aplicación sin configuración adicional (asume Kafka local)

## Migración desde Configuración Anterior

Si estabas usando la configuración anterior con Azure Event Hubs, migra así:

**Antes:**
```bash
KAFKA_BOOTSTRAP_SERVERS=namespace.servicebus.windows.net:9093
KAFKA_CONNECTION_STRING=Endpoint=sb://...
```

**Después:**
```bash
IS_AZURE=true
KAFKA_BOOTSTRAP_SERVERS=namespace.servicebus.windows.net:9093
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="Endpoint=sb://...";
```
