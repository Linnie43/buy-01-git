# Gateway Service

## Purpose

The Gateway service sits in front of multiple backend microservices and provides a single entry point for clients. 
It handles routing, aggregation, authentication, and request validation. This keeps your system clean and reduces duplicated logic across services.

---

## Benefits of a gateway service:

* **Keeps your backend lean**: Microservices focus on business logic, not gateway concerns. 
* **Scalability**: Easier to evolve or restructure backend services without breaking clients. 
* **Reduces repeated code and configurations**: No need to write security configuration in every service.
* **Centralized security**: One place to observe, monitor, and secure traffic. 
* **Easier frontend configuration**: Frontend only talks to one endpoint that forwards traffic.

---

## How it works:

1. **Routes incoming requests** to the correct backend service.
2. **Centralizes authentication/authorization** so each service doesn't need to implement it.
3. **Applies rate limiting and request validation** before traffic hits internal services.
4. **Handles cross-service communication** and can combine responses if needed.
5. **Improves maintainability** by decoupling clients from direct service-to-service knowledge.

---

## File Structure

```
backend/gateway/
├─ src/main/java/com/buy01/discovery
│  ├─ security/                 # Security configurations, filters, and utilities
│  └─ GatewayApplication.java   # Main Spring Boot application class
├─ src/main/resources
│  └─ application.yml           # Configuration file for the gateway service
├─ pom.xml                      # Maven build file
└─ README.md
```

---

## Usage

1. **Go into Gateway Service Project**
   ```bash
   cd buy-01-git/backend/gateway
   ```

2. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   
3. **Access the Gateway**

    Access the gateway at `https://localhost:8443`  to route requests to backend services.

---

## Key Benefits

* Single entry point for all backend services
* Centralized security and request handling
* Simplified client configuration
* Easier to maintain and scale backend services
