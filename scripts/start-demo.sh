#!/bin/bash
# ==============================================================================
# GramDrishti AI — Live Hackathon Demo Startup Script
# ==============================================================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}==============================================================================${NC}"
echo -e "${GREEN}      GRAMDRISHTI AI — LIVE HACKATHON JUDGE DEMO SERVER INITIALIZATION        ${NC}"
echo -e "${GREEN}==============================================================================${NC}"

# 1. Check Docker Daemon
echo -n "Checking Docker daemon... "
if docker info >/dev/null 2>&1; then
    echo -e "${GREEN}[OK]${NC}"
else
    echo -e "${RED}[FAILED] Please start Docker Desktop before running demo.${NC}"
    exit 1
fi

# 2. Check & Start MySQL Container
echo -n "Checking MySQL 8.0 (Container: gramdrishti-mysql)... "
if docker ps --filter "name=gramdrishti-mysql" --filter "status=running" | grep -q gramdrishti-mysql; then
    echo -e "${GREEN}[RUNNING]${NC}"
else
    echo -e "${YELLOW}[STARTING] Starting container...${NC}"
    docker start gramdrishti-mysql || docker-compose up -d mysql
    sleep 3
fi

# 3. Check Spring Boot Backend
echo -n "Checking Spring Boot Backend API (Port 8080)... "
if curl -s http://localhost:8080/api/health | grep -q "UP"; then
    echo -e "${GREEN}[UP & READY]${NC}"
else
    echo -e "${YELLOW}[STARTING] Launching Spring Boot in background...${NC}"
    cd "$(dirname "$0")/../backend"
    export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/amazon-corretto-11.jdk/Contents/Home}"
    export SPRING_PROFILES_ACTIVE=prod
    nohup mvn spring-boot:run > backend-demo.log 2>&1 &
    echo "Backend process launched (PID: $!). Waiting for health probe..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/health | grep -q "UP"; then
            echo -e "${GREEN}[BACKEND UP & HEALTHY]${NC}"
            break
        fi
        sleep 2
    done
    cd - >/dev/null
fi

# 4. Final Status Summary
echo -e "\n${GREEN}==============================================================================${NC}"
echo -e "  SYSTEM STATUS SUMMARY:"
echo -e "  - MySQL Database:       ${GREEN}PRIVATE (127.0.0.1:3306)${NC}"
echo -e "  - Backend API:          ${GREEN}http://localhost:8080/api/health [UP]${NC}"
echo -e "  - Demo User Account:    ${GREEN}demo.viewer@gramdrishti.gov.in / Password@123${NC}"
echo -e "  - Admin Account:        ${GREEN}admin@gramdrishti.gov.in / Password@123${NC}"
echo -e "${GREEN}==============================================================================${NC}"
echo -e "\n${YELLOW}To expose API securely via Cloudflare Tunnel or localtunnel for Vercel:${NC}"
echo -e "Run: ${GREEN}npx localtunnel --port 8080${NC} OR ${GREEN}cloudflared tunnel --url http://localhost:8080${NC}"
echo -e "Then set ${YELLOW}VITE_API_BASE_URL=<tunnel-url>${NC} in your Vercel Project Settings.\n"
