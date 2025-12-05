#!/bin/bash

# StreamRide Docker Quick Start Script
# This script helps you quickly start the StreamRide application

set -e

echo "🚗 StreamRide Docker Setup"
echo "=========================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker and Docker Compose are installed${NC}"
echo ""

# Check if .env file exists, if not create from example
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env file not found. Creating from .env.example...${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}✅ Created .env file${NC}"
    fi
fi

echo ""
echo "Select an option:"
echo "1) Start all services (build + run)"
echo "2) Start all services (detached mode)"
echo "3) Stop all services"
echo "4) View logs"
echo "5) Clean up (remove containers and volumes)"
echo "6) Rebuild specific service"
echo ""
read -p "Enter your choice [1-6]: " choice

case $choice in
    1)
        echo -e "${YELLOW}🔨 Building and starting all services...${NC}"
        docker-compose up --build
        ;;
    2)
        echo -e "${YELLOW}🔨 Building and starting all services in detached mode...${NC}"
        docker-compose up -d --build
        echo ""
        echo -e "${GREEN}✅ Services started successfully!${NC}"
        echo ""
        echo "Access the services at:"
        echo "  📊 Dashboard UI:      http://localhost:5173"
        echo "  🔍 Kafdrop (Kafka):   http://localhost:9000"
        echo "  🚀 Producer Service:  http://localhost:8081"
        echo "  ⚙️  Processor Service: http://localhost:8082"
        echo "  📡 Dashboard Service: http://localhost:8083"
        echo ""
        echo "To view logs: docker-compose logs -f"
        echo "To stop: docker-compose down"
        ;;
    3)
        echo -e "${YELLOW}🛑 Stopping all services...${NC}"
        docker-compose down
        echo -e "${GREEN}✅ Services stopped${NC}"
        ;;
    4)
        echo -e "${YELLOW}📋 Viewing logs (Ctrl+C to exit)...${NC}"
        docker-compose logs -f
        ;;
    5)
        echo -e "${RED}⚠️  This will remove all containers, networks, and volumes!${NC}"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            echo -e "${YELLOW}🧹 Cleaning up...${NC}"
            docker-compose down -v
            echo -e "${GREEN}✅ Cleanup complete${NC}"
        else
            echo "Cancelled"
        fi
        ;;
    6)
        echo "Available services:"
        echo "  - producer-service"
        echo "  - processor-service"
        echo "  - dashboard-service"
        echo "  - dashboard-ui"
        echo ""
        read -p "Enter service name to rebuild: " service
        echo -e "${YELLOW}🔨 Rebuilding $service...${NC}"
        docker-compose up -d --build $service
        echo -e "${GREEN}✅ $service rebuilt${NC}"
        ;;
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac
