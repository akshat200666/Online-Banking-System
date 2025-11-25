#!/bin/bash
# Build the shaded JAR inside a Maven container and leave the jar in target/
set -e
docker run --rm -v "$PWD":/app -w /app maven:3.9.4-eclipse-temurin-17 mvn clean package
echo "Built jar in target/ directory. Run: java -jar target/atm-management-1.0-SNAPSHOT-shaded.jar"
