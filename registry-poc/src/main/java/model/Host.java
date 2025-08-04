package model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * Host entity representing nameserver hosts.
 * 
 * Simplified version inspired by Nomulus's Host model.
 */
public final class Host {
    
    private final String hostName;
    private final String tld;
    private final Set<InetAddress> addresses;
    private final String registrarId;
    
    private Host(Builder builder) {
        this.hostName = builder.hostName;
        this.tld = builder.tld;
        this.addresses = ImmutableSet.copyOf(builder.addresses);
        this.registrarId = builder.registrarId;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public String getTld() {
        return tld;
    }
    
    public Set<InetAddress> getAddresses() {
        return addresses;
    }
    
    public String getRegistrarId() {
        return registrarId;
    }
    
    /**
     * Business logic: Check if this is an in-bailiwick host
     * (host is within the same TLD being managed)
     */
    public boolean isInBailiwick(String domainTld) {
        return this.tld != null && this.tld.equals(domainTld);
    }
    
    /**
     * Business logic: Check if host has valid IP addresses
     */
    public boolean hasValidAddresses() {
        return !addresses.isEmpty();
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return Objects.equals(hostName, host.hostName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hostName);
    }
    
    @Override
    public String toString() {
        return "Host{" +
                "hostName='" + hostName + '\'' +
                ", addresses=" + addresses.size() + " IPs" +
                '}';
    }
    
    public static final class Builder {
        private String hostName;
        private String tld;
        private Set<InetAddress> addresses = ImmutableSet.of();
        private String registrarId;
        
        public Builder setHostName(String hostName) {
            this.hostName = hostName;
            // Extract TLD from hostname
            int lastDot = hostName.lastIndexOf('.');
            if (lastDot > 0) {
                this.tld = hostName.substring(lastDot + 1);
            }
            return this;
        }
        
        public Builder setTld(String tld) {
            this.tld = tld;
            return this;
        }
        
        public Builder setAddresses(Set<InetAddress> addresses) {
            this.addresses = ImmutableSet.copyOf(addresses);
            return this;
        }
        
        public Builder addAddress(String ipAddress) throws UnknownHostException {
            InetAddress addr = InetAddress.getByName(ipAddress);
            this.addresses = ImmutableSet.<InetAddress>builder()
                .addAll(this.addresses)
                .add(addr)
                .build();
            return this;
        }
        
        public Builder setRegistrarId(String registrarId) {
            this.registrarId = registrarId;
            return this;
        }
        
        public Host build() {
            Objects.requireNonNull(hostName, "Host name is required");
            Objects.requireNonNull(registrarId, "Registrar ID is required");
            
            return new Host(this);
        }
    }
}