package events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.inject.Singleton;

/**
 * Event publisher for domain registry events.
 * 
 * Provides asynchronous event publishing with:
 * - Observer pattern implementation
 * - Thread-safe event handling
 * - Multiple subscriber support
 * - Non-blocking event processing
 */
@Singleton
public class EventPublisher {
    
    private final List<EventSubscriber> subscribers = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "EventPublisher");
        t.setDaemon(true);
        return t;
    });
    
    /**
     * Subscribe to domain events
     */
    public void subscribe(EventSubscriber subscriber) {
        subscribers.add(subscriber);
    }
    
    /**
     * Unsubscribe from domain events
     */
    public void unsubscribe(EventSubscriber subscriber) {
        subscribers.remove(subscriber);
    }
    
    /**
     * Publish an event to all subscribers asynchronously
     */
    public void publish(DomainEvent event) {
        for (EventSubscriber subscriber : subscribers) {
            executor.submit(() -> {
                try {
                    subscriber.onEvent(event);
                } catch (Exception e) {
                    // Log error but don't let one subscriber failure affect others
                    System.err.println("Error processing event in subscriber " + 
                                     subscriber.getClass().getSimpleName() + ": " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Publish an event synchronously for testing
     */
    public void publishSync(DomainEvent event) {
        for (EventSubscriber subscriber : subscribers) {
            try {
                subscriber.onEvent(event);
            } catch (Exception e) {
                System.err.println("Error processing event in subscriber " + 
                                 subscriber.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Get the number of active subscribers
     */
    public int getSubscriberCount() {
        return subscribers.size();
    }
    
    /**
     * Shutdown the event publisher (for clean application shutdown)
     */
    public void shutdown() {
        executor.shutdown();
    }
    
    /**
     * Interface for event subscribers
     */
    public interface EventSubscriber {
        void onEvent(DomainEvent event);
    }
}