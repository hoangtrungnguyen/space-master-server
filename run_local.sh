#!/bin/bash

# Environment Variables
export REDIS_URL="redis://:default@localhost:6379"
export JWT_SECRET="local-dev-secret-key-do-not-use-in-prod"
export FLAVOR="dev"
export DATABASE_URL="jdbc:postgresql://localhost:5432/space_main"

# Kafka (using defaults, but verified explicit if needed)
# export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
# export KAFKA_SCHEMA_REGISTRY_URL=http://localhost:8081

echo "Environment configured:"
echo "REDIS_URL=$REDIS_URL"
echo "JWT_SECRET=<hidden>"
echo "FLAVOR=$FLAVOR"

echo "Starting servers..."

# Function to run a gradle task
run_service() {
    local service=$1
    echo "Starting $service..."
    ./gradlew :$service:run
}

# Check argument to run specific service or both (basic logic)
if [ "$1" == "sync" ]; then
    run_service "space-sync-server"
elif [ "$1" == "gateway" ]; then
    run_service "space-api-gateway"
else
    echo "Usage: ./run_local.sh [sync|gateway]"
    echo "To run both, open two terminals and run each command separately after sourcing env vars, or use this script for one at a time."
fi
