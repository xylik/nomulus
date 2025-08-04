package flow;

import model.Domain;
import model.Contact;
import config.TldConfiguration;
import events.DomainEvent;
import events.EventPublisher;
import com.google.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Flow for creating new domain registrations.
 * 
 * Demonstrates complex business logic flow with:
 * - Multiple validation steps
 * - Transactional semantics
 * - Event generation
 * - Premium pricing logic
 */
public class DomainCreateFlow implements Flow<DomainCreateFlow.Input, DomainCreateFlow.Output> {
    
    private final Input input;
    private final DomainCheckFlow.DomainRepository domainRepository;
    private final TldConfiguration tldConfig;
    private final EventPublisher eventPublisher;
    
    @Inject
    public DomainCreateFlow(
            Input input,
            DomainCheckFlow.DomainRepository domainRepository,
            TldConfiguration tldConfig,
            EventPublisher eventPublisher) {
        this.input = input;
        this.domainRepository = domainRepository;
        this.tldConfig = tldConfig;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void validate() throws FlowException {
        if (input.domainName == null || input.domainName.trim().isEmpty()) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR, 
                "Domain name is required");
        }
        
        if (input.registrant == null) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR,
                "Registrant contact is required");
        }
        
        if (input.registrarId == null || input.registrarId.trim().isEmpty()) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR,
                "Registrar ID is required");
        }
        
        String tld = extractTld(input.domainName);
        if (!tldConfig.isSupportedTld(tld)) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR,
                "TLD '" + tld + "' is not supported");
        }
        
        if (input.registrationPeriod < 1 || input.registrationPeriod > 10) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR,
                "Registration period must be between 1 and 10 years");
        }
    }
    
    @Override
    public Output run() throws FlowException {
        validate();
        
        String domainName = input.domainName.toLowerCase();
        
        // Check availability
        if (domainRepository.exists(domainName)) {
            throw new FlowException(FlowException.Type.DOMAIN_NOT_AVAILABLE,
                "Domain " + domainName + " is not available");
        }
        
        // Calculate expiration time
        Instant now = Instant.now();
        Instant expirationTime = now.plus(input.registrationPeriod * 365, ChronoUnit.DAYS);
        
        // Create domain entity
        Domain domain = Domain.newBuilder()
            .setDomainName(domainName)
            .setRegistrarId(input.registrarId)
            .setCreationTime(now)
            .setExpirationTime(expirationTime)
            .setStatus(Domain.Status.ACTIVE)
            .setRegistrant(input.registrant)
            .setContacts(input.contacts)
            .setNameservers(input.nameservers)
            .build();
        
        // Save domain
        domainRepository.save(domain);
        
        // Calculate pricing
        String tld = extractTld(domainName);
        double price = calculatePrice(domainName, tld, input.registrationPeriod);
        
        // Publish creation event
        eventPublisher.publish(new DomainEvent.DomainCreated(
            domain,
            input.registrarId,
            price
        ));
        
        return new Output(domain, price, "Domain created successfully");
    }
    
    private String extractTld(String domainName) {
        int lastDot = domainName.lastIndexOf('.');
        return lastDot > 0 ? domainName.substring(lastDot + 1) : "";
    }
    
    private double calculatePrice(String domainName, String tld, int years) {
        // Simplified pricing logic - could be much more complex in real system
        double basePrice = tldConfig.getBasePriceForTld(tld);
        
        // Premium domain logic
        if (isPremiumDomain(domainName)) {
            basePrice *= 10; // Premium multiplier
        }
        
        return basePrice * years;
    }
    
    private boolean isPremiumDomain(String domainName) {
        // Simplified premium domain check
        String sld = domainName.substring(0, domainName.lastIndexOf('.'));
        return sld.length() <= 3 || 
               sld.matches(".*\\d+.*") ||  // Contains numbers
               isPremiumKeyword(sld);
    }
    
    private boolean isPremiumKeyword(String sld) {
        String[] premiumKeywords = {"app", "web", "tech", "cloud", "api", "dev"};
        for (String keyword : premiumKeywords) {
            if (sld.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Input for domain creation
     */
    public static class Input {
        public final String domainName;
        public final String registrarId;
        public final Contact registrant;
        public final java.util.Set<Contact> contacts;
        public final java.util.Set<model.Host> nameservers;
        public final int registrationPeriod; // in years
        
        public Input(String domainName, String registrarId, Contact registrant,
                    java.util.Set<Contact> contacts, java.util.Set<model.Host> nameservers,
                    int registrationPeriod) {
            this.domainName = domainName;
            this.registrarId = registrarId;
            this.registrant = registrant;
            this.contacts = contacts != null ? contacts : java.util.Set.of();
            this.nameservers = nameservers != null ? nameservers : java.util.Set.of();
            this.registrationPeriod = registrationPeriod;
        }
    }
    
    /**
     * Output of domain creation
     */
    public static class Output {
        public final Domain domain;
        public final double price;
        public final String message;
        
        public Output(Domain domain, double price, String message) {
            this.domain = domain;
            this.price = price;
            this.message = message;
        }
    }
}