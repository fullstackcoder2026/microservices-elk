# 🚀 Getting Started - Microservices ELK Stack

## What You Have

A complete Spring Boot microservices logging system with:
- **2 Microservices** (Order & Payment)
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- **Distributed Tracing** (Micrometer + Brave)
- **Request Correlation** (requestId tracking)
- **Auto Log Generation** (Scheduled events)

## Prerequisites Check

Before starting, ensure you have:
- [ ] Docker Desktop installed (or Docker + Docker Compose)
- [ ] At least 8GB RAM available
- [ ] Ports available: 5000, 5601, 8081, 8082, 9200

## Setup (5 Minutes)

### Step 1: Navigate to Project Directory

```bash
cd microservices-elk
```

### Step 2: Start Everything

```bash
docker compose up --build
```

**What happens:**
- Docker builds both microservices (2-3 minutes first time)
- Starts Elasticsearch, Logstash, Kibana
- Services start logging automatically
- Logs flow to Kibana

**Wait for this message:**
```
order-service    | Started OrderServiceApplication
payment-service  | Started PaymentServiceApplication
```

### Step 3: Verify Services (Optional)

Open a new terminal and run:
```bash
./verify-stack.sh
```

This checks all services are up and running.

## Your First Logs

### 1. Access Kibana

Open browser: **http://localhost:5601**

### 2. Create Data View

1. Click **☰** menu (top left)
2. Go to **Management** → **Stack Management**
3. Click **Data Views** (under Kibana section)
4. Click **Create data view**
5. Fill in:
   - **Name**: `Microservices Logs`
   - **Index pattern**: `microservices-logs-*`
   - **Timestamp field**: `@timestamp`
6. Click **Save data view to Kibana**

### 3. View Logs

1. Click **☰** menu → **Analytics** → **Discover**
2. Select **Microservices Logs** data view
3. **You should see logs!** 🎉

The services are already generating logs automatically.

## Make Your First Request

### Test Order Service

```bash
curl http://localhost:8081/api/v1/ping
```

**Response:**
```json
{
  "service": "order-service",
  "status": "ok",
  "requestId": "a1b2c3d4-e5f6-4789-a012-3456789abcde",
  "traceId": "6c3e8f9a7b2d4e5f",
  "spanId": "1a2b3c4d5e6f7890"
}
```

### Test Payment Service

```bash
curl http://localhost:8082/api/v1/ping
```

### Find Your Request in Kibana

1. Copy the `requestId` from the response
2. In Kibana Discover, click **Add filter**
3. Field: `requestId`, Operator: `is`, Value: `paste-your-requestId`
4. Click **Update**
5. You'll see exactly 3 logs for that request:
   - "Received ping request"
   - "Business step simulated"
   - "Sending response"

## Generate Test Traffic

Run the sample request generator:

```bash
./generate-sample-requests.sh
```

This creates 10 requests to each service. Watch them appear in Kibana!

## Explore the Logs

### Filter by Service

In Kibana, use KQL (Kibana Query Language):

```
service.name : "order-service"
```

### Filter by Log Level

```
level : "ERROR"
```

### Combine Filters

```
service.name : "payment-service" AND level : "WARN"
```

### Search Messages

```
message : *payment*
```

## Create Your First Dashboard

### Quick Dashboard

1. **☰** menu → **Analytics** → **Dashboard**
2. Click **Create dashboard**
3. Click **Create visualization**

**Add a time chart:**
- Visualization type: **Line**
- Vertical axis: **Count**
- Horizontal axis: **@timestamp**
- Break down by: **service.name.keyword**
- Click **Save and return**

4. Click **Save** to save dashboard

## What's Happening Automatically

### Scheduled Logs

Both services generate logs every 3-5 seconds:

**Order Service logs:**
- "Order created"
- "Order validated"
- "Inventory check completed"
- "Order confirmed"
- etc.

**Payment Service logs:**
- "Payment initiated"
- "Payment authorized"
- "Payment captured"
- etc.

These appear automatically in Kibana!

### Log Levels

You'll see a mix of:
- 🟢 **INFO** - Normal operations
- 🟡 **WARN** - Warnings (potential delays)
- 🔴 **ERROR** - Errors (validation failures)

## Understanding the Fields

Each log has:

| Field | What It Means | Example |
|-------|---------------|---------|
| `service.name` | Which service created the log | `order-service` |
| `requestId` | Unique ID for tracking a request | `a1b2c3d4...` |
| `traceId` | Distributed trace identifier | `6c3e8f9a...` |
| `spanId` | Current operation in the trace | `1a2b3c4d...` |
| `level` | Log severity | `INFO`, `WARN`, `ERROR` |
| `message` | The actual log content | `Order created for orderId=ORD-1234` |

## Common Questions

### Q: Why do I see logs already?

A: Both services automatically generate logs every few seconds to simulate activity.

### Q: How do I stop everything?

A: Press `Ctrl+C` in the terminal where you ran `docker compose up`, or run:
```bash
docker compose down
```

### Q: How do I restart?

A: Just run `docker compose up` again (no need for `--build` if code hasn't changed)

### Q: Where can I see errors?

A: In Kibana Discover, filter by `level : "ERROR"`

### Q: Can I use custom request IDs?

A: Yes! Use the `X-Request-Id` header:
```bash
curl -H "X-Request-Id: my-custom-id-123" http://localhost:8081/api/v1/ping
```

### Q: How do I track a request across services?

A: Use the `traceId` - all operations in a distributed transaction share the same `traceId`

## Next Steps

### Learn More

- Read **README.md** for detailed documentation
- Check **QUICK_REFERENCE.md** for commands and queries
- See **ARCHITECTURE.md** for system design
- Review **PROJECT_SUMMARY.md** for complete features

### Customize

- Modify log frequency in `RandomLogGenerator.java`
- Add more endpoints in the controller classes
- Create custom dashboard visualizations
- Add alerts for error conditions

### Extend

- Add inter-service communication
- Implement trace propagation between services
- Add metrics collection
- Connect to external tracing systems

## Troubleshooting

### Logs not showing?

1. Wait 30-60 seconds after startup
2. Check services are running: `docker compose ps`
3. Verify Logstash: `docker logs logstash`
4. Run health check: `./verify-stack.sh`

### Services won't start?

1. Check Docker is running
2. Ensure ports aren't in use: `lsof -i :8081`
3. Try clean restart: `docker compose down -v && docker compose up --build`

### Need help?

Check the comprehensive **README.md** file for detailed troubleshooting.

## Stopping the Stack

```bash
# Stop services (data preserved)
docker compose down

# Stop and remove all data
docker compose down -v
```

## Success Checklist

After following this guide, you should have:
- [x] All services running
- [x] Kibana accessible at http://localhost:5601
- [x] Data view created
- [x] Logs visible in Discover
- [x] Made test requests
- [x] Found logs by requestId
- [x] Created a simple dashboard

---

**🎉 Congratulations! You have a working microservices logging system!**

For more advanced usage, explore the other documentation files in this project.
