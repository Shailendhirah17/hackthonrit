# GramDrishti AI — Live Hackathon Demo Runbook

This runbook guides you step-by-step through running the GramDrishti AI server on your laptop, exposing the secure HTTPS backend API via Cloudflare / localtunnel, deploying the frontend to Vercel, and orchestrating the live hackathon judge presentation.

---

## 1. Laptop Server Preparation & Stability Guidelines

To ensure 100% stability during the live evaluation:
1. **Disable Sleep Mode**:
   - macOS: *System Settings $\rightarrow$ Displays $\rightarrow$ Advanced $\rightarrow$ Prevent automatic sleeping when display is off*.
   - Or run in terminal: `caffeinate -d`
2. **Power & Network**:
   - Keep the laptop connected to power.
   - Maintain a stable Wi-Fi or mobile hotspot connection.
   - Do NOT switch networks during active judge testing.
3. **Docker**:
   - Keep Docker Desktop running (`gramdrishti-mysql` on 127.0.0.1:3306).

---

## 2. One-Command Service Management

### 2.1 Start Demo Environment
```bash
./scripts/start-demo.sh
```
*Automatically validates Docker, MySQL, starts Spring Boot with `prod` profile, verifies `http://localhost:8080/api/health`, and checks AI service.*

### 2.2 Inspect Live System Diagnostics
```bash
./scripts/status-demo.sh
```
*Outputs a real-time matrix of MySQL, Backend, AI service, and system resource consumption.*

### 2.3 Hot Component Restart
```bash
# Restart everything
./scripts/restart-demo.sh all

# Or restart backend only
./scripts/restart-demo.sh backend

# Or restart MySQL container only
./scripts/restart-demo.sh mysql
```

### 2.4 Stop Demo Environment
```bash
./scripts/stop-demo.sh
```

---

## 3. Secure Public Tunneling (Connecting to Vercel)

Only the Spring Boot REST API is exposed to the internet. MySQL, Redis, MinIO, and AI FastAPI remain strictly private.

### Option A: Cloudflare Tunnel (Recommended)
```bash
cloudflared tunnel --url http://localhost:8080
```
*Note the public HTTPS URL (e.g. `https://random-subdomain.trycloudflare.com`).*

### Option B: Localtunnel
```bash
npx localtunnel --port 8080
```
*Note the generated public URL (e.g. `https://gentle-tiger-12.loca.lt`).*

---

## 4. Vercel Deployment Instructions

1. Push latest code to GitHub:
   ```bash
   git push origin main
   ```
2. In the [Vercel Dashboard](https://vercel.com/):
   - **Framework Preset**: `Vite`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Environment Variables**:
     ```
     VITE_API_BASE_URL = <your-public-tunnel-url>
     ```
3. Deploy! The judge accesses `https://<your-project>.vercel.app`.

---

## 5. Judge Demo Walkthrough & Tested Journey

When the judge opens the link on their mobile device (iOS Safari or Android Chrome):

1. **Login Page**:
   - Tap **"1-Click Role Login"** $\rightarrow$ select **"Super Admin"** or **"Viewer"**.
   - Or enter `demo.viewer@gramdrishti.gov.in` / `Password@123`.
2. **Executive Dashboard**:
   - Inspect multi-dimensional Habitations scanned, Critical Deficit counts, and Closed-Loop Deficit Reduction metrics (e.g. Thuamul Rampur $-61.1\%$).
3. **GIS Spatial Intelligence**:
   - Tap **"Map"** on the bottom navigation bar.
   - Use the floating search bar or filter by State (e.g. *Maharashtra* or *Odisha*).
   - Tap on **Bhamragad** marker (🔴 High Deficit 86.4).
   - Swipe up the **Village Intelligence Bottom Sheet** to view road connectivity ($24\%$), health access ($18\%$), and piped water coverage ($31\%$).
4. **AI Road & Deficit Triage**:
   - Tap **"Run AI Triage"** to inspect detected road defects (potholes, unpaved kutcha roads) and water contamination parameters (Fluoride/Iron).
5. **Closed-Loop Project Tracking**:
   - Tap **"Projects"** on the bottom navigation bar to review sanctioning, geotagged evidence, and before-vs-after impact re-evaluation.

---

## 6. Failure Recovery Protocols

| Scenario | Symptom | Action |
|:---|:---|:---|
| **Tunnel Disconnected** | Vercel shows "Unable to reach GramDrishti backend" | Re-run `npx localtunnel --port 8080` and update `VITE_API_BASE_URL` on Vercel. |
| **Backend Out of Memory** | `curl http://localhost:8080/api/health` hangs | Run `./scripts/restart-demo.sh backend`. |
| **MySQL Container Paused** | Database connection refused | Run `./scripts/restart-demo.sh mysql`. |
| **CORS Blocked** | Browser console reports CORS error | Ensure `CORS_ALLOWED_ORIGINS` in backend includes your Vercel domain or `https://*.vercel.app`. |
