# MicroLogistics System

A distributed microservices-based logistics management system implementing cloud-native patterns.

## Table of Contents
- [System Overview](#system-overview)
- [Architecture](#architecture)
- [Services](#services)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Monitoring & Observability](#monitoring--observability)
- [Event-Driven Architecture](#event-driven-architecture)
- [Security](#security)
- [Resilience Patterns](#resilience-patterns)
- [Contributing](#contributing)
- [License](#license)

## System Overview

Key Features:
- Service Discovery with Eureka
- Centralized Configuration
- API Gateway with Intelligent Routing
- Event-Driven Communication
- Containerized Deployment Ready
- Comprehensive Monitoring
- Automated Resilience Patterns

Technology Stack:
- Java 17
- Spring Boot 3.x
- Spring Cloud
- PostgreSQL
- Apache Kafka
- Prometheus & Grafana
- Docker & Kubernetes

## Architecture

```
graph TD
    A[Client] --> B[API Gateway]
    B --> C[Item Service]
    B --> D[Routing Service]
    B --> E[Container Service]
    B --> F[Metrics Service]
    C -->|Kafka Events| D
    D -->|Kafka Events| E
    G[Service Registry] --> H[Config Server]
    C -.-> G
    D -.-> G
    E -.-> G
    F -.-> G
    H --> I[Git Repository]
```

![Untitled diagram-2025-04-21-075440](https://github.com/user-attachments/assets/f7455466-74ea-4204-973a-932c95d97c3c)


## Services

### Core Infrastructure
1. **Service Registry** (port: 8761)
   - Eureka-based service discovery
   - Secure endpoints with basic auth
   - Health monitoring

2. **Config Server** (port: 8888)
   - Git-backed configuration
   - Profile-based configurations
   - Encryption support

3. **API Gateway** (port: 8080)
   - Dynamic routing
   - Authentication filters
   - Circuit breakers
   - Rate limiting

### Business Services
4. **Item Registration Service** (port: 8081)
   - CRUD operations for logistics items
   - Tracking ID generation
   - Kafka event publishing
   - Priority handling

5. **Routing Service** (port: 8082)
   - Optimal path calculation
   - Route progress tracking
   - Kafka event consumption
   - Multi-step routing

6. **Common Library**
   - Shared DTOs
   - Custom exceptions
   - Event models
   - Validation logic

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Kafka 3.0+
- Docker (optional)

### Installation

1. Clone the repository:
```
git clone https://github.com/your-org/micrologistics.git
```

2. Build the project:
```
mvn clean install
```

3. Start infrastructure:
```
docker-compose -f docker-compose-infra.yml up
```

4. Run services in order:
1. Service Registry
2. Config Server
3. API Gateway
4. Item Service
5. Routing Service

## Configuration

Key configuration files:
- `service-registry/application.yml`
- `config-server/application.yml`
- `api-gateway/application.yml`
- `item-service/application.yml`

Environment variables:
```
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/items
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## API Documentation

Access via API Gateway:
```
http://localhost:8080/swagger-ui.html
```

Sample endpoints:
- `POST /api/items` - Register new item
- `GET /api/routes/{id}` - Get route details
- `PUT /api/containers/{id}/status` - Update container status

## Testing

Run all tests:
```
mvn test
```

Test types:
1. Unit tests (JUnit 5)
2. Integration tests (Testcontainers)
3. API tests (MockMvc)
4. Kafka integration tests

Test coverage:
```
mvn jacoco:report
```

## Deployment

Docker build:
```
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=micrologistics/
```

Kubernetes deployment:
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: item-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: item-service
        image: micrologistics/item-service:latest
        ports:
        - containerPort: 8081
```

## Monitoring & Observability

Endpoints:
- `/actuator/health` - Service health
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus endpoint

Grafana Dashboards:
1. Service Health Overview
2. API Gateway Metrics
3. Kafka Message Rates

## Event-Driven Architecture

Kafka Topics:
- `item-registered` - New item notifications
- `route-calculated` - Route updates
- `container-status` - Container changes

Event Flow:
```
sequenceDiagram
    Item Service->>Kafka: ItemRegisteredEvent
    Kafka->>Routing Service: Consume event
    Routing Service->>Kafka: RouteCalculatedEvent
    Kafka->>Container Service: Consume event
```

## Security

Implemented Features:
- HTTPS/TLS termination
- Basic authentication
- Role-based access control
- Sensitive data encryption
- CSRF protection

Security Matrix:

| Service         | Authentication | Roles       |
|-----------------|----------------|-------------|
| Service Registry| Basic Auth     | ADMIN       |
| Config Server   | Basic Auth     | ADMIN       |
| API Gateway     | JWT            | USER, ADMIN |

## Resilience Patterns

1. **Circuit Breakers**
   - Fail-fast for downstream services
   - Fallback responses

2. **Retry Mechanisms**
   - Configurable retry policies
   - Exponential backoff

3. **Rate Limiting**
   - Service-level throttling
   - Priority-based quotas

4. **Bulkheads**
   - Thread pool isolation
   - Service instance limits

## Contributing

1. Fork the repository
2. Create feature branch
3. Submit PR with:
   - Tests
   - Documentation
   - CHANGELOG update
