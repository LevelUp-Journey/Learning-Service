# Learning Service - Docker Setup

Este documento explica cómo ejecutar el Learning Service usando Docker y Docker Compose.

## 🚀 Inicio Rápido

### Opción 1: Usando el script de automatización (Recomendado)

```bash
# Hacer el script ejecutable (solo la primera vez)
chmod +x docker-run.sh

# Construir y ejecutar todo
./docker-run.sh build && ./docker-run.sh up

# Verificar que todo esté funcionando
./docker-run.sh test

# Ver logs de la aplicación
./docker-run.sh logs-app
```

### Opción 2: Usando Docker Compose directamente

```bash
# Construir la imagen
docker-compose build

# Ejecutar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f learning-service
```

## 📋 Servicios Incluidos

La configuración de Docker incluye todos los servicios necesarios:

- **🗄️ PostgreSQL** (puerto 5432) - Base de datos
- **📨 Kafka + Zookeeper** (puertos 9092, 2181) - Message broker
- **🌐 Eureka** (puerto 8761) - Service Discovery
- **🚀 Learning Service** (puerto 8085) - Aplicación principal

## 🔧 Comandos Disponibles

### Script `docker-run.sh`

| Comando | Descripción |
|---------|-------------|
| `./docker-run.sh build` | Construir la imagen Docker |
| `./docker-run.sh up` | Iniciar todos los servicios |
| `./docker-run.sh down` | Detener todos los servicios |
| `./docker-run.sh restart` | Reiniciar todos los servicios |
| `./docker-run.sh logs` | Ver logs de todos los servicios |
| `./docker-run.sh logs-app` | Ver logs solo de la aplicación |
| `./docker-run.sh status` | Ver estado de todos los servicios |
| `./docker-run.sh clean` | Limpiar contenedores y volúmenes |
| `./docker-run.sh shell` | Abrir shell en el contenedor de la app |
| `./docker-run.sh test` | Ejecutar pruebas de salud |

### Docker Compose directo

```bash
# Ver estado de servicios
docker-compose ps

# Ver logs específicos
docker-compose logs postgres
docker-compose logs kafka
docker-compose logs eureka
docker-compose logs learning-service

# Ejecutar comandos en contenedores
docker-compose exec learning-service /bin/bash
docker-compose exec postgres psql -U postgres -d learning_db
```

## 🌐 Acceder a los Servicios

Una vez que todos los servicios estén ejecutándose:

- **📖 Learning Service API**: http://localhost:8085
- **📚 Swagger UI**: http://localhost:8085/swagger-ui/index.html
- **💚 Health Check**: http://localhost:8085/actuator/health
- **🗄️ PostgreSQL**: localhost:5432 (usuario: postgres, password: postgres)
- **📨 Kafka**: localhost:9092
- **🌐 Eureka Dashboard**: http://localhost:8761

## 🔧 Configuración

### Variables de Entorno

El servicio usa las siguientes variables de entorno (configuradas en `docker-compose.yml`):

```yaml
# Base de datos
DB_URL: jdbc:postgresql://postgres:5432/learning_db
DB_USERNAME: postgres
DB_PASSWORD: postgres

# Kafka (local)
KAFKA_BOOTSTRAP_SERVERS: kafka:29092

# JWT
JWT_SECRET: your-secret-key-at-least-512-bits-long-change-this-in-production-use-a-strong-random-key

# Servidor
SERVER_PORT: 8080

# Eureka
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka:8761/eureka/

# Perfil Spring
SPRING_PROFILES_ACTIVE: docker
```

### Perfiles de Spring Boot

- **`default`**: Configuración para desarrollo local con Azure Event Hubs
- **`docker`**: Configuración optimizada para contenedores con Kafka local

## 🏗️ Arquitectura Docker

### Multi-stage Build

El `Dockerfile` usa una construcción multi-etapa optimizada:

1. **Build Stage**: Compila la aplicación usando JDK
2. **Runtime Stage**: Ejecuta la aplicación usando JRE optimizado

### Mejores Prácticas Implementadas

- ✅ **Usuario no-root** para seguridad
- ✅ **Health checks** para todos los servicios
- ✅ **Graceful shutdown** configurado
- ✅ **JVM optimizada** para contenedores
- ✅ **Compresión de respuestas** habilitada
- ✅ **Logging estructurado** para Docker
- ✅ **Dependencias saludables** entre servicios

## 🔍 Monitoreo y Logs

### Logs de Conexión

La aplicación incluye loggers automáticos que muestran:

- ✅ Conexión a PostgreSQL
- ✅ Configuración de Kafka
- ✅ Registro en Eureka
- ✅ Estado de salud de todos los servicios

### Health Checks

Cada servicio tiene health checks configurados:

```bash
# Verificar salud de todos los servicios
./docker-run.sh status

# Health check individual
curl http://localhost:8085/actuator/health
```

## 🧪 Testing

### Pruebas Automáticas

```bash
# Ejecutar todas las pruebas de salud
./docker-run.sh test
```

### Pruebas Manuales

```bash
# Verificar conectividad
curl http://localhost:8085/actuator/health

# Ver servicios registrados en Eureka
curl http://localhost:8761/eureka/apps

# Ver métricas
curl http://localhost:8085/actuator/metrics
```

## 🛠️ Troubleshooting

### Problemas Comunes

1. **Puerto ocupado**: Cambia los puertos en `docker-compose.yml`
2. **Sin memoria**: Aumenta la memoria asignada a Docker
3. **Kafka no inicia**: Verifica que Zookeeper esté saludable primero
4. **Base de datos no conecta**: Espera a que PostgreSQL esté completamente listo

### Logs de Debug

```bash
# Ver logs detallados
./docker-run.sh logs

# Ver logs de un servicio específico
docker-compose logs -f learning-service

# Ver logs con timestamps
docker-compose logs --timestamps learning-service
```

### Limpiar Todo

```bash
# Opción segura (pregunta confirmación)
./docker-run.sh clean

# Forzar limpieza completa
docker-compose down -v --remove-orphans
docker system prune -f
```

## 📊 Rendimiento

### Optimizaciones JVM

```bash
# Configuración actual
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
-XX:+UseG1GC
-XX:+UseStringDeduplication
-Djava.security.egd=file:/dev/./urandom
```

### Recursos Recomendados

- **CPU**: 1-2 cores por servicio
- **RAM**: 512MB-1GB para la aplicación
- **Disco**: 2GB para imágenes + 1GB para volúmenes

## 🚀 Producción

Para producción, modifica:

1. **Variables de entorno** con valores reales
2. **Secrets seguros** (no uses valores por defecto)
3. **Configuración de red** para tu infraestructura
4. **Logging** a servicios centralizados
5. **Health checks** más estrictos
6. **Resource limits** apropiados

### Variables de Producción

```yaml
# Ejemplo para producción
environment:
  DB_URL: jdbc:postgresql://prod-db:5432/learning_prod
  KAFKA_BOOTSTRAP_SERVERS: prod-kafka:9092
  JWT_SECRET: ${JWT_SECRET}  # Desde secret manager
  SPRING_PROFILES_ACTIVE: prod
```

---

¡Listo! Tu Learning Service está completamente dockerizado y listo para desarrollo y producción. 🎉
