package flow;

/**
 * Core interface for registry operations flows.
 * 
 * Inspired by Nomulus's Flow interface, this represents a unit of work
 * that can be executed to process registry commands. Each flow is responsible
 * for validating input, performing the operation, and returning a result.
 * 
 * This pattern provides:
 * - Clear separation of concerns
 * - Transactional boundaries
 * - Testable units of work
 * - Consistent error handling
 */
public interface Flow<TInput, TOutput> {
    
    /**
     * Executes the flow operation.
     * 
     * @return the result of the flow execution
     * @throws FlowException if the operation fails
     */
    TOutput run() throws FlowException;
    
    /**
     * Validates that the flow can be executed with the current input.
     * Called before run() to catch validation errors early.
     * 
     * @throws FlowException if validation fails
     */
    default void validate() throws FlowException {
        // Default implementation does nothing - subclasses can override
    }
}