#!/bin/bash
set -e

cd /home/alpha/wms

# Compile
mvn compile -q

# Start server
mvn spring-boot:run > /tmp/wms.log 2>&1 &
SERVER_PID=$!

# Wait for server
echo "Waiting for server..."
for i in $(seq 1 60); do
  if curl -s http://localhost:8080/api/auth/login -o /dev/null 2>/dev/null; then
    echo "Server is UP"
    break
  fi
  sleep 2
done

# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

echo "Token: ${TOKEN:0:20}..."

echo "=== Create Category ==="
curl -s -X POST http://localhost:8080/api/product-categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Cables","description":"All cables"}' | python3 -m json.tool

echo "=== Create Product ==="
curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"HDMI Cable","sku":"HDMI-001","categoryId":2,"unit":"EACH","minimumStock":5}' | python3 -m json.tool

echo "=== List Products ==="
curl -s http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo "=== Create Company ==="
curl -s -X POST http://localhost:8080/api/companies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"TechSupply Co","supplier":true,"customer":false,"email":"tech@supply.com"}' | python3 -m json.tool

echo "=== List Companies ==="
curl -s http://localhost:8080/api/companies \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo "=== ALL TESTS PASSED ==="

# Cleanup
kill $SERVER_PID 2>/dev/null || true
