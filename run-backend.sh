#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
fi

# Ensure Docker Compose is running
echo "Checking PostgreSQL status..."
docker compose up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 2

# Run the Spring Boot application
echo "Starting Spring Boot application..."
./gradlew :backend:bootRun