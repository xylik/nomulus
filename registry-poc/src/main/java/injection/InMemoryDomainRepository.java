package injection;

import flow.DomainCheckFlow;
import model.Domain;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.google.inject.Singleton;

/**
 * In-memory implementation of domain repository for demo purposes.
 * 
 * In a real system, this would be backed by a database like PostgreSQL
 * or Cloud SQL as in Nomulus.
 */
@Singleton
public class InMemoryDomainRepository implements DomainCheckFlow.DomainRepository {
    
    private final Map<String, Domain> domains = new ConcurrentHashMap<>();
    
    public InMemoryDomainRepository() {
        // Pre-populate with some example domains for demo
        initializeExampleDomains();
    }
    
    @Override
    public boolean exists(String domainName) {
        return domains.containsKey(domainName.toLowerCase());
    }
    
    @Override
    public void save(Domain domain) {
        domains.put(domain.getDomainName().toLowerCase(), domain);
    }
    
    @Override
    public Domain findByName(String domainName) {
        return domains.get(domainName.toLowerCase());
    }
    
    /**
     * Get all registered domains (for demo/testing purposes)
     */
    public Map<String, Domain> getAllDomains() {
        return new ConcurrentHashMap<>(domains);
    }
    
    /**
     * Clear all domains (for testing)
     */
    public void clear() {
        domains.clear();
    }
    
    private void initializeExampleDomains() {
        // Create some sample registered domains for demo
        model.Contact sampleContact = model.Contact.newBuilder()
            .setContactId("CONTACT-1")
            .setName("John Doe")
            .setEmail("john@example.com")
            .setType(model.Contact.Type.REGISTRANT)
            .build();
        
        Domain exampleDomain = Domain.newBuilder()
            .setDomainName("example.com")
            .setRegistrarId("REG-001")
            .setRegistrant(sampleContact)
            .setStatus(Domain.Status.ACTIVE)
            .build();
        
        Domain testDomain = Domain.newBuilder()
            .setDomainName("test.org")
            .setRegistrarId("REG-001")
            .setRegistrant(sampleContact)
            .setStatus(Domain.Status.ACTIVE)
            .build();
        
        domains.put("example.com", exampleDomain);
        domains.put("test.org", testDomain);
        
        System.out.println("Initialized repository with " + domains.size() + " sample domains");
    }
}