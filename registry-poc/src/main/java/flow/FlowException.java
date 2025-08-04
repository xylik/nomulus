package flow;

/**
 * Exception thrown by flows when operations fail.
 * 
 * Similar to Nomulus's EppException, this provides structured
 * error handling for registry operations.
 */
public class FlowException extends Exception {
    
    public enum Type {
        VALIDATION_ERROR,
        DOMAIN_NOT_AVAILABLE,
        DOMAIN_NOT_FOUND,
        UNAUTHORIZED,
        INTERNAL_ERROR,
        CONFIGURATION_ERROR
    }
    
    private final Type type;
    private final String errorCode;
    
    public FlowException(Type type, String message) {
        super(message);
        this.type = type;
        this.errorCode = type.name();
    }
    
    public FlowException(Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.errorCode = type.name();
    }
    
    public FlowException(Type type, String errorCode, String message) {
        super(message);
        this.type = type;
        this.errorCode = errorCode;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}