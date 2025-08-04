package flow;

import model.Domain;
import config.TldConfiguration;
import events.DomainEvent;
import events.EventPublisher;
import com.google.inject.Inject;

/**
 * Flow for checking domain availability.
 * 
 * Demonstrates the flow pattern from Nomulus:
 * - Input validation
 * - Business logic execution  
 * - Result generation
 * - Event publishing
 */
public class DomainCheckFlow implements Flow<DomainCheckFlow.Input, DomainCheckFlow.Output> {
    
    private final Input input;
    private final DomainRepository domainRepository;
    private final TldConfiguration tldConfig;
    private final EventPublisher eventPublisher;
    
    @Inject
    public DomainCheckFlow(
            Input input,
            DomainRepository domainRepository,
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
        
        String tld = extractTld(input.domainName);
        if (!tldConfig.isSupportedTld(tld)) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR,
                "TLD '" + tld + "' is not supported");
        }
        
        if (!isValidDomainName(input.domainName)) {
            throw new FlowException(FlowException.Type.VALIDATION_ERROR,
                "Invalid domain name format");
        }
    }
    
    @Override
    public Output run() throws FlowException {
        validate();
        
        String domainName = input.domainName.toLowerCase();
        boolean isAvailable = !domainRepository.exists(domainName);
        String reason = isAvailable ? "Available" : "Already registered";
        
        // Publish domain check event for analytics
        eventPublisher.publish(new DomainEvent.DomainChecked(
            domainName, 
            input.registrarId,
            isAvailable
        ));
        
        return new Output(domainName, isAvailable, reason);
    }
    
    private String extractTld(String domainName) {
        int lastDot = domainName.lastIndexOf('.');
        return lastDot > 0 ? domainName.substring(lastDot + 1) : "";
    }
    
    private boolean isValidDomainName(String domainName) {
        // Simplified domain name validation
        return domainName.matches("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * Input for domain check operation
     */
    public static class Input {
        public final String domainName;
        public final String registrarId;
        
        public Input(String domainName, String registrarId) {
            this.domainName = domainName;
            this.registrarId = registrarId;
        }
    }
    
    /**
     * Output of domain check operation
     */
    public static class Output {
        public final String domainName;
        public final boolean available;
        public final String reason;
        
        public Output(String domainName, boolean available, String reason) {
            this.domainName = domainName;
            this.available = available;
            this.reason = reason;
        }
    }
    
    /**
     * Repository interface for domain persistence
     */
    public interface DomainRepository {
        boolean exists(String domainName);
        void save(Domain domain);
        Domain findByName(String domainName);
    }
}