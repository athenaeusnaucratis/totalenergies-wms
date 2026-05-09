#!/bin/bash
set -e

sudo service postgresql start

cd /home/alpha/wms
mvn compile -q
mvn spring-boot:run &
BPID=$!

cd /home/alpha/wms/frontend
npx vite --host 0.0.0.0 &
FPID=$!

echo "Waiting for backend..."
for i in $(seq 1 60); do
  if curl -s http://localhost:8080/api/auth/login -o /dev/null 2>/dev/null; then
    echo "Backend UP"
    break
  fi
  sleep 2
done

echo "Waiting for frontend..."
for i in $(seq 1 20); do
  if curl -s http://localhost:5173 -o /dev/null 2>/dev/null; then
    echo "Frontend UP"
    break
  fi
  sleep 2
done

# Register admin
curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123","fullName":"Admin"}' || true

# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')

echo "=== Product count ==="
curl -s http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" | python3 -c 'import sys,json; print(len(json.load(sys.stdin)),"products")'

echo "=== Dashboard stats ==="
curl -s http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

echo "=== Low stock count ==="
curl -s http://localhost:8080/api/dashboard/low-stock \
  -H "Authorization: Bearer $TOKEN" | python3 -c 'import sys,json; print(len(json.load(sys.stdin)),"low stock items")'

echo ""
echo "READY - http://localhost:5173 (admin/admin123)"

wait
