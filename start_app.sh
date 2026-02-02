#!/bin/bash

# Function to kill process on a specific port
kill_port() {
    PORT=$1
    NAME=$2
    PID=$(lsof -t -i:$PORT)
    if [ -n "$PID" ]; then
        echo "Stopping existing $NAME on port $PORT (PID: $PID)..."
        kill -9 $PID
        echo "$NAME stopped."
    else
        echo "No existing $NAME found on port $PORT."
    fi
}

echo "=== Starting DORA Metrics Viewer ==="

# 1. Stop existing processes
kill_port 8080 "Backend"
kill_port 4200 "Frontend"

# 2. Start Backend
echo "Starting Backend..."
cd backend
# using nohup to keep it running if shell closes, and redirecting logs
nohup ./mvnw spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend started with PID $BACKEND_PID. Logs: backend.log"
cd ..

# 3. Start Frontend
echo "Starting Frontend..."
cd frontend
# using nohup and --host 0.0.0.0 to ensure accessibility if needed, though default is localhost
nohup npm start -- --host 0.0.0.0 > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend started with PID $FRONTEND_PID. Logs: frontend.log"
cd ..

echo "=== Application Starting ==="
echo "Please wait a moment for services to initialize."
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:4200"
echo "To stop: Use 'kill $BACKEND_PID' and 'kill $FRONTEND_PID' or run this script again."
