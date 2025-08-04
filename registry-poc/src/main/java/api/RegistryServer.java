package api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import injection.RegistryModule;
import flow.*;
import model.*;
import config.TldConfiguration;
import events.EventPublisher;

import java.util.Set;

/**
 * REST API server for the registry PoC.
 * 
 * Demonstrates:
 * - RESTful API design
 * - JSON serialization
 * - Error handling  
 * - Dependency injection integration
 * - Clean API layer separation
 */
public class RegistryServer {
    
    private final Injector injector;
    private final ObjectMapper objectMapper;
    private Javalin app;
    
    public RegistryServer() {
        this.injector = Guice.createInjector(new RegistryModule());
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public void start(int port) {
        app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            // CORS configuration - simplified for demo
        }).start(port);
        
        setupRoutes();
        
        System.out.println("Registry PoC Server started on http://localhost:" + port);
        System.out.println("Try these endpoints:");
        System.out.println("  GET  /api/config - View TLD configuration");
        System.out.println("  POST /api/domain/check - Check domain availability");
        System.out.println("  POST /api/domain/create - Create domain registration");
        System.out.println("  GET  /api/domains - List all registered domains");
    }
    
    public void stop() {
        if (app != null) {
            app.stop();
        }
        injector.getInstance(EventPublisher.class).shutdown();
    }
    
    private void setupRoutes() {
        // Health check
        app.get("/health", ctx -> ctx.json(new HealthResponse("OK", "Registry PoC is running")));
        
        // TLD configuration endpoint
        app.get("/api/config", this::getTldConfiguration);
        
        // Domain operations
        app.post("/api/domain/check", this::checkDomain);
        app.post("/api/domain/create", this::createDomain);
        app.get("/api/domains", this::listDomains);
        
        // Error handling
        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(new ErrorResponse("INTERNAL_ERROR", e.getMessage()));
        });
    }
    
    private void getTldConfiguration(Context ctx) {
        TldConfiguration config = injector.getInstance(TldConfiguration.class);
        ctx.json(new ConfigResponse(config.getSupportedTlds()));
    }
    
    private void checkDomain(Context ctx) {
        try {
            DomainCheckRequest request = ctx.bodyAsClass(DomainCheckRequest.class);
            
            DomainCheckFlow.Input input = new DomainCheckFlow.Input(
                request.domainName, 
                request.registrarId
            );
            
            DomainCheckFlow flow = new DomainCheckFlow(
                input,
                injector.getInstance(DomainCheckFlow.DomainRepository.class),
                injector.getInstance(TldConfiguration.class),
                injector.getInstance(EventPublisher.class)
            );
            
            DomainCheckFlow.Output output = flow.run();
            ctx.json(new DomainCheckResponse(
                output.domainName, 
                output.available, 
                output.reason
            ));
            
        } catch (FlowException e) {
            ctx.status(400).json(new ErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }
    
    private void createDomain(Context ctx) {
        try {
            DomainCreateRequest request = ctx.bodyAsClass(DomainCreateRequest.class);
            
            // Create registrant contact
            Contact registrant = Contact.newBuilder()
                .setContactId(request.registrant.contactId)
                .setName(request.registrant.name)
                .setEmail(request.registrant.email)
                .setType(Contact.Type.REGISTRANT)
                .build();
            
            DomainCreateFlow.Input input = new DomainCreateFlow.Input(
                request.domainName,
                request.registrarId,
                registrant,
                Set.of(), // No additional contacts for simplicity
                Set.of(), // No nameservers for simplicity  
                request.registrationPeriod
            );
            
            DomainCreateFlow flow = new DomainCreateFlow(
                input,
                injector.getInstance(DomainCheckFlow.DomainRepository.class),
                injector.getInstance(TldConfiguration.class),
                injector.getInstance(EventPublisher.class)
            );
            
            DomainCreateFlow.Output output = flow.run();
            ctx.status(201).json(new DomainCreateResponse(
                output.domain.getDomainName(),
                output.domain.getStatus().toString(),
                output.price,
                output.message
            ));
            
        } catch (FlowException e) {
            ctx.status(400).json(new ErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }
    
    private void listDomains(Context ctx) {
        injection.InMemoryDomainRepository repo = 
            (injection.InMemoryDomainRepository) injector.getInstance(DomainCheckFlow.DomainRepository.class);
        ctx.json(repo.getAllDomains());
    }
    
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new RegistryServer().start(port);
    }
    
    // Request/Response classes
    public static class HealthResponse {
        public final String status;
        public final String message;
        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
    
    public static class ConfigResponse {
        public final Set<String> supportedTlds;
        public ConfigResponse(Set<String> supportedTlds) {
            this.supportedTlds = supportedTlds;
        }
    }
    
    public static class DomainCheckRequest {
        public String domainName;
        public String registrarId;
    }
    
    public static class DomainCheckResponse {
        public final String domainName;
        public final boolean available;
        public final String reason;
        public DomainCheckResponse(String domainName, boolean available, String reason) {
            this.domainName = domainName;
            this.available = available;
            this.reason = reason;
        }
    }
    
    public static class DomainCreateRequest {
        public String domainName;
        public String registrarId;
        public ContactRequest registrant;
        public int registrationPeriod;
        
        public static class ContactRequest {
            public String contactId;
            public String name;
            public String email;
        }
    }
    
    public static class DomainCreateResponse {
        public final String domainName;
        public final String status;
        public final double price;
        public final String message;
        public DomainCreateResponse(String domainName, String status, double price, String message) {
            this.domainName = domainName;
            this.status = status;
            this.price = price;
            this.message = message;
        }
    }
    
    public static class ErrorResponse {
        public final String errorCode;
        public final String message;
        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }
    }
}