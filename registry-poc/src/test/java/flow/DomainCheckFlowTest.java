package flow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import config.TldConfiguration;
import events.EventPublisher;

/**
 * Test for DomainCheckFlow demonstrating testing patterns from Nomulus.
 * 
 * Shows:
 * - Mock-based unit testing
 * - Flow validation testing
 * - Business logic verification
 * - Event publishing verification
 */
class DomainCheckFlowTest {
    
    @Mock
    private DomainCheckFlow.DomainRepository mockRepository;
    
    @Mock
    private EventPublisher mockEventPublisher;
    
    private TldConfiguration tldConfig;
    private DomainCheckFlow.Input input;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tldConfig = new TldConfiguration();
        input = new DomainCheckFlow.Input("example.com", "REG-001");
    }
    
    @Test
    void testDomainAvailable() throws FlowException {
        // Given
        when(mockRepository.exists("example.com")).thenReturn(false);
        
        DomainCheckFlow flow = new DomainCheckFlow(
            input, mockRepository, tldConfig, mockEventPublisher);
        
        // When
        DomainCheckFlow.Output output = flow.run();
        
        // Then
        assertTrue(output.available);
        assertEquals("example.com", output.domainName);
        assertEquals("Available", output.reason);
        
        // Verify event was published
        verify(mockEventPublisher).publish(any(events.DomainEvent.DomainChecked.class));
    }
    
    @Test
    void testDomainNotAvailable() throws FlowException {
        // Given
        when(mockRepository.exists("example.com")).thenReturn(true);
        
        DomainCheckFlow flow = new DomainCheckFlow(
            input, mockRepository, tldConfig, mockEventPublisher);
        
        // When
        DomainCheckFlow.Output output = flow.run();
        
        // Then
        assertFalse(output.available);
        assertEquals("example.com", output.domainName);
        assertEquals("Already registered", output.reason);
    }
    
    @Test
    void testValidationEmptyDomainName() {
        // Given
        DomainCheckFlow.Input invalidInput = new DomainCheckFlow.Input("", "REG-001");
        DomainCheckFlow flow = new DomainCheckFlow(
            invalidInput, mockRepository, tldConfig, mockEventPublisher);
        
        // When/Then
        FlowException exception = assertThrows(FlowException.class, flow::validate);
        assertEquals(FlowException.Type.VALIDATION_ERROR, exception.getType());
        assertTrue(exception.getMessage().contains("Domain name is required"));
    }
    
    @Test
    void testValidationUnsupportedTld() {
        // Given
        DomainCheckFlow.Input invalidInput = new DomainCheckFlow.Input("example.xyz", "REG-001");
        DomainCheckFlow flow = new DomainCheckFlow(
            invalidInput, mockRepository, tldConfig, mockEventPublisher);
        
        // When/Then
        FlowException exception = assertThrows(FlowException.class, flow::validate);
        assertEquals(FlowException.Type.VALIDATION_ERROR, exception.getType());
        assertTrue(exception.getMessage().contains("TLD 'xyz' is not supported"));
    }
    
    @Test
    void testValidationInvalidDomainFormat() {
        // Given
        DomainCheckFlow.Input invalidInput = new DomainCheckFlow.Input("invalid..domain.com", "REG-001");
        DomainCheckFlow flow = new DomainCheckFlow(
            invalidInput, mockRepository, tldConfig, mockEventPublisher);
        
        // When/Then
        FlowException exception = assertThrows(FlowException.class, flow::validate);
        assertEquals(FlowException.Type.VALIDATION_ERROR, exception.getType());
        assertTrue(exception.getMessage().contains("Invalid domain name format"));
    }
    
    @Test
    void testCaseInsensitiveDomainCheck() throws FlowException {
        // Given
        DomainCheckFlow.Input upperCaseInput = new DomainCheckFlow.Input("EXAMPLE.COM", "REG-001");
        when(mockRepository.exists("example.com")).thenReturn(false);
        
        DomainCheckFlow flow = new DomainCheckFlow(
            upperCaseInput, mockRepository, tldConfig, mockEventPublisher);
        
        // When
        DomainCheckFlow.Output output = flow.run();
        
        // Then
        assertEquals("example.com", output.domainName); // Should be normalized to lowercase
        verify(mockRepository).exists("example.com");
    }
}