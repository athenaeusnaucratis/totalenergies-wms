#!/bin/bash
# Kill existing servers
pkill -f 'spring-boot:run' 2>/dev/null || true
pkill -f 'wms-0.0.1' 2>/dev/null || true
pkill -f 'node.*vite' 2>/dev/null || true
sleep 2

# Reset database for clean migration
sudo service postgresql start
sudo -u postgres psql -c "DROP DATABASE IF EXISTS wms;" 2>/dev/null
sudo -u postgres psql -c "CREATE DATABASE wms OWNER wms;"

# Compile and start backend
cd /home/alpha/wms
mvn compile -q 2>&1
mvn spring-boot:run > /tmp/wms-backend.log 2>&1 &

# Start frontend
cd /home/alpha/wms/frontend
npx vite --host 0.0.0.0 > /tmp/wms-frontend.log 2>&1 &

# Wait for backend
echo "Waiting for backend..."
for i in $(seq 1 90); do
  if curl -s http://localhost:8080/api/auth/login -o /dev/null 2>/dev/null; then
    echo "Backend UP on http://localhost:8080"
    break
  fi
  sleep 2
done

# Wait for frontend
echo "Waiting for frontend..."
for i in $(seq 1 30); do
  if curl -s http://localhost:5173 -o /dev/null 2>/dev/null; then
    echo "Frontend UP on http://localhost:5173"
    break
  fi
  sleep 2
done

# Register a default admin user
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","fullName":"Admin User"}' > /dev/null

echo ""
echo "=== READY ==="
echo "Frontend: http://localhost:5173"
echo "Login: admin / admin123"
echo ""
echo "Servers running. Keeping shell alive..."
wait
