package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Tests for Domain entity demonstrating domain model testing patterns.
 */
class DomainTest {
    
    @Test
    void testDomainCreation() {
        // Given
        Contact registrant = Contact.newBuilder()
            .setContactId("CONTACT-1")
            .setName("John Doe")
            .setEmail("john@example.com")
            .build();
        
        // When
        Domain domain = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .build();
        
        // Then
        assertEquals("example.com", domain.getDomainName());
        assertEquals("com", domain.getTld());
        assertEquals("REG-001", domain.getRegistrarId());
        assertEquals(registrant, domain.getRegistrant());
        assertEquals(Domain.Status.PENDING_CREATE, domain.getStatus());
        assertNotNull(domain.getCreationTime());
    }
    
    @Test
    void testDomainIsActive() {
        // Given
        Contact registrant = createTestContact();
        Instant futureExpiration = Instant.now().plus(365, ChronoUnit.DAYS);
        
        Domain domain = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .setStatus(Domain.Status.ACTIVE)
            .setExpirationTime(futureExpiration)
            .build();
        
        // When/Then
        assertTrue(domain.isActive());
    }
    
    @Test
    void testDomainIsNotActiveWhenExpired() {
        // Given
        Contact registrant = createTestContact();
        Instant pastExpiration = Instant.now().minus(30, ChronoUnit.DAYS);
        
        Domain domain = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .setStatus(Domain.Status.ACTIVE)
            .setExpirationTime(pastExpiration)
            .build();
        
        // When/Then
        assertFalse(domain.isActive());
    }
    
    @Test
    void testDomainCanBeRenewed() {
        // Given
        Contact registrant = createTestContact();
        
        Domain activeDomain = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .setStatus(Domain.Status.ACTIVE)
            .build();
        
        Domain expiredDomain = Domain.newBuilder()
            .setDomainName("expired.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .setStatus(Domain.Status.EXPIRED)
            .build();
        
        // When/Then
        assertTrue(activeDomain.canBeRenewed());
        assertTrue(expiredDomain.canBeRenewed());
    }
    
    @Test
    void testDomainRenewal() {
        // Given
        Contact registrant = createTestContact();
        Instant originalExpiration = Instant.now().plus(30, ChronoUnit.DAYS);
        
        Domain domain = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .setExpirationTime(originalExpiration)
            .build();
        
        // When
        Instant newExpiration = Instant.now().plus(395, ChronoUnit.DAYS);
        Domain renewedDomain = domain.withNewExpirationTime(newExpiration);
        
        // Then
        assertEquals(newExpiration, renewedDomain.getExpirationTime());
        assertNotEquals(originalExpiration, renewedDomain.getExpirationTime());
        assertTrue(renewedDomain.getLastUpdateTime().isAfter(domain.getLastUpdateTime()));
    }
    
    @Test
    void testDomainBuilderValidation() {
        // When/Then - Missing domain name
        assertThrows(NullPointerException.class, () -> {
            Domain.newBuilder()
                .setRegistrarId("REG-001")
                .setRegistrant(createTestContact())
                .build();
        });
        
        // When/Then - Missing registrar ID
        assertThrows(NullPointerException.class, () -> {
            Domain.newBuilder()
                .setDomainName("example.com")
                .setRegistrant(createTestContact())
                .build();
        });
        
        // When/Then - Missing registrant
        assertThrows(NullPointerException.class, () -> {
            Domain.newBuilder()
                .setDomainName("example.com")
                .setRegistrarId("REG-001")
                .build();
        });
    }
    
    @Test
    void testDomainEquality() {
        // Given
        Contact registrant = createTestContact();
        
        Domain domain1 = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .build();
        
        Domain domain2 = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-002") // Different registrar
            .setRegistrant(registrant)
            .build();
        
        // When/Then
        assertEquals(domain1, domain2); // Equality based on domain name only
        assertEquals(domain1.hashCode(), domain2.hashCode());
    }
    
    @Test
    void testTldExtraction() {
        // Given
        Contact registrant = createTestContact();
        
        // When
        Domain domain = Domain.newBuilder()
            .setDomainName("subdomain.example.org")
            .setRegistrarId("REG-001")
            .setRegistrant(registrant)
            .build();
        
        // Then
        assertEquals("org", domain.getTld());
    }
    
    private Contact createTestContact() {
        return Contact.newBuilder()
            .setContactId("TEST-CONTACT")
            .setName("Test User")
            .setEmail("test@example.com")
            .build();
    }
}