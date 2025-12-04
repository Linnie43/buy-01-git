# Buy-01 Backend

## Overview

The backend is built as a **microservice-based system** using Java, Spring Boot, Eureka for discovery, a gateway service, Kafka for asynchronous communication, and MongoDB for persistence. 
Each service is independent, responsible for its own domain, and communicates through REST or Kafka events.

## Architecture

* **Gateway Service**: Single entry point for the frontend. Handles routing, security, and request filtering.
* **Discovery Service (Eureka)**: Registers all microservices and enables dynamic service discovery.
* **User Service**: Handles authentication, authorization, and user management.
* **Product Service**: Manages product CRUD operations.
* **Media Service**: Handles media uploads, storage, and retrieval.
* **Kafka**: Used for asynchronous events (e.g., product updates, media processing, notifications).
* **MongoDB**: Primary database for all microservices.

## Technologies

* Java 17+
* Spring Boot
* Spring Web
* Spring Security
* Spring Data MongoDB
* Spring Cloud Netflix Eureka
* Spring Cloud Gateway
* Kafka
* MongoDB
* Maven

## How Services Work Together

1. The **frontend** communicates only with the **gateway**.
2. Gateway routes requests to the correct microservice.
3. Microservices register with **Eureka**, so routing stays dynamic.
4. For async tasks, services publish and listen to **Kafka topics**.
5. Each microservice stores its own data in **MongoDB collections**.

## Project Structure

```
backend/
├─ discovery/               # Eureka service registry
├─ gateway/                 # API gateway
├─ user-service/
├─ product-service/
├─ media-service/
├─ docker-compose.yml    # Kafka
├─ start-backend.sh      # Script to start all services
└─ pom.xml
```

## Data Flow Example

### User signup

1. Frontend → Gateway → User Service
2. User service creates user in MongoDB

## User delete

1. Frontend → Gateway → User Service
2. User service deletes user from MongoDB
3. User service emits **user-deleted** event to Kafka
4. Other services (Product, Media) listen for **user-deleted** and clean up related data

### Product creation

1. Frontend → Gateway → Product Service
2. Product service stores product in MongoDB

### Media upload

1. Frontend → Gateway → Media Service
2. Media stored locally
3. Media service stores path to file in MongoDB

## Running the Backend

### Starting MongoDB

If MongoDB is not included in your Docker setup, you can run it separately:

#### Option 1: Run MongoDB in Docker

```bash
docker run -d \
--name mongodb \
-p 27017:27017 \
-v mongo_data:/data/db \
mongo:latest
```

#### Option 2: Install MongoDB locally

```bash
brew install mongodb-community
```

Start it with:

1. Start Docker services (Kafka):

```bash
docker compose up -d
```

2. Start Discovery, Gateway, and all microservices in their respective directories:

```bash
mvn spring-boot:run
```

(or run shell script `start-backend.sh`)

## Notes

* Each service has its own `application.yml`.
* JWT authentication is centralized.
* All internal communication uses service names resolved by Eureka.
* Kafka topics are predefined for inter-service events.

