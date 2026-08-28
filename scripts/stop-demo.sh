#!/bin/bash
# ==============================================================================
# GramDrishti AI — Stop Demo Services Script
# ==============================================================================

YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${YELLOW}Stopping GramDrishti AI local demo services...${NC}"
pkill -f "gramdrishti" || true
pkill -f "spring-boot:run" || true
pkill -f "localtunnel" || true
pkill -f "cloudflared" || true

echo -e "${GREEN}All background processes stopped cleanly.${NC}"
