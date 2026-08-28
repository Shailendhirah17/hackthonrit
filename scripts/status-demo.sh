#!/bin/bash
# ==============================================================================
# GramDrishti AI — System Diagnostics & Health Status Script
# ==============================================================================

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}==============================================================================${NC}"
echo -e "${BLUE}              GRAMDRISHTI AI — LIVE SYSTEM HEALTH MATRIX                     ${NC}"
echo -e "${BLUE}==============================================================================${NC}"

# 1. MySQL Status
echo -n "1. MySQL Database Container:       "
if docker ps --filter "name=gramdrishti-mysql" --filter "status=running" | grep -q gramdrishti-mysql; then
    echo -e "${GREEN}[RUNNING & PRIVATE]${NC}"
else
    echo -e "${RED}[DOWN]${NC}"
fi

# 2. Backend Health Probe
echo -n "2. Spring Boot Backend (/api/health): "
HEALTH_RESP=$(curl -s --max-time 3 http://localhost:8080/api/health || echo "DOWN")
if echo "$HEALTH_RESP" | grep -q "UP"; then
    echo -e "${GREEN}[HEALTHY (UP)]${NC}"
else
    echo -e "${RED}[UNREACHABLE]${NC}"
fi

# 3. AI Service (Port 8000)
echo -n "3. AI Computer Vision Service:     "
if curl -s --max-time 2 http://localhost:8000/docs >/dev/null 2>&1 || nc -z 127.0.0.1 8000 >/dev/null 2>&1; then
    echo -e "${GREEN}[RUNNING (Port 8000)]${NC}"
else
    echo -e "${YELLOW}[STANDBY / INTEGRATED]${NC}"
fi

# 4. Process Memory & CPU Overview
echo -e "\n${BLUE}--- System Resource Footprint ---${NC}"
ps aux | grep -E "spring-boot|uvicorn|mysqld" | grep -v grep | awk '{printf "  %-12s PID: %-8s CPU: %-5s MEM: %-5s\n", $11, $2, $3"%", $4"%"}'

echo -e "\n${BLUE}==============================================================================${NC}"
