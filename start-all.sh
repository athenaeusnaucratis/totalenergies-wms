#!/bin/bash
cd /home/alpha/wms

# Kill any existing processes
pkill -f 'spring-boot:run' 2>/dev/null || true
pkill -f 'vite' 2>/dev/null || true
sleep 1

# Start backend
cd /home/alpha/wms
mvn spring-boot:run -q > /tmp/wms-backend.log 2>&1 &
BACKEND_PID=
echo "Backend PID: "

# Wait for backend to start
for i in {1..30}; do
    if curl -s http://localhost:8080/api/auth/login > /dev/null 2>&1; then
        echo 'Backend is up'
        break
    fi
    sleep 1
done

# Start frontend
cd /home/alpha/wms/frontend
npx vite --host > /tmp/wms-frontend.log 2>&1 &
FRONTEND_PID=
echo "Frontend PID: "

echo 'Both servers running. Keeping alive...'
wait
