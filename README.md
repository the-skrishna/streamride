# StreamRide - Real-time Ride Analytics Platform

A real-time ride analytics platform built with Kafka Streams, Spring Boot, and React.

## 🚀 Quick Start with Docker

The easiest way to run the entire StreamRide application is using Docker Compose.

### Prerequisites

- Docker (20.10+)
- Docker Compose (2.0+)
- 4GB+ RAM available

### Start the Application

```bash
# Clone the repository
git clone <your-repo-url>
cd streamride

# Start all services
docker-compose up -d --build

# Or use the interactive script
./start.sh
```

### Access the Services

Once running, access:

- **Dashboard UI**: http://localhost:5173 - Real-time analytics dashboard
- **Kafdrop**: http://localhost:9000 - Kafka topic browser
- **Producer API**: Running on port: 8081 - Ride event generator
- **Processor API**: Running on port: 8082 - Stream processor
- **Dashboard API**: Running on port: 8083 - Backend API

### Stop the Application

```bash
docker-compose down

# Or to remove all data
docker-compose down -v
```

## 📚 Documentation

For detailed setup instructions, troubleshooting, and development guide, see:

- **[DOCKER_SETUP.md](DOCKER_SETUP.md)** - Complete Docker setup guide

## 🏗️ Architecture

```
Producer Service → Kafka (KRaft) → Processor Service → Dashboard Service → React UI
                        ↓
                   Kafdrop (Monitoring)
```

**Note**: This setup uses Kafka in KRaft mode (no Zookeeper required), which is the modern approach for Kafka deployments.

### Services

- **Producer Service** (Port 8081): Generates simulated ride events
- **Processor Service** (Port 8082): Kafka Streams processing for analytics
- **Dashboard Service** (Port 8083): WebSocket server for real-time updates
- **Dashboard UI** (Port 5173): React frontend with live metrics
- **Kafka (KRaft Mode)**: Message broker infrastructure (no Zookeeper needed)
- **Kafdrop** (Port 9000): Web UI for Kafka monitoring

## 🛠️ Development

### Local Development (without Docker)

1. **Start Kafka infrastructure**:
   ```bash
   brew services start kafka (via terminal)
   ```

2. **Run backend services**:
   ```bash
   # Build all modules
   mvn clean install
   
   # Run each service
   mvn spring-boot:run -pl producer-service
   mvn spring-boot:run -pl processor-service
   mvn spring-boot:run -pl dashboard-service
   ```

3. **Run frontend**:
   ```bash
   cd dashboard-ui
   npm install
   npm run dev
   ```

### Project Structure

```
streamride/
├── commons/              # Shared models and utilities
├── producer-service/     # Ride event producer
├── processor-service/    # Kafka Streams processor
├── dashboard-service/    # Backend API + WebSocket
├── dashboard-ui/         # React frontend
├── docker-compose.yml    # Docker orchestration
└── DOCKER_SETUP.md      # Detailed setup guide
```

## 🧪 Testing

```bash
# Check service health/running status
curl http://localhost:8081/api/producer/status
curl http://localhost:8083/health

# View Kafka topics in Kafdrop
open http://localhost:9000

# View real-time dashboard
open http://localhost:5173
```

## 📊 Features

- ✅ Real-time ride event streaming
- ✅ Kafka Streams processing for analytics
- ✅ WebSocket-based live dashboard
- ✅ City-wise ride statistics
- ✅ Active ride tracking
- ✅ Containerized deployment
- ✅ Scalable microservices architecture

## 🐛 Troubleshooting

See [DOCKER_SETUP.md](DOCKER_SETUP.md) for detailed troubleshooting guide.

Common issues:

- **Services not starting**: Wait 30-60 seconds for Kafka to initialize
- **Port conflicts**: Check if ports 5173, 8081-8083, 9000, 9092 are available
- **Out of memory**: Increase Docker memory limit to 4GB+

---

**Built with ❤️ using Kafka, Spring Boot, and React**
