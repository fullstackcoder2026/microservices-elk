# Spring Boot Microservices with ELK Stack

Complete setup for two Spring Boot microservices (Order Service & Payment Service) with full ELK stack integration, distributed tracing, and request ID tracking.

## Architecture Overview

- **Order Service** (Port 8081): Handles order operations
- **Payment Service** (Port 8082): Handles payment operations
- **Elasticsearch** (Port 9200): Log storage and indexing
- **Logstash** (Port 5000): Log aggregation and processing
- **Kibana** (Port 5601): Log visualization and dashboards

## Features

✅ **Request ID Tracking**: Each request gets a unique ID (UUID) that persists across logs  
✅ **Distributed Tracing**: Micrometer Tracing with Brave (traceId + spanId in all logs)  
✅ **JSON Structured Logging**: Logback with Logstash encoder  
✅ **Real-time Log Shipping**: TCP socket appender to Logstash  
✅ **Scheduled Log Generation**: Random logs every 3-5 seconds simulating business events  
✅ **Multi-level Logging**: INFO/WARN/ERROR logs with context  
✅ **Docker Compose**: Complete local development environment  

## Prerequisites

- Docker Desktop or Docker Engine + Docker Compose
- 8GB RAM recommended for running the full stack
- Ports 5000, 5601, 8081, 8082, 9200, 9300 available

## Quick Start

### 1. Start the Stack

```bash
cd microservices-elk
docker compose up --build
```

This will:
- Build both microservices
- Start Elasticsearch, Logstash, and Kibana
- Start Order Service and Payment Service
- Begin sending logs to ELK

**⏱️ Wait Time**: First startup takes 2-3 minutes for ELK to be fully ready.

### 2. Verify Services

Check that all services are running:

```bash
# Check Elasticsearch
curl http://localhost:9200/_cluster/health

# Check Kibana (should return 200)
curl http://localhost:5601/api/status

# Check Order Service
curl http://localhost:8081/api/v1/ping

# Check Payment Service
curl http://localhost:8082/api/v1/ping
```

### 3. Test the Endpoints

**Order Service:**
```bash
# Without custom Request ID
curl http://localhost:8081/api/v1/ping

# With custom Request ID
curl -H "X-Request-Id: my-custom-request-123" http://localhost:8081/api/v1/ping
```

**Payment Service:**
```bash
# Without custom Request ID
curl http://localhost:8082/api/v1/ping

# With custom Request ID
curl -H "X-Request-Id: my-payment-request-456" http://localhost:8082/api/v1/ping
```

**Expected Response:**
```json
{
  "service": "order-service",
  "status": "ok",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "traceId": "6c3e8f9a7b2d4e5f",
  "spanId": "1a2b3c4d5e6f7890"
}
```

## Kibana Setup

### Step 1: Access Kibana

Open your browser and navigate to:
```
http://localhost:5601
```

### Step 2: Create Index Pattern / Data View

1. Click the **☰ menu** (top left)
2. Navigate to **Management** → **Stack Management**
3. Under **Kibana**, click **Data Views** (or **Index Patterns** in older versions)
4. Click **Create data view**
5. Enter:
   - **Name**: `Microservices Logs`
   - **Index pattern**: `microservices-logs-*`
   - **Timestamp field**: `@timestamp`
6. Click **Save data view to Kibana**

### Step 3: View Logs in Discover

1. Click the **☰ menu** → **Analytics** → **Discover**
2. Select the **Microservices Logs** data view
3. You should see logs streaming in!

**Useful Filters:**
- Filter by service: `service.name : "order-service"`
- Filter by request: `requestId : "your-request-id"`
- Filter by trace: `traceId : "your-trace-id"`
- Filter by log level: `level : "ERROR"`

### Step 4: Create a Dashboard

1. Click the **☰ menu** → **Analytics** → **Dashboard**
2. Click **Create dashboard**
3. Click **Create visualization**

**Visualization 1: Logs Over Time (Line Chart)**
- Visualization type: **Line**
- Vertical axis: **Count**
- Horizontal axis: **@timestamp** (Date Histogram)
- Break down by: **service.name.keyword**
- Click **Save and return**

**Visualization 2: Top Services (Pie Chart)**
- Visualization type: **Pie**
- Slice by: **service.name.keyword** (Top 10)
- Metric: **Count**
- Click **Save and return**

**Visualization 3: Log Level Distribution (Bar Chart)**
- Visualization type: **Bar vertical**
- Horizontal axis: **level.keyword** (Terms)
- Vertical axis: **Count**
- Click **Save and return**

**Visualization 4: Latest Logs Table**
- Visualization type: **Table**
- Rows: **@timestamp** (Date Histogram)
- Split rows by: 
  - `@timestamp`
  - `service.name.keyword`
  - `level.keyword`
  - `requestId.keyword`
  - `traceId.keyword`
  - `spanId.keyword`
  - `message.keyword`
- Click **Save and return**

5. Click **Save** to save the dashboard with a name like "Microservices Overview"

## Log Fields Reference

Each log entry contains:

| Field | Description | Example |
|-------|-------------|---------|
| `@timestamp` | Log timestamp | `2024-02-05T10:30:45.123Z` |
| `service.name` | Service identifier | `order-service` |
| `requestId` | Unique request identifier | `550e8400-e29b-41d4-a716-446655440000` |
| `traceId` | Distributed trace ID | `6c3e8f9a7b2d4e5f` |
| `spanId` | Current span ID | `1a2b3c4d5e6f7890` |
| `level` | Log level | `INFO`, `WARN`, `ERROR` |
| `message` | Log message | `Order created for orderId=ORD-1234` |
| `logger` | Logger class name | `com.example.order.controller.PingController` |
| `thread` | Thread name | `http-nio-8081-exec-1` |

## Scheduled Log Events

Both services automatically generate logs every 3-5 seconds:

**Order Service Events:**
- Order created
- Order validated
- Inventory check completed
- Order confirmed
- Order dispatched
- Order delivered

**Payment Service Events:**
- Payment initiated
- Payment authorized
- Payment captured
- Payment settled
- Refund processed
- Payment failed

## Troubleshooting

### Logs Not Appearing in Kibana

**Check 1: Verify Logstash is receiving logs**
```bash
# Check Logstash logs
docker logs logstash

# Look for "Pipeline started successfully"
```

**Check 2: Verify Elasticsearch has indices**
```bash
curl http://localhost:9200/_cat/indices?v

# Should see indices like: microservices-logs-2024.02.05
```

**Check 3: Verify services can reach Logstash**
```bash
# Check service logs
docker logs order-service
docker logs payment-service

# Should not see connection errors
```

**Check 4: Index pattern matches**
- In Kibana, ensure the data view pattern is `microservices-logs-*` (with asterisk)
- Check the date format matches the index names in Elasticsearch

### Services Not Starting

**Check Docker resources:**
```bash
docker stats

# Ensure sufficient memory is available
```

**Check port conflicts:**
```bash
# Linux/Mac
lsof -i :5000
lsof -i :5601
lsof -i :8081
lsof -i :8082
lsof -i :9200

# Windows
netstat -ano | findstr :5000
```

### Elasticsearch Health Issues

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health?pretty

# Should show status: "green" or "yellow"
```

If status is red:
```bash
# Restart Elasticsearch
docker compose restart elasticsearch
```

### Clean Restart

If things get messy, clean restart:

```bash
# Stop and remove all containers and volumes
docker compose down -v

# Rebuild and start fresh
docker compose up --build
```

## Performance Tuning

### Reduce Log Volume

Edit the `@Scheduled` annotation in `RandomLogGenerator.java`:

```java
// Less frequent logs
@Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
```

### Increase Elasticsearch Memory

Edit `docker-compose.yml`:

```yaml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Increase from 512m
```

### Async Logging Buffer

Edit `logback-spring.xml` to adjust buffer size:

```xml
<appender name="ASYNC_LOGSTASH" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>1024</queueSize>  <!-- Increase from 512 -->
    <discardingThreshold>0</discardingThreshold>
</appender>
```

## Architecture Details

### Log Flow

```
Microservices (Java/Spring Boot)
    ↓ (JSON logs via TCP)
Logstash (Port 5000)
    ↓ (Parsed and enriched)
Elasticsearch (Port 9200)
    ↓ (Query and aggregation)
Kibana (Port 5601)
    → User Dashboard
```

### Tracing Implementation

- **Library**: Micrometer Tracing with Brave
- **Span Creation**: Automatic for HTTP requests, manual for scheduled tasks
- **Context Propagation**: Via MDC (Mapped Diagnostic Context)
- **Sampling**: 100% (all traces captured)

### Request ID Flow

1. HTTP request arrives
2. `RequestIdFilter` extracts or generates `requestId`
3. `requestId` added to MDC
4. All logs in that request context include `requestId`
5. `requestId` returned in response header `X-Request-Id`

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.1
- **Micrometer Tracing**: Brave implementation
- **Logging**: Logback with Logstash encoder 7.4
- **Elasticsearch**: 8.11.3
- **Logstash**: 8.11.3
- **Kibana**: 8.11.3

## Project Structure

```
microservices-elk/
├── docker-compose.yml           # Orchestration
├── logstash/
│   └── pipeline/
│       └── logstash.conf        # Log processing pipeline
├── order-service/               # Order microservice
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/
│       │   └── com/example/order/
│       │       ├── OrderServiceApplication.java
│       │       ├── controller/PingController.java
│       │       ├── filter/RequestIdFilter.java
│       │       └── scheduler/RandomLogGenerator.java
│       └── resources/
│           ├── application.yml
│           └── logback-spring.xml
└── payment-service/             # Payment microservice
    ├── Dockerfile
    ├── pom.xml
    └── src/main/
        ├── java/
        │   └── com/example/payment/
        │       ├── PaymentServiceApplication.java
        │       ├── controller/PingController.java
        │       ├── filter/RequestIdFilter.java
        │       └── scheduler/RandomLogGenerator.java
        └── resources/
            ├── application.yml
            └── logback-spring.xml
```

## Stopping the Stack

```bash
# Stop all services
docker compose down

# Stop and remove volumes (clean state)
docker compose down -v
```

## Next Steps

- Add inter-service communication to see trace propagation
- Implement alerting rules in Kibana
- Add APM (Application Performance Monitoring)
- Integrate with external tracing systems (Zipkin, Jaeger)
- Add authentication and security

## License

MIT License - Feel free to use and modify as needed.
