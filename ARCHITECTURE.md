# Architecture Diagram

## System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        C[Client/Browser]
    end
    
    subgraph "Microservices Layer"
        OS[Order Service<br/>:8081]
        PS[Payment Service<br/>:8082]
    end
    
    subgraph "ELK Stack"
        LS[Logstash<br/>:5000]
        ES[Elasticsearch<br/>:9200]
        KB[Kibana<br/>:5601]
    end
    
    C -->|HTTP GET /api/v1/ping| OS
    C -->|HTTP GET /api/v1/ping| PS
    
    OS -->|JSON Logs via TCP| LS
    PS -->|JSON Logs via TCP| LS
    
    LS -->|Parsed Logs| ES
    KB -->|Query| ES
    C -->|View Logs| KB
    
    style OS fill:#a8d5ba
    style PS fill:#a8d5ba
    style LS fill:#ffd89b
    style ES fill:#ffd89b
    style KB fill:#ffd89b
```

## Log Flow

```mermaid
sequenceDiagram
    participant Client
    participant OrderService
    participant Logstash
    participant Elasticsearch
    participant Kibana
    
    Client->>OrderService: GET /api/v1/ping
    activate OrderService
    
    Note over OrderService: Generate requestId<br/>Create trace span
    OrderService->>OrderService: Log: "Received request"
    OrderService-->>Logstash: JSON log via TCP
    
    Note over OrderService: Process business logic
    OrderService->>OrderService: Log: "Business step"
    OrderService-->>Logstash: JSON log via TCP
    
    OrderService->>OrderService: Log: "Sending response"
    OrderService-->>Logstash: JSON log via TCP
    
    OrderService->>Client: Response with requestId, traceId, spanId
    deactivate OrderService
    
    Logstash->>Elasticsearch: Store logs in index
    Kibana->>Elasticsearch: Query logs
    Elasticsearch->>Kibana: Return results
    Client->>Kibana: View logs in dashboard
```

## Request ID & Trace Flow

```mermaid
graph LR
    A[HTTP Request] -->|X-Request-Id header| B{RequestId exists?}
    B -->|Yes| C[Use provided requestId]
    B -->|No| D[Generate UUID]
    C --> E[Add to MDC]
    D --> E
    E --> F[Create/Get Trace Span]
    F --> G[Extract traceId & spanId]
    G --> H[Add to MDC]
    H --> I[Log messages include all context]
    I --> J[Response includes requestId]
    
    style E fill:#e1f5c4
    style H fill:#e1f5c4
    style I fill:#ffe66d
```

## Scheduled Log Generation

```mermaid
graph TB
    A[Scheduler triggers every 3-5s]
    A --> B[Generate new requestId UUID]
    B --> C[Create new trace span]
    C --> D[Extract traceId & spanId]
    D --> E[Add all to MDC]
    E --> F[Select random event]
    F --> G[Select random log level]
    G --> H{Log Level}
    H -->|INFO| I[log.info]
    H -->|WARN| J[log.warn]
    H -->|ERROR| K[log.error]
    I --> L[Send to Logstash]
    J --> L
    K --> L
    L --> M[Clean up MDC]
    
    style B fill:#a8d5ba
    style C fill:#a8d5ba
    style E fill:#ffd89b
```

## Technology Stack

```mermaid
graph TB
    subgraph "Application Layer"
        J[Java 17]
        SB[Spring Boot 3.2.1]
        MT[Micrometer Tracing<br/>Brave]
    end
    
    subgraph "Logging Layer"
        LB[Logback]
        LSE[Logstash Encoder 7.4]
    end
    
    subgraph "Infrastructure"
        D[Docker Compose]
        E[Elasticsearch 8.11]
        L[Logstash 8.11]
        K[Kibana 8.11]
    end
    
    J --> SB
    SB --> MT
    SB --> LB
    LB --> LSE
    LSE --> L
    L --> E
    K --> E
    D --> E
    D --> L
    D --> K
    
    style SB fill:#6fbf73
    style MT fill:#6fbf73
    style LSE fill:#ffd166
    style E fill:#f77f00
    style L fill:#f77f00
    style K fill:#f77f00
```

## Data Model

### Log Entry Structure

```json
{
  "@timestamp": "ISO8601 datetime",
  "service.name": "string",
  "requestId": "UUID string",
  "traceId": "hex string",
  "spanId": "hex string",
  "level": "INFO | WARN | ERROR",
  "message": "string",
  "logger": "fully.qualified.class.name",
  "thread": "thread-name",
  "stack_trace": "string (only on ERROR)"
}
```

### Key Features

- **Consistent Field Names**: All services use same field structure
- **Trace Context Propagation**: traceId/spanId maintained across services
- **Request Correlation**: requestId for tracking user requests
- **Time-based Indexing**: Daily indices for efficient querying
- **Async Processing**: Non-blocking log shipping to Logstash
