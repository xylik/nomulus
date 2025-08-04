package injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import config.TldConfiguration;
import events.EventPublisher;
import flow.DomainCheckFlow;

/**
 * Guice module for dependency injection configuration.
 * 
 * Demonstrates clean dependency injection patterns from Nomulus:
 * - Singleton scoping for shared resources
 * - Provider methods for complex object creation
 * - Interface-based binding for testability
 * - Separation of concerns
 */
public class RegistryModule extends AbstractModule {
    
    @Override
    protected void configure() {
        // Bind repository implementation
        bind(DomainCheckFlow.DomainRepository.class).to(InMemoryDomainRepository.class).in(Singleton.class);
    }
    
    @Provides
    @Singleton
    public TldConfiguration provideTldConfiguration() {
        return new TldConfiguration();
    }
    
    @Provides
    @Singleton 
    public EventPublisher provideEventPublisher() {
        EventPublisher publisher = new EventPublisher();
        
        // Register default event subscribers
        publisher.subscribe(new AnalyticsEventSubscriber());
        publisher.subscribe(new AuditEventSubscriber());
        
        return publisher;
    }
    
    /**
     * Example event subscriber for analytics
     */
    private static class AnalyticsEventSubscriber implements EventPublisher.EventSubscriber {
        @Override
        public void onEvent(events.DomainEvent event) {
            // In a real system, this would send to analytics service
            System.out.println("[ANALYTICS] " + event.getEventType() + 
                             " for domain: " + event.getDomainName() +
                             " by registrar: " + event.getRegistrarId());
        }
    }
    
    /**
     * Example event subscriber for audit trail
     */
    private static class AuditEventSubscriber implements EventPublisher.EventSubscriber {
        @Override
        public void onEvent(events.DomainEvent event) {
            // In a real system, this would write to audit database
            System.out.println("[AUDIT] " + event.getTimestamp() + " - " + 
                             event.getEventType() + " - " + event.getDomainName());
        }
    }
}