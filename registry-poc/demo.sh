#!/bin/bash

# Registry PoC Demo Script
# This script demonstrates the key technical patterns extracted from Nomulus

echo "=========================================="
echo "Registry PoC - Nomulus Technical Patterns"
echo "=========================================="

# Start the server in background
echo "Starting Registry PoC server..."
cd "$(dirname "$0")"
./gradlew runApp &
SERVER_PID=$!

# Wait for server to start
echo "Waiting for server to start..."
sleep 8

# Test health endpoint
echo ""
echo "1. Testing Health Endpoint"
echo "GET /health"
curl -s http://localhost:8080/health | python3 -m json.tool
echo ""

# Test TLD configuration
echo "2. Testing TLD Configuration"
echo "GET /api/config"
curl -s http://localhost:8080/api/config | python3 -m json.tool
echo ""

# Test domain availability check
echo "3. Testing Domain Availability Check"
echo "POST /api/domain/check (available domain)"
curl -s -X POST http://localhost:8080/api/domain/check \
  -H "Content-Type: application/json" \
  -d '{"domainName": "mynewdomain.com", "registrarId": "REG-DEMO"}' | python3 -m json.tool
echo ""

echo "POST /api/domain/check (unavailable domain)"  
curl -s -X POST http://localhost:8080/api/domain/check \
  -H "Content-Type: application/json" \
  -d '{"domainName": "example.com", "registrarId": "REG-DEMO"}' | python3 -m json.tool
echo ""

# Test domain creation
echo "4. Testing Domain Creation"
echo "POST /api/domain/create"
curl -s -X POST http://localhost:8080/api/domain/create \
  -H "Content-Type: application/json" \
  -d '{
    "domainName": "demo-domain.app", 
    "registrarId": "REG-DEMO", 
    "registrationPeriod": 1,
    "registrant": {
      "contactId": "DEMO-CONTACT", 
      "name": "Demo User", 
      "email": "demo@example.com"
    }
  }' | python3 -m json.tool
echo ""

# Test premium domain pricing
echo "5. Testing Premium Domain Pricing"
echo "POST /api/domain/create (premium domain with 'app' keyword)"
curl -s -X POST http://localhost:8080/api/domain/create \
  -H "Content-Type: application/json" \
  -d '{
    "domainName": "myapp.app", 
    "registrarId": "REG-DEMO", 
    "registrationPeriod": 1,
    "registrant": {
      "contactId": "DEMO-CONTACT", 
      "name": "Demo User", 
      "email": "demo@example.com"
    }
  }' | python3 -m json.tool
echo ""

# Test validation errors  
echo "6. Testing Validation Errors"
echo "POST /api/domain/check (unsupported TLD)"
curl -s -X POST http://localhost:8080/api/domain/check \
  -H "Content-Type: application/json" \
  -d '{"domainName": "example.invalid", "registrarId": "REG-DEMO"}' | python3 -m json.tool
echo ""

# List all domains
echo "7. Listing All Registered Domains"
echo "GET /api/domains"
curl -s http://localhost:8080/api/domains | python3 -m json.tool
echo ""

# Demo complete
echo "=========================================="
echo "Demo Complete!"
echo ""
echo "Key patterns demonstrated:"
echo "• Flow-based command processing"
echo "• Rich domain model with builders"
echo "• Multi-tenant TLD configuration"
echo "• Event-driven architecture"
echo "• Structured exception handling"
echo "• RESTful API design"
echo "• Premium domain pricing logic"
echo "• Dependency injection patterns"
echo ""
echo "The server will continue running at http://localhost:8080"
echo "Press Ctrl+C to stop the server when done."
echo "=========================================="

# Keep the server running
wait $SERVER_PID