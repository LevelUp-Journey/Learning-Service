#!/bin/bash

# Docker Run Script for Learning Service
# This script provides easy commands to run the Learning Service with Docker

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose.yml"
SERVICE_NAME="learning-service"

# Function to show usage
show_usage() {
    echo -e "${BLUE}Learning Service Docker Management Script${NC}"
    echo ""
    echo -e "${CYAN}Usage:${NC}"
    echo -e "  $0 ${GREEN}build${NC}          - Build the Docker image"
    echo -e "  $0 ${GREEN}up${NC}             - Start all services"
    echo -e "  $0 ${GREEN}down${NC}           - Stop all services"
    echo -e "  $0 ${GREEN}restart${NC}        - Restart all services"
    echo -e "  $0 ${GREEN}logs${NC}           - Show logs from all services"
    echo -e "  $0 ${GREEN}logs-app${NC}       - Show logs from learning service only"
    echo -e "  $0 ${GREEN}status${NC}         - Show status of all services"
    echo -e "  $0 ${GREEN}clean${NC}          - Remove containers and volumes"
    echo -e "  $0 ${GREEN}shell${NC}          - Open shell in learning service container"
    echo -e "  $0 ${GREEN}test${NC}           - Run health check tests"
    echo ""
    echo -e "${CYAN}Examples:${NC}"
    echo -e "  $0 build && $0 up    # Build and start"
    echo -e "  $0 logs-app          # Monitor application logs"
    echo -e "  $0 test              # Check if everything is working"
    echo ""
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}Error: Docker is not running${NC}"
        exit 1
    fi
}

# Function to check if docker-compose file exists
check_compose() {
    if [ ! -f "$COMPOSE_FILE" ]; then
        echo -e "${RED}Error: $COMPOSE_FILE not found${NC}"
        exit 1
    fi
}

# Function to build the image
build_image() {
    echo -e "${BLUE}Building Docker image...${NC}"
    docker-compose -f $COMPOSE_FILE build --no-cache $SERVICE_NAME
    echo -e "${GREEN}✅ Build completed${NC}"
}

# Function to start services
start_services() {
    echo -e "${BLUE}Starting services...${NC}"
    docker-compose -f $COMPOSE_FILE up -d
    echo -e "${GREEN}✅ Services started${NC}"
    echo -e "${YELLOW}Waiting for services to be ready...${NC}"
    sleep 10
    show_status
}

# Function to stop services
stop_services() {
    echo -e "${BLUE}Stopping services...${NC}"
    docker-compose -f $COMPOSE_FILE down
    echo -e "${GREEN}✅ Services stopped${NC}"
}

# Function to restart services
restart_services() {
    echo -e "${BLUE}Restarting services...${NC}"
    docker-compose -f $COMPOSE_FILE restart
    echo -e "${GREEN}✅ Services restarted${NC}"
}

# Function to show logs
show_logs() {
    echo -e "${BLUE}Showing logs from all services...${NC}"
    docker-compose -f $COMPOSE_FILE logs -f
}

# Function to show app logs only
show_app_logs() {
    echo -e "${BLUE}Showing logs from learning service...${NC}"
    docker-compose -f $COMPOSE_FILE logs -f $SERVICE_NAME
}

# Function to show status
show_status() {
    echo -e "${BLUE}Service Status:${NC}"
    docker-compose -f $COMPOSE_FILE ps
    echo ""
    echo -e "${CYAN}Health Checks:${NC}"

    # Check PostgreSQL
    if docker-compose -f $COMPOSE_FILE exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
        echo -e "  🗄️  PostgreSQL: ${GREEN}✅ Healthy${NC}"
    else
        echo -e "  🗄️  PostgreSQL: ${RED}❌ Unhealthy${NC}"
    fi

    # Check Kafka
    if docker-compose -f $COMPOSE_FILE exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        echo -e "  📨 Kafka: ${GREEN}✅ Healthy${NC}"
    else
        echo -e "  📨 Kafka: ${RED}❌ Unhealthy${NC}"
    fi

    # Check Eureka
    if curl -f http://localhost:8761/actuator/health > /dev/null 2>&1; then
        echo -e "  🌐 Eureka: ${GREEN}✅ Healthy${NC}"
    else
        echo -e "  🌐 Eureka: ${RED}❌ Unhealthy${NC}"
    fi

    # Check Learning Service
    if curl -f http://localhost:8085/actuator/health > /dev/null 2>&1; then
        echo -e "  🚀 Learning Service: ${GREEN}✅ Healthy${NC}"
        echo -e "    📖 Swagger UI: http://localhost:8085/swagger-ui/index.html"
    else
        echo -e "  🚀 Learning Service: ${RED}❌ Unhealthy${NC}"
    fi
}

# Function to clean up
cleanup() {
    echo -e "${YELLOW}This will remove all containers and volumes. Are you sure? (y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo -e "${BLUE}Cleaning up...${NC}"
        docker-compose -f $COMPOSE_FILE down -v --remove-orphans
        docker system prune -f
        echo -e "${GREEN}✅ Cleanup completed${NC}"
    else
        echo -e "${BLUE}Cleanup cancelled${NC}"
    fi
}

# Function to open shell
open_shell() {
    echo -e "${BLUE}Opening shell in learning service container...${NC}"
    docker-compose -f $COMPOSE_FILE exec $SERVICE_NAME /bin/bash
}

# Function to run tests
run_tests() {
    echo -e "${BLUE}Running health checks...${NC}"

    # Wait for services to be ready
    echo -e "${YELLOW}Waiting for services to be ready...${NC}"
    sleep 30

    local failed=0

    # Test PostgreSQL connection
    echo -e "${CYAN}Testing PostgreSQL connection...${NC}"
    if docker-compose -f $COMPOSE_FILE exec -T postgres pg_isready -U postgres; then
        echo -e "  ✅ PostgreSQL connection successful"
    else
        echo -e "  ❌ PostgreSQL connection failed"
        ((failed++))
    fi

    # Test Kafka connection
    echo -e "${CYAN}Testing Kafka connection...${NC}"
    if docker-compose -f $COMPOSE_FILE exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
        echo -e "  ✅ Kafka connection successful"
    else
        echo -e "  ❌ Kafka connection failed"
        ((failed++))
    fi

    # Test Eureka
    echo -e "${CYAN}Testing Eureka...${NC}"
    if curl -f http://localhost:8761/actuator/health > /dev/null 2>&1; then
        echo -e "  ✅ Eureka is healthy"
    else
        echo -e "  ❌ Eureka is unhealthy"
        ((failed++))
    fi

    # Test Learning Service
    echo -e "${CYAN}Testing Learning Service...${NC}"
    if curl -f http://localhost:8085/actuator/health > /dev/null 2>&1; then
        echo -e "  ✅ Learning Service is healthy"
        echo -e "  📖 Swagger UI: http://localhost:8085/swagger-ui/index.html"
    else
        echo -e "  ❌ Learning Service is unhealthy"
        ((failed++))
    fi

    echo ""
    if [ $failed -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed! Services are ready.${NC}"
        return 0
    else
        echo -e "${RED}❌ $failed test(s) failed. Check the services.${NC}"
        return 1
    fi
}

# Main script logic
check_docker
check_compose

case "${1:-help}" in
    build)
        build_image
        ;;
    up)
        start_services
        ;;
    down)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs
        ;;
    logs-app)
        show_app_logs
        ;;
    status)
        show_status
        ;;
    clean)
        cleanup
        ;;
    shell)
        open_shell
        ;;
    test)
        run_tests
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        echo -e "${RED}Unknown command: $1${NC}"
        echo ""
        show_usage
        exit 1
        ;;
esac

