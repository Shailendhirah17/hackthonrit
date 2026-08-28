#!/bin/bash
# ==============================================================================
# GramDrishti AI — Restart Components Script
# ==============================================================================

TARGET=${1:-all}

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

case "$TARGET" in
    mysql)
        echo -e "${YELLOW}Restarting MySQL 8.0 container...${NC}"
        docker restart gramdrishti-mysql
        echo -e "${GREEN}MySQL restarted.${NC}"
        ;;
    backend)
        echo -e "${YELLOW}Restarting Spring Boot Backend...${NC}"
        pkill -f "gramdrishti" || true
        pkill -f "spring-boot:run" || true
        sleep 2
        "$(dirname "$0")/start-demo.sh"
        ;;
    all|*)
        echo -e "${YELLOW}Restarting all GramDrishti demo services...${NC}"
        pkill -f "gramdrishti" || true
        pkill -f "spring-boot:run" || true
        docker restart gramdrishti-mysql || true
        sleep 2
        "$(dirname "$0")/start-demo.sh"
        ;;
esac
