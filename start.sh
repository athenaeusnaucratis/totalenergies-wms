#!/bin/bash
sudo service postgresql start

# Kill any existing instances
pkill -f 'spring-boot:run' 2>/dev/null || true
pkill -f 'wms-0.0.1' 2>/dev/null || true
pkill -f 'vite' 2>/dev/null || true
sleep 2

cd /home/alpha/wms
mvn spring-boot:run > /tmp/wms-backend.log 2>&1 &

cd /home/alpha/wms/frontend
npx vite --host 0.0.0.0 > /tmp/wms-frontend.log 2>&1 &

echo "Waiting for backend..."
for i in $(seq 1 90); do
  if curl -s http://localhost:8080/api/auth/login -o /dev/null 2>/dev/null; then
    echo "Backend is UP on http://localhost:8080"
    break
  fi
  sleep 2
done

echo "Waiting for frontend..."
for i in $(seq 1 30); do
  if curl -s http://localhost:5173 -o /dev/null 2>/dev/null; then
    echo "Frontend is UP on http://localhost:5173"
    break
  fi
  sleep 2
done

echo "Both servers running. Open http://localhost:5173 in your browser."
echo "Press Ctrl+C to stop."
wait
