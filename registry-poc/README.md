# Registry PoC - Nomulus Technical Patterns Demo

This proof of concept extracts and demonstrates the key technical ideas from the Google Nomulus domain registry system in a simplified, educational format.

## Extracted Technical Patterns

### 1. Flow-Based Command Processing
- Clean command pattern implementation for EPP operations
- Separation of concerns between command parsing and execution
- Transaction scoping and error handling

### 2. Domain-Driven Design
- Rich domain models with clear boundaries
- Entity lifecycle management
- Value objects and immutable patterns

### 3. Dependency Injection Architecture
- Component-based design
- Scoped injection for request handling
- Clean separation of infrastructure and domain logic

### 4. Multi-Tenant Configuration
- Configurable TLD support
- Environment-specific settings
- Premium domain pricing logic

### 5. Event-Driven Architecture
- Domain events for audit trails
- Billing event generation
- State change tracking

## Project Structure

```
registry-poc/
├── src/main/java/
│   ├── flow/              # Flow-based command processing
│   ├── model/             # Domain entities and value objects  
│   ├── config/            # Configuration management
│   ├── injection/         # Dependency injection setup
│   ├── api/              # REST API endpoints
│   └── events/           # Event handling system
├── src/test/java/        # Comprehensive tests
├── build.gradle          # Build configuration
└── README.md            # This file
```

## Key Demonstrations

1. **EPP Command Flow**: Simplified domain check/create operations
2. **Entity Modeling**: Domain, Host, and Contact entities with relationships
3. **Injection Framework**: Lightweight DI container mimicking Dagger patterns
4. **Configuration System**: Multi-environment TLD configuration
5. **Event System**: Domain lifecycle events and history tracking
6. **API Layer**: REST endpoints for registry operations
7. **Testing Patterns**: Unit and integration test examples

## Running the PoC

```bash
cd registry-poc
./gradlew build
./gradlew run
```

Access the demo API at: http://localhost:8080

## Educational Value

This PoC demonstrates enterprise-level architectural patterns:
- Clean Architecture principles
- SOLID design principles  
- Domain-Driven Design patterns
- Microservice-ready structure
- Test-driven development approach
- Configuration-driven flexibility