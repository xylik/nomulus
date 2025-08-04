# Technical Patterns Extracted from Nomulus

This document details the key technical patterns and architectural ideas extracted from the Google Nomulus domain registry system and implemented in this proof of concept.

## 1. Flow-Based Command Processing Architecture

**Extracted from**: `core/src/main/java/google/registry/flows/`

**Key Pattern**: The Flow interface provides a clean abstraction for processing registry operations.

```java
public interface Flow<TInput, TOutput> {
    TOutput run() throws FlowException;
    default void validate() throws FlowException { }
}
```

**Benefits**:
- Clear separation of concerns
- Consistent error handling
- Testable units of work
- Transaction boundary definition
- Reusable operation patterns

**Implementation**: `DomainCheckFlow` and `DomainCreateFlow` demonstrate this pattern with validation, business logic execution, and event publishing.

## 2. Rich Domain Model with Builder Pattern

**Extracted from**: `core/src/main/java/google/registry/model/domain/Domain.java`

**Key Pattern**: Immutable entities with builder pattern and business logic encapsulation.

```java
public final class Domain {
    // Immutable fields
    private final String domainName;
    private final Instant expirationTime;
    
    // Business logic methods  
    public boolean isActive() { ... }
    public boolean canBeRenewed() { ... }
    
    // Builder pattern for construction
    public static Builder newBuilder() { ... }
}
```

**Benefits**:
- Immutability ensures thread safety
- Rich domain logic co-located with data
- Builder pattern for complex object construction
- Clear validation rules
- Value object composition

## 3. Dependency Injection with Guice

**Extracted from**: Nomulus's extensive use of Google Guice for dependency injection

**Key Pattern**: Component-based architecture with clean dependency management.

```java
@Module
public class RegistryModule extends AbstractModule {
    @Provides @Singleton
    public TldConfiguration provideTldConfiguration() { ... }
}
```

**Benefits**:
- Testability through mock injection
- Loose coupling between components
- Singleton lifecycle management
- Provider methods for complex object creation
- Clean separation of concerns

## 4. Multi-Tenant TLD Configuration

**Extracted from**: `core/src/main/java/google/registry/tldconfig/`

**Key Pattern**: Configurable TLD support with different pricing and policies.

```java
public class TldConfiguration {
    private final Map<String, TldConfig> tldConfigs;
    
    public boolean isSupportedTld(String tld) { ... }
    public double getBasePriceForTld(String tld) { ... }
}
```

**Benefits**:
- Multi-tenant registry support
- Configurable pricing models
- Environment-specific settings
- Premium domain handling
- Extensible TLD policies

## 5. Event-Driven Architecture

**Extracted from**: Nomulus's billing and history event systems

**Key Pattern**: Immutable domain events for audit trails and analytics.

```java
public abstract class DomainEvent {
    private final String eventType;
    private final Instant timestamp;
    
    public static class DomainCreated extends DomainEvent { ... }
}
```

**Benefits**:
- Audit trail generation
- Analytics data collection
- Asynchronous processing
- Event sourcing patterns
- Integration points for external systems

## 6. Repository Pattern for Persistence

**Extracted from**: Nomulus's database access patterns

**Key Pattern**: Abstract repository interfaces for clean data access.

```java
public interface DomainRepository {
    boolean exists(String domainName);
    void save(Domain domain);
    Domain findByName(String domainName);
}
```

**Benefits**:
- Database abstraction
- Testability with mock repositories
- Clean architecture boundaries
- Technology independence
- Easy unit testing

## 7. Structured Exception Handling

**Extracted from**: `core/src/main/java/google/registry/flows/EppException.java`

**Key Pattern**: Typed exceptions with error codes for structured error handling.

```java
public class FlowException extends Exception {
    public enum Type { VALIDATION_ERROR, DOMAIN_NOT_AVAILABLE, ... }
    private final Type type;
    private final String errorCode;
}
```

**Benefits**:
- Structured error responses
- Consistent error handling
- Machine-readable error codes
- Proper HTTP status mapping
- Debugging and monitoring support

## 8. RESTful API Design

**Extracted from**: Nomulus's web service patterns

**Key Pattern**: Clean REST API with JSON serialization and proper HTTP semantics.

```java
app.post("/api/domain/check", this::checkDomain);
app.post("/api/domain/create", this::createDomain);
```

**Benefits**:
- Standard HTTP semantics
- JSON request/response format
- Clear resource modeling
- Proper status codes
- API versioning support

## 9. Comprehensive Testing Strategy

**Extracted from**: Nomulus's extensive test suite patterns

**Key Pattern**: Multiple testing levels with mocks, unit tests, and integration tests.

```java
@Test
void testDomainAvailable() throws FlowException {
    when(mockRepository.exists("example.com")).thenReturn(false);
    // Test execution and verification
}
```

**Benefits**:
- High test coverage
- Mock-based unit testing
- Integration testing patterns
- Business logic verification
- Regression prevention

## 10. Pricing and Premium Domain Logic

**Extracted from**: Nomulus's premium pricing system

**Key Pattern**: Configurable pricing with premium domain detection.

```java
private double calculatePrice(String domainName, String tld, int years) {
    double basePrice = tldConfig.getBasePriceForTld(tld);
    if (isPremiumDomain(domainName)) {
        basePrice *= 10; // Premium multiplier
    }
    return basePrice * years;
}
```

**Benefits**:
- Dynamic pricing models
- Premium domain detection
- Revenue optimization
- Market-based pricing
- Configurable business rules

## Architecture Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   Flow Layer    │    │  Domain Model   │
│                 │    │                 │    │                 │
│ • JSON Endpoints│◄──►│ • DomainCheck   │◄──►│ • Domain        │
│ • HTTP Handlers │    │ • DomainCreate  │    │ • Contact       │
│ • Error Mapping │    │ • Validation    │    │ • Host          │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Configuration  │    │  Event System   │    │  Repository     │
│                 │    │                 │    │                 │
│ • TLD Config    │    │ • Domain Events │    │ • Data Access   │
│ • Pricing Rules │    │ • Event Bus     │    │ • Persistence   │
│ • Environment   │    │ • Audit Trail   │    │ • Transactions  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Running the Demo

The proof of concept demonstrates these patterns in a working registry system:

1. **Start the server**: `./gradlew runApp`
2. **Check domain availability**: `POST /api/domain/check`
3. **Create domain registration**: `POST /api/domain/create`
4. **View configuration**: `GET /api/config`
5. **List all domains**: `GET /api/domains`

This implementation showcases enterprise-level architectural patterns that can be applied to domain registries, content management systems, e-commerce platforms, and other complex business applications.