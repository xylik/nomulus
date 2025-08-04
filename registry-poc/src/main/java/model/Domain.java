package model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * Domain entity representing a registered domain name.
 * 
 * Simplified version of Nomulus's Domain entity, demonstrating:
 * - Immutable entity design patterns
 * - Rich domain model with business logic
 * - Value object composition
 * - Audit trail fields
 */
public final class Domain {
    
    public enum Status {
        ACTIVE,
        PENDING_CREATE,
        PENDING_DELETE,
        EXPIRED,
        SUSPENDED
    }
    
    private final String domainName;
    private final String tld;
    private final String registrarId;
    private final Instant creationTime;
    private final Instant expirationTime;
    private final Instant lastUpdateTime;
    private final Status status;
    private final Contact registrant;
    private final Set<Contact> contacts;
    private final Set<Host> nameservers;
    
    private Domain(Builder builder) {
        this.domainName = builder.domainName;
        this.tld = builder.tld;
        this.registrarId = builder.registrarId;
        this.creationTime = builder.creationTime;
        this.expirationTime = builder.expirationTime;
        this.lastUpdateTime = builder.lastUpdateTime;
        this.status = builder.status;
        this.registrant = builder.registrant;
        this.contacts = ImmutableSet.copyOf(builder.contacts);
        this.nameservers = ImmutableSet.copyOf(builder.nameservers);
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public String getTld() {
        return tld;
    }
    
    public String getRegistrarId() {
        return registrarId;
    }
    
    public Instant getCreationTime() {
        return creationTime;
    }
    
    public Instant getExpirationTime() {
        return expirationTime;
    }
    
    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public Contact getRegistrant() {
        return registrant;
    }
    
    public Set<Contact> getContacts() {
        return contacts;
    }
    
    public Set<Host> getNameservers() {
        return nameservers;
    }
    
    /**
     * Business logic: Check if domain is currently active and not expired
     */
    public boolean isActive() {
        return status == Status.ACTIVE && 
               (expirationTime == null || expirationTime.isAfter(Instant.now()));
    }
    
    /**
     * Business logic: Check if domain can be renewed
     */
    public boolean canBeRenewed() {
        return status == Status.ACTIVE || status == Status.EXPIRED;
    }
    
    /**
     * Create a new Domain with updated expiration time (for renewal)
     */
    public Domain withNewExpirationTime(Instant newExpiration) {
        return toBuilder()
            .setExpirationTime(newExpiration)
            .setLastUpdateTime(Instant.now())
            .build();
    }
    
    public Builder toBuilder() {
        return new Builder()
            .setDomainName(domainName)
            .setTld(tld)
            .setRegistrarId(registrarId)
            .setCreationTime(creationTime)
            .setExpirationTime(expirationTime)
            .setLastUpdateTime(lastUpdateTime)
            .setStatus(status)
            .setRegistrant(registrant)
            .setContacts(contacts)
            .setNameservers(nameservers);
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domain domain = (Domain) o;
        return Objects.equals(domainName, domain.domainName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(domainName);
    }
    
    @Override
    public String toString() {
        return "Domain{" +
                "domainName='" + domainName + '\'' +
                ", tld='" + tld + '\'' +
                ", status=" + status +
                '}';
    }
    
    public static final class Builder {
        private String domainName;
        private String tld;
        private String registrarId;
        private Instant creationTime;
        private Instant expirationTime;
        private Instant lastUpdateTime;
        private Status status = Status.PENDING_CREATE;
        private Contact registrant;
        private Set<Contact> contacts = ImmutableSet.of();
        private Set<Host> nameservers = ImmutableSet.of();
        
        public Builder setDomainName(String domainName) {
            this.domainName = domainName;
            // Extract TLD from domain name
            int lastDot = domainName.lastIndexOf('.');
            if (lastDot > 0) {
                this.tld = domainName.substring(lastDot + 1);
            }
            return this;
        }
        
        public Builder setTld(String tld) {
            this.tld = tld;
            return this;
        }
        
        public Builder setRegistrarId(String registrarId) {
            this.registrarId = registrarId;
            return this;
        }
        
        public Builder setCreationTime(Instant creationTime) {
            this.creationTime = creationTime;
            return this;
        }
        
        public Builder setExpirationTime(Instant expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }
        
        public Builder setLastUpdateTime(Instant lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }
        
        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }
        
        public Builder setRegistrant(Contact registrant) {
            this.registrant = registrant;
            return this;
        }
        
        public Builder setContacts(Set<Contact> contacts) {
            this.contacts = ImmutableSet.copyOf(contacts);
            return this;
        }
        
        public Builder setNameservers(Set<Host> nameservers) {
            this.nameservers = ImmutableSet.copyOf(nameservers);
            return this;
        }
        
        public Domain build() {
            Objects.requireNonNull(domainName, "Domain name is required");
            Objects.requireNonNull(registrarId, "Registrar ID is required");
            Objects.requireNonNull(registrant, "Registrant contact is required");
            
            if (creationTime == null) {
                creationTime = Instant.now();
            }
            if (lastUpdateTime == null) {
                lastUpdateTime = creationTime;
            }
            
            return new Domain(this);
        }
    }
}