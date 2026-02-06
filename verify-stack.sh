#!/bin/bash

echo "================================================"
echo "Microservices ELK Stack - Health Check"
echo "================================================"
echo ""

# Function to check if service is up
check_service() {
    local name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo -n "Checking $name..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo " ✓ UP"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    echo " ✗ FAILED"
    return 1
}

# Wait for services
echo "Waiting for services to start..."
echo ""

check_service "Elasticsearch" "http://localhost:9200/_cluster/health"
check_service "Kibana" "http://localhost:5601/api/status"
check_service "Order Service" "http://localhost:8081/api/v1/ping"
check_service "Payment Service" "http://localhost:8082/api/v1/ping"

echo ""
echo "================================================"
echo "Testing endpoints..."
echo "================================================"
echo ""

# Test Order Service
echo "Order Service Response:"
curl -s http://localhost:8081/api/v1/ping | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8081/api/v1/ping
echo ""
echo ""

# Test Payment Service
echo "Payment Service Response:"
curl -s http://localhost:8082/api/v1/ping | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8082/api/v1/ping
echo ""
echo ""

# Check Elasticsearch indices
echo "================================================"
echo "Elasticsearch Indices:"
echo "================================================"
curl -s "http://localhost:9200/_cat/indices/microservices-logs-*?v"
echo ""
echo ""

echo "================================================"
echo "Setup Complete!"
echo "================================================"
echo ""
echo "Access points:"
echo "  • Kibana: http://localhost:5601"
echo "  • Elasticsearch: http://localhost:9200"
echo "  • Order Service: http://localhost:8081/api/v1/ping"
echo "  • Payment Service: http://localhost:8082/api/v1/ping"
echo ""
echo "Next steps:"
echo "  1. Open Kibana at http://localhost:5601"
echo "  2. Create data view: 'microservices-logs-*'"
echo "  3. Go to Discover to see logs"
echo ""
