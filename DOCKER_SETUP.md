# StreamRide Docker Setup Guide

Complete guide for running the StreamRide project using Docker Compose.

## 📋 Prerequisites

- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Git**: For cloning the repository
- **Minimum 4GB RAM**: Recommended for running all services

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd streamride
```

### 2. Start All Services

```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode (background)
docker-compose up -d --build
```

### 3. Access the Services

Once all services are running, you can access:

| Service | URL | Description |
|---------|-----|-------------|
| **Dashboard UI** | http://localhost:5173 | React frontend for real-time metrics |
| **Kafdrop** | http://localhost:9000 | Kafka Web UI for monitoring topics |
| **Producer Service** | http://localhost:8081 | Ride event producer API |
| **Processor Service** | http://localhost:8082 | Kafka Streams processor |
| **Dashboard Service** | http://localhost:8083 | Backend API & WebSocket server |
| **Kafka Broker** | localhost:9092 (internal)<br>localhost:29092 (external) | Kafka broker endpoints |

### 4. Verify Services

```bash
# Check all services are running
docker-compose ps

# View logs from all services
docker-compose logs -f

# View logs from specific service
docker-compose logs -f producer-service
```

## 🏗️ Architecture

The Docker setup includes:

```
┌─────────────────────────────────────────────────────────┐
│                    StreamRide Stack                      │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  ┌──────────────┐         ┌──────────────┐              │
│  │ Dashboard UI │────────▶│   Nginx      │              │
│  │  (React)     │         │  (Port 5173) │              │
│  └──────────────┘         └──────┬───────┘              │
│                                   │                       │
│                                   ▼                       │
│  ┌──────────────────────────────────────────────┐       │
│  │        Dashboard Service (Port 8083)         │       │
│  │         WebSocket + REST API                 │       │
│  └───────────────────┬──────────────────────────┘       │
│                      │                                    │
│                      ▼                                    │
│  ┌──────────────────────────────────────────────┐       │
│  │      Kafka Broker - KRaft Mode (9092)       │       │
│  │        (No Zookeeper Required)              │       │
│  │                                              │       │
│  │  ┌────────────┐  ┌────────────┐            │       │
│  │  │  Producer  │  │ Processor  │            │       │
│  │  │  Service   │  │  Service   │            │       │
│  │  │ (Port 8081)│  │ (Port 8082)│            │       │
│  │  └────────────┘  └────────────┘            │       │
│  └──────────────────────────────────────────────┘       │
│                                                           │
│  ┌──────────────────────────────────────────────┐       │
│  │         Kafdrop UI (Port 9000)              │       │
│  └──────────────────────────────────────────────┘       │
│                                                           │
└─────────────────────────────────────────────────────────┘

Note: This setup uses Kafka in KRaft mode (Kafka Raft), which
eliminates the need for Zookeeper. KRaft is the modern approach
and will be the default in future Kafka versions.
```

## 🛠️ Common Commands

### Service Management

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# Restart a specific service
docker-compose restart producer-service

# Rebuild a specific service
docker-compose up -d --build producer-service

# Scale a service (if needed)
docker-compose up -d --scale producer-service=2
```

### Monitoring & Debugging

```bash
# View all logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f kafka
docker-compose logs -f dashboard-service

# Check service health
docker-compose ps

# Execute command in running container
docker-compose exec producer-service sh

# View resource usage
docker stats
```

### Data Management

```bash
# Remove all containers and networks (keeps volumes)
docker-compose down

# Remove everything including volumes (CAUTION: deletes Kafka data)
docker-compose down -v

# Clean up unused Docker resources
docker system prune -a
```

## 🔧 Configuration

### Environment Variables

Copy the example environment file and customize:

```bash
cp .env.example .env
```

Key environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Kafka broker address |
| `PRODUCER_SERVICE_PORT` | `8081` | Producer service port |
| `PROCESSOR_SERVICE_PORT` | `8082` | Processor service port |
| `DASHBOARD_SERVICE_PORT` | `8083` | Dashboard service port |
| `DASHBOARD_UI_PORT` | `5173` | Frontend UI port |
| `KAFDROP_PORT` | `9000` | Kafdrop UI port |

### Service Configuration

Each service can be configured via its `application.yml` file:

- **Producer**: `producer-service/src/main/resources/application.yml`
- **Processor**: `processor-service/src/main/resources/application.yml`
- **Dashboard**: `dashboard-service/src/main/resources/application.yml`

## 🧪 Testing the Setup

### 1. Check Kafka Topics

Visit Kafdrop at http://localhost:9000 to see:
- `ride-events` - Raw ride events
- `ride-analytics` - Processed analytics
- `city-stats` - City-level statistics

### 2. Generate Test Data

The producer service automatically generates ride events. You can also trigger manually:

```bash
# Check producer health
curl http://localhost:8081/actuator/health

# View producer logs
docker-compose logs -f producer-service
```

### 3. View Real-time Dashboard

Open http://localhost:5173 in your browser to see:
- Active rides count
- Total rides
- City-wise statistics
- Real-time updates via WebSocket

## 🐛 Troubleshooting

### Services Not Starting

**Problem**: Services fail to start or exit immediately

**Solutions**:
```bash
# Check logs for errors
docker-compose logs

# Ensure ports are not in use
lsof -i :9092  # Kafka
lsof -i :8081  # Producer
lsof -i :8083  # Dashboard

# Rebuild from scratch
docker-compose down -v
docker-compose up --build
```

### Kafka Connection Issues

**Problem**: Services can't connect to Kafka

**Solutions**:
```bash
# Wait for Kafka to be fully ready (takes ~30-40 seconds)
docker-compose logs kafka

# Check Kafka health
docker-compose exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092

# Restart dependent services
docker-compose restart producer-service processor-service dashboard-service
```

### Frontend Not Loading

**Problem**: Dashboard UI shows blank page or errors

**Solutions**:
```bash
# Check nginx logs
docker-compose logs dashboard-ui

# Verify backend is running
curl http://localhost:8083/actuator/health

# Rebuild frontend
docker-compose up -d --build dashboard-ui
```

### Out of Memory Errors

**Problem**: Services crash with OOM errors

**Solutions**:
```bash
# Increase Docker memory limit (Docker Desktop > Settings > Resources)
# Or reduce JVM heap size in docker-compose.yml:
JAVA_OPTS: "-Xmx256m -Xms128m"
```

### Kafka Topics Not Created

**Problem**: Topics don't exist in Kafdrop

**Solutions**:
```bash
# Kafka auto-creates topics on first message
# Ensure producer is running and sending events
docker-compose logs producer-service

# Manually create topics if needed
docker-compose exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic ride-events \
  --partitions 3 \
  --replication-factor 1
```

## 🔄 Development Workflow

### Local Development with Docker

1. **Run infrastructure only** (Kafka, Kafdrop):
   ```bash
   docker-compose up -d kafka kafdrop
   ```

2. **Run services locally** with IDE/Maven:
   ```bash
   # Services will connect to Docker Kafka on localhost:29092
   mvn spring-boot:run -pl producer-service
   ```

3. **Hot reload frontend**:
   ```bash
   cd dashboard-ui
   npm run dev
   ```

### Making Changes

1. **Backend changes**:
   ```bash
   # Rebuild specific service
   docker-compose up -d --build producer-service
   ```

2. **Frontend changes**:
   ```bash
   # Rebuild UI
   docker-compose up -d --build dashboard-ui
   ```

3. **Configuration changes**:
   ```bash
   # Restart service to pick up new config
   docker-compose restart dashboard-service
   ```

## 📊 Health Checks

All services include health checks:

```bash
# Producer Service
curl http://localhost:8081/actuator/health

# Processor Service
curl http://localhost:8082/actuator/health

# Dashboard Service
curl http://localhost:8083/actuator/health

# Dashboard UI
curl http://localhost:5173
```

## 🔐 Production Considerations

For production deployment, consider:

1. **Security**:
   - Enable Kafka authentication (SASL/SSL)
   - Add API authentication/authorization
   - Use secrets management (Docker secrets, Vault)

2. **Scalability**:
   - Increase Kafka partitions
   - Scale processor service instances
   - Add load balancer for frontend

3. **Monitoring**:
   - Add Prometheus + Grafana
   - Enable Spring Boot Actuator metrics
   - Set up log aggregation (ELK stack)

4. **Persistence**:
   - Use named volumes for Kafka data
   - Configure backup strategies
   - Set appropriate retention policies

## 📝 Additional Resources

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)

## 🆘 Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review service logs: `docker-compose logs -f`
3. Verify all prerequisites are met
4. Ensure sufficient system resources (RAM, disk space)

---

**Happy Streaming! 🚗💨**
