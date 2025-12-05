#!/bin/bash
# Make sure the script exits on error
set -e

# Optional: colors for readability
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting all backend microservices...${NC}"

# Start Eureka (discovery)
echo -e "${GREEN}Starting Discovey...${NC}"
cd discovery
mvn spring-boot:run &
EUREKA_PID=$!
cd ..

# Start User-service
echo -e "${GREEN}Starting user-service...${NC}"
cd user-service
mvn spring-boot:run &
MS1_PID=$!
cd ..

# Start Product-service
echo -e "${GREEN}Starting product-service...${NC}"
cd product-service
mvn spring-boot:run &
MS2_PID=$!
cd ..

# Start Media-service
echo -e "${GREEN}Starting media-service...${NC}"
cd media-service
mvn spring-boot:run &
MS3_PID=$!
cd ..

# Start Gateway
echo -e "${GREEN}Starting gateway...${NC}"
cd gateway
mvn spring-boot:run &
GATEWAY_PID=$!
cd ..

echo -e "${GREEN}All backend services started.${NC}"
echo "Eureka PID: $EUREKA_PID, MS1 PID: $MS1_PID, MS2 PID: $MS2_PID, MS3 PID: $MS3_PID, Gateway PID: $GATEWAY_PID"

# Wait for all processes (optional)
wait
