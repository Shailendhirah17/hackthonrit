# GramDrishti AI — Rural Infrastructure Gap Intelligence Platform

An AI-powered rural infrastructure gap intelligence and project lifecycle tracking platform for India. The system maps, collects, analyzes, and prioritizes gaps in rural infrastructure (roads, healthcare, education, water/sanitation, digital telecom, power grids, market access) using AI/computer vision + GIS, ranks habitations for intervention, and measures closed-loop impact before and after intervention.

---

## System Architecture

```
                               ┌────────────────────────┐
                               │  Nginx Reverse Proxy   │ (Port 80)
                               └───────────┬────────────┘
                                           │
                    ┌──────────────────────┴──────────────────────┐
                    ▼                                             ▼
        ┌───────────────────────┐                     ┌───────────────────────┐
        │  React + GIS Frontend │                     │ Spring Boot Backend   │ (Port 8080)
        │  (Leaflet + Tailwind) │                     │ (JWT + RBAC + JPA)    │
        └───────────────────────┘                     └───────────┬───────────┘
                                                                  │
                                      ┌───────────────────────────┼───────────────────────────┐
                                      ▼                           ▼                           ▼
                          ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐
                          │ FastAPI AI / CV Engine│   │ MySQL 8+ Database     │   │ MinIO Object Storage  │
                          │ (Port 8000)           │   │ (Port 3306)           │   │ (Port 9000/9001)      │
                          └───────────────────────┘   └───────────────────────┘   └───────────────────────┘
```

---

## Tech Stack

- **Frontend**: React 18, React Router v6, Axios, TanStack Query v5, Zustand, Tailwind CSS, Recharts, Leaflet GIS, React Hook Form, Lucide React
- **Backend**: Java Spring Boot (2.7.18 / 3.x), Spring Security, Spring Data JPA / Hibernate, Bean Validation, JJWT, Lombok, Springdoc OpenAPI/Swagger
- **Database**: MySQL 8+ with soft-deletes (`deleted_at`, `deleted_by`, `is_deleted`)
- **AI/ML Service**: Python 3.11 + FastAPI, YOLO/OpenCV feature extraction, NumPy, Scikit-learn, Indian rural composite deficit index algorithms
- **File Storage**: MinIO / S3 with SHA-256 integrity verification, versioning, and presigned expiring URLs
- **Cache**: Redis 7
- **Reverse Proxy & Deployment**: Nginx + Docker Compose (7 container orchestration)

---

## Role Hierarchy & Permission Matrix

$$\text{SUPER ADMIN} \longrightarrow \text{ADMIN} \longrightarrow \{\text{PROJECT MANAGER}, \text{ANALYST}\} \longrightarrow \{\text{FIELD OFFICER}, \text{VIEWER}\}$$

| Permission Module | Super Admin | Admin | PM | Analyst | Field Officer | Viewer |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| **User Management** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Role Management** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Project Create/Edit** | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Project View** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Requirement Create** | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| **Field Data Entry** | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| **AI Analysis & CV** | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| **GIS Analysis** | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Reports & Analytics** | ✅ | ✅ | ✅ | ✅ | Limited | View |
| **File Upload** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| **File Delete** | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Audit Logs** | ✅ | ✅ | Limited | ❌ | ❌ | ❌ |
| **System Settings** | ✅ | Limited | ❌ | ❌ | ❌ | ❌ |

---

## Default Demo Credentials

| Role | Email | Password | Scope |
|:---|:---|:---|:---|
| **Super Admin** | `superadmin@gramdrishti.gov.in` | `Password@123` | Apex National MoRD |
| **Admin** | `admin@gramdrishti.gov.in` | `Password@123` | State Planning Board (Maharashtra) |
| **Project Manager** | `pm@gramdrishti.gov.in` | `Password@123` | PMGSY Directorate (Odisha) |
| **Analyst** | `analyst@gramdrishti.gov.in` | `Password@123` | GIS & Remote Sensing Cell (Rajasthan) |
| **Field Officer** | `field@gramdrishti.gov.in` | `Password@123` | Panchayat Ground Unit (Gadchiroli) |
| **Viewer** | `viewer@gramdrishti.gov.in` | `Password@123` | Public & Citizen Oversight |

---

## Quick Start Guide

### Option 1: Docker Compose (Full Stack 7 Containers)

```bash
docker compose up --build -d
```

Access services:
- **Web Application Console**: [http://localhost](http://localhost) (or [http://localhost:5173](http://localhost:5173))
- **Spring Boot OpenAPI Swagger**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **FastAPI ML Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **MinIO Storage Console**: [http://localhost:9001](http://localhost:9001) (`minioadmin` / `minioadmin`)

### Option 2: Running Locally for Development

1. **AI Microservice**:
   ```bash
   cd ai-service
   pip install -r requirements.txt
   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
   ```

2. **Backend**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---

## Closed-Loop Intelligence Workflow

1. **Telemetry & Detection**: Village demographic and spatial data ingested; AI/CV detector scores multi-dimensional gaps.
2. **Deficit Index (0-100)**: Habitations categorized (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) with top suggested schemes (PMGSY, Jal Jeevan, Ayushman Bharat).
3. **Project Launch**: Administrative work order created with target outcome.
4. **Field Evidence Verification**: Geotagged site imagery and sensor metrics uploaded with SHA-256 integrity verification.
5. **AI Re-Evaluation & Rescoring**: AI compares post-intervention data with baseline and computes verified impact delta ($\Delta \text{Gap Score}$), closing the intelligence loop.
