# GramDrishti AI — Mobile Judge Demo Verification Checklist

Use this checklist before and during the live hackathon demonstration to ensure a flawless mobile evaluation experience.

---

## 1. Pre-Flight Server Checks (Laptop)

- [x] **Laptop Sleep Disabled**: `caffeinate -d` or macOS sleep settings set to *Never Sleep*.
- [x] **Power Connected**: Laptop charger plugged in.
- [x] **Docker Running**: `docker ps` shows `gramdrishti-mysql` up and healthy.
- [x] **MySQL Private**: MySQL bound to internal container network / `127.0.0.1:3306` (No public port forward).
- [x] **AI FastAPI Private**: Port 8000 accessible only via Spring Boot internal client.
- [x] **Spring Boot Backend Running**: `GET http://localhost:8080/api/health` returns `{"status":"UP"}`.
- [x] **Database Seeded**: All 7 roles, demo judge accounts, 18,802 villages, 600,000 habitations, and 4,412 water contamination records available.
- [x] **Tunnel Active**: Public HTTPS tunnel URL generating valid responses.

---

## 2. Frontend & Vercel Checks

- [x] **Vercel Build Succeeded**: `npm run build` generates split bundles with zero errors.
- [x] **Environment Variable Configured**: `VITE_API_BASE_URL` matches the active HTTPS tunnel URL.
- [x] **CORS Working**: Backend accepts requests from `https://*.vercel.app` with credentials.
- [x] **Route-Based Lazy Loading**: Fast initial page load (< 2s on 4G mobile networks).
- [x] **Zero Raw Secrets in Frontend**: No database passwords or JWT signing keys committed.

---

## 3. Mobile Device Responsiveness (320px – 430px)

- [x] **No Horizontal Scroll**: Viewport meta tag properly configured; cards and containers wrap cleanly.
- [x] **Bottom Navigation Bar**: Fixed bottom bar with 5 thumb-friendly actions (`Home`, `Map`, `AI Gap`, `Projects`, `More`).
- [x] **Touch Targets $\ge 48\text{px}$**: All buttons, inputs, and tab triggers meet accessible mobile tap standards.
- [x] **GIS Mobile Map**: Full-screen mobile viewport with floating search, filter button, and swipeable Village Intelligence Bottom Sheet.
- [x] **Mobile Keyboard Friendly**: Inputs use `inputMode="email"`, `autoComplete="email"`, and proper labels.
- [x] **Network Error Resilience**: Friendly error states and retry buttons when latency spikes occur.

---

## 4. Judge Walkthrough Flow

- [x] **Step 1 — Login**: 1-Click login or `demo.viewer@gramdrishti.gov.in` (`Password@123`).
- [x] **Step 2 — Dashboard**: Review high-level Habitations Scanned, Critical Deficits (🔴 $\ge 75$), and Closed-Loop Deficit Reduction ($54.8\%$).
- [x] **Step 3 — GIS Spatial Explorer**: Locate critical habitations (e.g. *Bhamragad*, *Thuamul Rampur*, *Darbha*).
- [x] **Step 4 — Village Intelligence Sheet**: Review multi-sector indicators (Roads, Drinking Water, Health, Digital).
- [x] **Step 5 — AI Triage**: Inspect detected road obstacles (potholes, unpaved kutcha roads) and water quality contamination.
- [x] **Step 6 — Project Sanction & Impact**: Inspect project lifecycle, geotagged evidence, and measured post-intervention gap score drop.
