#!/bin/bash

echo "================================================"
echo "Generating Sample Requests"
echo "================================================"
echo ""

# Generate 10 requests to each service
for i in {1..10}; do
    echo "Request #$i"
    
    # Order Service
    echo -n "  Order Service: "
    RESPONSE=$(curl -s -H "X-Request-Id: test-order-$i" http://localhost:8081/api/v1/ping)
    REQUEST_ID=$(echo $RESPONSE | grep -o '"requestId":"[^"]*"' | cut -d'"' -f4)
    echo "RequestId: $REQUEST_ID"
    
    # Payment Service
    echo -n "  Payment Service: "
    RESPONSE=$(curl -s -H "X-Request-Id: test-payment-$i" http://localhost:8082/api/v1/ping)
    REQUEST_ID=$(echo $RESPONSE | grep -o '"requestId":"[^"]*"' | cut -d'"' -f4)
    echo "RequestId: $REQUEST_ID"
    
    echo ""
    sleep 1
done

echo "================================================"
echo "Sample requests completed!"
echo "================================================"
echo ""
echo "Check Kibana to see these logs:"
echo "  • Filter by: requestId : test-order-* OR requestId : test-payment-*"
echo "  • Or search for: 'Received ping request'"
echo ""
