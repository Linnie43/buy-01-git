# Discovery Service (Eureka)


## Purpose

The Discovery service acts as a **central registry** for all microservices in the system. Every microservice (Product, Media, User, Gateway, etc.) registers itself with the Discovery service on startup. 
This allows other services to find and communicate with each other dynamically, without hardcoding hostnames or ports.

---

## Benefits of a discovery service:

* **Dynamic Service Discovery**: If a service’s port or host changes (e.g., after a restart or in a Docker environment), other services can still find it automatically.

* **Decoupling Services**: Microservices don’t need to know each other’s addresses; they query the registry instead.

* **Health Monitoring**: Eureka can mark instances as unavailable if they fail to send heartbeats, helping the system avoid routing traffic to down services.

* **Resilience**: Load balancing helps to avoid overloading a single instance and re-routes traffic if an instance would fail.

---

## How it works with LoadBalancer:

1. **Service Registration**: Each microservice (Product, Media, User) registers itself with Eureka on startup.

2. **Heartbeat & Health**: Microservices send periodic heartbeats. Eureka marks unhealthy or offline instances as unavailable.

3. **Dynamic Routing**: When the gateway wants to call another service, it queries Eureka to get a list of available instances.

---

## Key points

* Runs on `localhost:8761`
* All microservices register on startup
* Minimal code: `DiscoveryApplication.java` + configuration

---

## Package structure

```
backend/discovery/
├── src/main/java/com/buy01/discovery
│   └── DiscoveryApplication.java       # Main Spring Boot application class
├── src/main/resources
│   └── application.yml                 # Configuration file for the discovery service
├── pom.xml                             # Maven build file
└── README.md
```

---

## Usage

Run discovery **first** before other services.

1. **Go into Discovery Service Project**
   ```bash
   cd buy-01-git/backend/discovery
   ```

2. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **Check connected services**

   Go to http://localhost:8761 to see registered services

---

## Key Benefits

* No hardcoded URLs
* Scales easily
* Automatic failover to healthy instances