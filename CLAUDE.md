# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Quarkus MicroProfile project structured as a multi-module Maven application using the BCE (Boundary-Control-Entity) architectural pattern. The application provides a REST API to control PWM (Pulse Width Modulation) outputs on a Raspberry Pi for fan control.

## Architecture: BCE Pattern

BCE (Boundary-Control-Entity) organizes code by domain responsibility:

- **Boundary**: External interaction layer (JAX-RS resources, REST clients). Located in `boundary` packages.
- **Control**: Procedural business logic. Located in `control` packages.
- **Entity**: Domain objects and data structures. Located in `entity` packages.

Example structure: `be.codesolutions.pwm.{boundary,control,entity}`

Learn more at: https://bce.design

## Module Structure

### service (Main Application)
- Location: `service/`
- The primary Quarkus application module
- Contains business logic organized by domain using BCE pattern
- Uses Java 25
- Dependencies: Quarkus REST, CDI (Arc), SmallRye Health, Pi4J v3

### service-st (System Tests)
- Location: `service-st/`
- Standalone black-box testing module
- Tests the deployed service via REST client
- Uses Java 21
- Runs against local or remote environments

## Common Commands

### Build the project
```bash
# From root (builds all modules)
mvn clean package

# Build service only
cd service && mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

### Run in Development Mode
```bash
cd service
mvn quarkus:dev
```
Service will be available at http://localhost:8080

### Run in Production
```bash
cd service
java -jar target/quarkus-app/quarkus-run.jar
```

### Run Tests
```bash
# Run service module unit tests
cd service && mvn test

# Run system tests (requires service to be running on localhost:8080)
cd service-st && mvn clean test-compile failsafe:integration-test

# Run system tests against remote environment
export BASE_URI=https://deployed.com
cd service-st && mvn clean test-compile failsafe:integration-test
```

### Build Native Image
```bash
cd service
mvn clean package -Pnative
```

### Health Checks
- Liveness: http://localhost:8080/q/health/live
- Readiness: http://localhost:8080/q/health/ready
- Combined: http://localhost:8080/q/health

## Business Domains

### PWM (Pulse Width Modulation) Control
Located in `be.codesolutions.pwm.*` packages, following BCE pattern:

- **Entity** (`be.codesolutions.pwm.entity`): Domain objects
  - `PwmOutput`: Represents a single PWM output with id, pin, and value (0.0-1.0)

- **Control** (`be.codesolutions.pwm.control`): Business logic and hardware interaction
  - `PwmOutputController`: Manages Pi4J hardware interaction for PWM outputs
  - Handles Pi4J context initialization and PWM device configuration (1500Hz frequency)

- **Boundary** (`be.codesolutions.pwm.boundary`): REST API
  - `PwmResource`: Endpoints for controlling PWM outputs
  - GET `/pwm` - List all PWM outputs
  - GET `/pwm/{id}` - Get specific output status
  - PUT `/pwm/{id}?value=0.5` - Set PWM value (0.0-1.0)

**Hardware Configuration**:
- 5 PWM outputs mapped in `application.properties`:
  - zk → GPIO 13
  - mb → GPIO 16
  - lk → GPIO 19
  - kk → GPIO 20
  - bk → GPIO 26
- PWM frequency: 1500Hz
- Active high configuration

## Key Configuration

- Service runs on port 8080 by default (Quarkus default)
- Application configuration: `service/src/main/resources/application.properties`
- PWM output mappings configured with `pwm.outputs.<id>.pin` properties
- System tests use BASE_URI environment variable (defaults to http://localhost:8080)

## Development Workflow

1. The service module must be built and running before executing system tests
2. Use `mvnw` wrapper scripts for consistent Maven version across environments
3. Health endpoints are implemented using MicroProfile Health API
4. CDI constructor injection is used throughout (see PwmResource boundary layer)
5. Configuration injection via `@ConfigProperty` (see PwmOutputController control layer)

## CI/CD Pipeline

GitHub Actions workflow (`.github/workflows/maven-verify.yml`):
1. Builds service module
2. Starts service in background
3. Waits for health check readiness
4. Runs service unit tests
5. Runs system tests
6. Stops service

Requires: Java 25 (Oracle distribution), Maven