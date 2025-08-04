package events;

import model.Domain;
import java.time.Instant;

/**
 * Domain events for tracking registry operations.
 * 
 * Demonstrates event-driven architecture patterns from Nomulus:
 * - Immutable event objects
 * - Rich event data for analytics
 * - Audit trail generation
 * - Business event modeling
 */
public abstract class DomainEvent {
    
    private final String eventType;
    private final Instant timestamp;
    private final String domainName;
    private final String registrarId;
    
    protected DomainEvent(String eventType, String domainName, String registrarId) {
        this.eventType = eventType;
        this.domainName = domainName;
        this.registrarId = registrarId;
        this.timestamp = Instant.now();
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public String getRegistrarId() {
        return registrarId;
    }
    
    /**
     * Event fired when a domain availability check is performed
     */
    public static class DomainChecked extends DomainEvent {
        private final boolean available;
        
        public DomainChecked(String domainName, String registrarId, boolean available) {
            super("DOMAIN_CHECKED", domainName, registrarId);
            this.available = available;
        }
        
        public boolean isAvailable() {
            return available;
        }
    }
    
    /**
     * Event fired when a domain is successfully created
     */
    public static class DomainCreated extends DomainEvent {
        private final Domain domain;
        private final double price;
        
        public DomainCreated(Domain domain, String registrarId, double price) {
            super("DOMAIN_CREATED", domain.getDomainName(), registrarId);
            this.domain = domain;
            this.price = price;
        }
        
        public Domain getDomain() {
            return domain;
        }
        
        public double getPrice() {
            return price;
        }
    }
    
    /**
     * Event fired when a domain is renewed
     */
    public static class DomainRenewed extends DomainEvent {
        private final Instant oldExpirationTime;
        private final Instant newExpirationTime;
        private final int renewalPeriod;
        private final double price;
        
        public DomainRenewed(String domainName, String registrarId, 
                           Instant oldExpirationTime, Instant newExpirationTime,
                           int renewalPeriod, double price) {
            super("DOMAIN_RENEWED", domainName, registrarId);
            this.oldExpirationTime = oldExpirationTime;
            this.newExpirationTime = newExpirationTime;
            this.renewalPeriod = renewalPeriod;
            this.price = price;
        }
        
        public Instant getOldExpirationTime() {
            return oldExpirationTime;
        }
        
        public Instant getNewExpirationTime() {
            return newExpirationTime;
        }
        
        public int getRenewalPeriod() {
            return renewalPeriod;
        }
        
        public double getPrice() {
            return price;
        }
    }
    
    /**
     * Event fired when a domain is deleted
     */
    public static class DomainDeleted extends DomainEvent {
        private final String reason;
        
        public DomainDeleted(String domainName, String registrarId, String reason) {
            super("DOMAIN_DELETED", domainName, registrarId);
            this.reason = reason;
        }
        
        public String getReason() {
            return reason;
        }
    }
    
    /**
     * Event fired when a domain status changes
     */
    public static class DomainStatusChanged extends DomainEvent {
        private final Domain.Status oldStatus;
        private final Domain.Status newStatus;
        private final String reason;
        
        public DomainStatusChanged(String domainName, String registrarId,
                                 Domain.Status oldStatus, Domain.Status newStatus, String reason) {
            super("DOMAIN_STATUS_CHANGED", domainName, registrarId);
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.reason = reason;
        }
        
        public Domain.Status getOldStatus() {
            return oldStatus;
        }
        
        public Domain.Status getNewStatus() {
            return newStatus;
        }
        
        public String getReason() {
            return reason;
        }
    }
}