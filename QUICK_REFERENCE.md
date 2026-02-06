# Quick Reference Guide

## 🚀 Quick Start Commands

```bash
# Start everything
docker compose up --build

# Start in background
docker compose up --build -d

# Stop everything
docker compose down

# Clean restart (removes all data)
docker compose down -v && docker compose up --build

# View logs
docker compose logs -f order-service
docker compose logs -f payment-service
docker compose logs -f logstash
```

## 🔗 URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Kibana | http://localhost:5601 | Log visualization |
| Elasticsearch | http://localhost:9200 | Log storage |
| Order Service | http://localhost:8081/api/v1/ping | Order API |
| Payment Service | http://localhost:8082/api/v1/ping | Payment API |
| Logstash | tcp://localhost:5000 | Log ingestion |

## 📝 Testing Commands

```bash
# Test Order Service
curl http://localhost:8081/api/v1/ping

# Test with custom Request ID
curl -H "X-Request-Id: my-test-123" http://localhost:8081/api/v1/ping

# Test Payment Service
curl http://localhost:8082/api/v1/ping

# Generate sample load
./generate-sample-requests.sh

# Verify all services
./verify-stack.sh
```

## 🔍 Kibana Queries

### Common Filters

```
# Filter by service
service.name : "order-service"

# Filter by log level
level : "ERROR"

# Filter by request ID
requestId : "550e8400-e29b-41d4-a716-446655440000"

# Filter by trace ID
traceId : "6c3e8f9a7b2d4e5f"

# Search for specific message
message : "Order created"

# Combine filters (AND)
service.name : "order-service" AND level : "ERROR"

# Combine filters (OR)
level : "ERROR" OR level : "WARN"
```

### KQL Examples

```
# Find all errors in the last hour
level: "ERROR" and @timestamp > now-1h

# Find specific request flow
requestId: "my-test-123"

# Find payment failures
service.name: "payment-service" AND message: *failed*

# Find high-value payments
service.name: "payment-service" AND message: *amount*

# Exclude INFO logs
NOT level: "INFO"
```

## 📊 Log Fields

```json
{
  "@timestamp": "2024-02-05T10:30:45.123Z",
  "service.name": "order-service",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "traceId": "6c3e8f9a7b2d4e5f",
  "spanId": "1a2b3c4d5e6f7890",
  "level": "INFO",
  "message": "Order created for orderId=ORD-1234",
  "logger": "com.example.order.controller.PingController",
  "thread": "http-nio-8081-exec-1"
}
```

## 🐛 Troubleshooting Commands

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health?pretty

# List all indices
curl http://localhost:9200/_cat/indices?v

# Check specific index
curl http://localhost:9200/microservices-logs-*/_count

# View Logstash stats
curl http://localhost:9600/_node/stats?pretty

# Check container status
docker compose ps

# View container logs
docker logs elasticsearch
docker logs logstash
docker logs kibana
docker logs order-service
docker logs payment-service

# Restart specific service
docker compose restart order-service

# Check resource usage
docker stats
```

## 🔧 Common Issues

### Issue: Logs not appearing in Kibana

**Solution 1**: Wait 30-60 seconds after startup  
**Solution 2**: Check if index pattern is created correctly  
**Solution 3**: Verify Logstash connection:
```bash
docker logs logstash | grep "Pipeline started"
```

### Issue: Services can't connect to Logstash

**Solution**: Ensure Logstash is fully started:
```bash
docker compose logs logstash | grep "Successfully started"
```

### Issue: Out of memory

**Solution**: Increase Docker resources or reduce Elasticsearch memory:
```yaml
# In docker-compose.yml
- "ES_JAVA_OPTS=-Xms256m -Xmx256m"
```

### Issue: Port already in use

**Solution**: Find and kill the process:
```bash
# Linux/Mac
lsof -ti:8081 | xargs kill -9

# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

## 📦 Ports Reference

| Port | Service | Protocol |
|------|---------|----------|
| 5000 | Logstash TCP | TCP |
| 5601 | Kibana Web UI | HTTP |
| 8081 | Order Service API | HTTP |
| 8082 | Payment Service API | HTTP |
| 9200 | Elasticsearch API | HTTP |
| 9300 | Elasticsearch Transport | TCP |
| 9600 | Logstash Monitoring | HTTP |

## 🎯 Performance Tips

1. **Reduce log frequency**: Increase scheduled delay in `RandomLogGenerator.java`
2. **Increase buffer size**: Modify `queueSize` in `logback-spring.xml`
3. **Disable console logging**: Comment out `CONSOLE` appender in production
4. **Use async logging**: Already configured via `ASYNC_LOGSTASH` appender
5. **Tune Elasticsearch**: Adjust heap size based on available RAM

## 📚 Additional Resources

- [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)
- [Micrometer Tracing](https://micrometer.io/docs/tracing)
- [Elastic Stack Documentation](https://www.elastic.co/guide/index.html)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)
