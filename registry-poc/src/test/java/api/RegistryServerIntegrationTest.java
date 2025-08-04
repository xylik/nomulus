package api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for the Registry API.
 * 
 * Demonstrates full end-to-end testing patterns:
 * - REST API testing
 * - JSON request/response validation
 * - Business flow testing
 * - Error condition testing
 */
@org.junit.jupiter.api.Disabled("Integration tests require server setup - run manually")
class RegistryServerIntegrationTest {
    
    private static RegistryServer server;
    private static final int TEST_PORT = 8081;
    
    @BeforeAll
    static void setUp() {
        server = new RegistryServer();
        server.start(TEST_PORT);
        RestAssured.port = TEST_PORT;
        RestAssured.baseURI = "http://localhost";
    }
    
    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }
    
    @Test
    void testHealthEndpoint() {
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("OK"))
            .body("message", containsString("Registry PoC"));
    }
    
    @Test
    void testGetTldConfiguration() {
        given()
            .when()
            .get("/api/config")
            .then()
            .statusCode(200)
            .body("supportedTlds", hasItems("com", "org", "app", "dev"));
    }
    
    @Test
    void testDomainCheckAvailable() {
        String requestBody = """
            {
                "domainName": "available-domain.com",
                "registrarId": "REG-TEST"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/domain/check")
            .then()
            .statusCode(200)
            .body("domainName", equalTo("available-domain.com"))
            .body("available", equalTo(true))
            .body("reason", equalTo("Available"));
    }
    
    @Test
    void testDomainCheckNotAvailable() {
        String requestBody = """
            {
                "domainName": "example.com",
                "registrarId": "REG-TEST"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/domain/check")
            .then()
            .statusCode(200)
            .body("domainName", equalTo("example.com"))
            .body("available", equalTo(false))
            .body("reason", equalTo("Already registered"));
    }
    
    @Test
    void testDomainCheckUnsupportedTld() {
        String requestBody = """
            {
                "domainName": "example.xyz",
                "registrarId": "REG-TEST"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/domain/check")
            .then()
            .statusCode(400)
            .body("errorCode", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("TLD 'xyz' is not supported"));
    }
    
    @Test
    void testDomainCreateSuccess() {
        String requestBody = """
            {
                "domainName": "new-domain.app",
                "registrarId": "REG-TEST",
                "registrationPeriod": 2,
                "registrant": {
                    "contactId": "CONTACT-TEST",
                    "name": "Test User",
                    "email": "test@example.com"
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/domain/create")
            .then()
            .statusCode(201)
            .body("domainName", equalTo("new-domain.app"))
            .body("status", equalTo("ACTIVE"))
            .body("price", greaterThan(0.0f))
            .body("message", containsString("successfully"));
    }
    
    @Test
    void testDomainCreateAlreadyExists() {
        String requestBody = """
            {
                "domainName": "example.com",
                "registrarId": "REG-TEST",
                "registrationPeriod": 1,
                "registrant": {
                    "contactId": "CONTACT-TEST",
                    "name": "Test User",
                    "email": "test@example.com"
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/domain/create")
            .then()
            .statusCode(400)
            .body("errorCode", equalTo("DOMAIN_NOT_AVAILABLE"))
            .body("message", containsString("not available"));
    }
    
    @Test
    void testDomainCreateInvalidPeriod() {
        String requestBody = """
            {
                "domainName": "test-period.com",
                "registrarId": "REG-TEST",
                "registrationPeriod": 15,
                "registrant": {
                    "contactId": "CONTACT-TEST",
                    "name": "Test User",
                    "email": "test@example.com"
                }
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/domain/create")
            .then()
            .statusCode(400)
            .body("errorCode", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("Registration period must be between 1 and 10 years"));
    }
    
    @Test
    void testListDomains() {
        given()
            .when()
            .get("/api/domains")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2)) // At least the pre-populated domains
            .body("'example.com'", notNullValue())
            .body("'test.org'", notNullValue());
    }
    
    @Test
    void testDomainWorkflow() {
        String uniqueDomain = "workflow-test-" + System.currentTimeMillis() + ".org";
        
        // 1. Check domain is available
        String checkRequest = String.format("""
            {
                "domainName": "%s",
                "registrarId": "REG-TEST"
            }
            """, uniqueDomain);
        
        given()
            .contentType(ContentType.JSON)
            .body(checkRequest)
            .when()
            .post("/api/domain/check")
            .then()
            .statusCode(200)
            .body("available", equalTo(true));
        
        // 2. Create the domain
        String createRequest = String.format("""
            {
                "domainName": "%s",
                "registrarId": "REG-TEST",
                "registrationPeriod": 1,
                "registrant": {
                    "contactId": "CONTACT-WORKFLOW",
                    "name": "Workflow Test",
                    "email": "workflow@example.com"
                }
            }
            """, uniqueDomain);
        
        given()
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/domain/create")
            .then()
            .statusCode(201)
            .body("domainName", equalTo(uniqueDomain));
        
        // 3. Check domain is no longer available
        given()
            .contentType(ContentType.JSON)
            .body(checkRequest)
            .when()
            .post("/api/domain/check")
            .then()
            .statusCode(200)
            .body("available", equalTo(false));
    }
}