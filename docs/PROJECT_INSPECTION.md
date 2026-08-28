# GramDrishti AI — Project Architecture & Inspection Report

**Document Version:** 1.0.0  
**Inspection Date:** 2026-08-28  
**Scope:** Full-stack Architecture, Backend, Frontend, AI/ML Microservice, Database, and Dataset Integration

---

## 1. Executive Summary

**GramDrishti AI** is an AI-powered rural infrastructure gap intelligence and project tracking platform for India. The system identifies, analyzes, scores, prioritizes, and monitors infrastructure deficits across roads, healthcare, education, water/sanitation, digital telecom, power, and market access, pairing satellite/drone computer vision with village-level socioeconomic datasets and closed-loop impact verification.

---

## 2. Existing System Architecture

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
                          │ (Port 8000)           │   │ (Docker Port 3306)    │   │ (Port 9000/9001)      │
                          └───────────────────────┘   └───────────────────────┘   └───────────────────────┘
```

---

## 3. Technology Stack Breakdown

### 3.1 Frontend (`frontend/`)
- **Framework**: React 18, Vite 5.4, React Router v6
- **State Management & Data Fetching**: Zustand (`useAuthStore`), TanStack Query v5
- **GIS & Mapping**: Leaflet 1.9, React-Leaflet
- **Styling & UI**: Tailwind CSS 3.4, Lucide Icons, Glassmorphic Design Tokens
- **Visualizations**: Recharts (Radar charts, Bar charts, Area charts)
- **Forms**: React Hook Form
- **HTTP Client**: Axios with JWT Interceptors and standard envelope unwrapping

### 3.2 Backend (`backend/`)
- **Framework**: Java Spring Boot 2.7.18 / Java 11 (Amazon Corretto)
- **Security**: Spring Security (modern `SecurityFilterChain` + `DaoAuthenticationProvider`), JJWT (0.11.5), Rate Limiting Interceptor, Account Lockout (5 attempts)
- **Persistence**: Spring Data JPA / Hibernate 5.6, Soft Deletes (`is_deleted = false`), native restore queries
- **Database**: MySQL 8.0 running in Docker (`gramdrishti-mysql` on port 3306) + H2 in-memory profile
- **Object Storage**: MinIO / S3 client with SHA-256 integrity checksums and expiring presigned URLs
- **Documentation**: Springdoc OpenAPI / Swagger UI 1.7.0 (`/swagger-ui/index.html`)

### 3.3 AI Microservice (`ai-service/`)
- **Framework**: Python 3.13 / 3.11 + FastAPI + Uvicorn
- **Capabilities**:
  1. Indian Rural Composite Deficit Index calculation ($0-100$) across 7 dimensions (PMGSY, IPHS, RTE, Jal Jeevan standards)
  2. Computer Vision / YOLO-style obstacle and road feature extraction from aerial/satellite imagery
  3. Closed-Loop Impact Re-Evaluation comparing post-intervention telemetry against baseline data

### 3.4 Orchestration & Infrastructure
- **Docker Compose**: 7-service orchestration (`mysql`, `redis`, `minio`, `ai-service`, `backend`, `frontend`, `nginx`)
- **Reverse Proxy**: Nginx 1.25 Alpine

---

## 4. Existing Routes & Navigation Matrix

| Route | Page Component | Permission Required | Description |
|:---|:---|:---|:---|
| `/login` | `LoginPage.jsx` | Public | SSO/JWT login with 1-click role switcher |
| `/` | `DashboardPage.jsx` | Authenticated | Executive KPIs, deficit breakdown, quick map preview |
| `/gis-explorer` | `GISExplorerPage.jsx` | `GIS_VIEW` | Fullscreen Leaflet map, layer filters, village inspector drawer |
| `/ai-intelligence` | `AIGapIntelligencePage.jsx` | `AI_ANALYSIS` | Multi-parameter simulation sliders + CV scan + radar chart |
| `/impact-reevaluation` | `ImpactReEvaluationPage.jsx` | `PROJECT_VIEW` | Closed-loop impact measurement & before/after delta certification |
| `/projects` | `ProjectsPage.jsx` | `PROJECT_VIEW` | Kanban & table pipeline, project creation modal, officer assignment |
| `/requirements` | `RequirementsPage.jsx` | `REQUIREMENT_VIEW`| Deficit triage board, government scheme matching, work order approvals |
| `/field-evidence` | `FieldEvidencePage.jsx` | `FIELD_DATA_ENTRY`| GPS geotagged photo/evidence upload portal with SHA-256 checksums |
| `/assets` | `AssetManagerPage.jsx` | `FILE_VIEW` | Digital Asset Management (DAM) with folder tree and version history |
| `/audit-logs` | `AuditLogsPage.jsx` | `AUDIT_LOGS` | Immutable security audit trail with IP, role, and diff tracking |
| `/users` | `UserManagementPage.jsx` | `USER_MANAGEMENT` | User directory + 13-permission RBAC security matrix reference |

---

## 5. Existing Reusable Components

1. **`src/components/gis/GISMapView.jsx`**:
   - Leaflet interactive map with custom pulsing markers for Critical/High/Medium/Low deficit habitations.
   - Infrastructure point markers with type-specific colored badges.
   - Dynamic GeoJSON deficit zone polygons and project impact boundary lines.
2. **`src/components/layout/Navbar.jsx`**:
   - Navigation header with live 6-role switcher modal for instant evaluation.
3. **`src/components/layout/Sidebar.jsx`**:
   - Permission-filtered sidebar navigation with active indicator highlighting.
4. **`src/components/common/RoleGuard.jsx`**:
   - Declarative RBAC wrapper checking client-side permissions against JWT authorities.

---

## 6. Identified Problems & Resolutions

| Issue | Area | Severity | Status | Resolution Applied |
|:---|:---|:---:|:---:|:---|
| Deprecated `WebSecurityConfigurerAdapter` | Backend Security | Warning | **RESOLVED** | Migrated to modern Spring Security `SecurityFilterChain` bean. |
| Unused Imports & Fields | `AIServiceClient`, `ProjectService`, `UserService` | Warning | **RESOLVED** | Removed all unused imports and redundant repository fields. |
| Maven Compiler Annotation Processor | `pom.xml` | Build | **RESOLVED** | Configured `maven-compiler-plugin` 3.11.0 with explicit Lombok processor. |

---

## 7. Recommended Next Steps for Dataset Integration

1. **Batch Ingestion Pipeline**: Ingest official Survey of India Toponyms (`soi_toponyms.csv`) and Census 2011 (`india-districts-census-2011.csv`) into canonical normalized database tables (`states`, `districts`, `blocks`, `villages`).
2. **Habitation Quality Overlay**: Ingest Ministry of Drinking Water & Sanitation Habitation quality indices (`archive-5`) to populate real water quality parameters (Fluoride, Arsenic, Salinity, Iron, Nitrate).
3. **Computer Vision Fine-Tuning**: Feed `Dataset3Class` (4,462 YOLO annotated images) into FastAPI PyTorch/YOLO pipeline for live road defect detection.
4. **Semantic Landcover Segmentation**: Connect `archive` (aerial landcover masks) and `archive-6` (satellite road masks) to aerial analysis endpoint.
