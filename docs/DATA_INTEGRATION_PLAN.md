# GramDrishti AI — Data Integration Architecture & Implementation Plan

**Document Version:** 2.0.0  
**Phase:** Phase 2 (Data Integration) & Phase 3 (Canonical Database)  
**Status:** Approved & Active  
**Author:** DeepMind AI Engineering Team & GramDrishti Core Architecture Group  
**Target Repository:** `/Users/shailendhirah/Downloads/GramDrishti AI`

---

## 1. Executive Summary & Architectural Principles

GramDrishti AI is a national-scale intelligence platform engineered to identify, prioritize, and monitor rural infrastructure deficits across India's 660,000+ villages and 1.6+ million habitations. To power precise, multi-sector gap intelligence without compromising data integrity, the system integrates 7 heterogeneous datasets spanning:
1. **Computer Vision Ground Observations** (`Dataset3Class`): 4,462 YOLO road defect & obstacle images.
2. **Administrative & Census Baseline** (`Indian Villages.ods`): 6,634 sub-district/village records.
3. **Aerial Landcover Semantic Segmentation** (`archive`): 74 UAV/drone orthomosaic-mask pairs.
4. **District Socioeconomic & Education Time-Series** (`archive-4`): Census 2011, 800+ elementary education indicators, housing amenities, and state GDP series across 34 states/UTs.
5. **National Drinking Water Security & Chemical Contamination** (`archive-5`): 7.2 Million habitation records and 550,000+ water-quality affected records across 4 historical audit cycles (2009–2012).
6. **High-Resolution Satellite Road Corridors** (`archive-6`): 2,256 satellite images & binary road masks.
7. **Official Geospatial Gazetteer** (`soi_toponyms.csv`): 1,283,668 Survey of India (SOI) WGS84 coordinates and multilingual toponyms.

### Core Non-Negotiable Operational Rules:
- **Raw Data Immutability**: All original files in `/dataset_1` remain strictly read-only.
- **Strict Memory Management**: Neither the 943 MB SOI gazetteer nor the 6.6M habitation records are loaded entirely into RAM. Ingestion operates via chunked streams (10,000 to 50,000 records/batch).
- **Historical Fidelity**: Multi-year habitation audits (2009, 2010, 2011, 2012) are preserved as distinct time-series observations indexed by `source_year` rather than collapsed or overwritten.
- **Conservative Entity Resolution**: No uncertain village names are automatically merged. Matching uses strict phonetic, administrative, and spatial distance gates (`AUTO_MATCHED`, `REVIEW_REQUIRED`, `CONFIRMED`, `REJECTED`, `UNMATCHED`).
- **No Fabricated Data**: If a coordinate, population count, or infrastructure metric is missing, it is preserved as `NULL` / `"Data unavailable"` / `"Insufficient data"`.

---

## 2. Multi-Tier Data Flow & Layered Processing Pipeline

The GramDrishti data lifecycle follows a 9-layer pipeline transitioning from raw unstructured/semi-structured files to enterprise analytics and AI inference:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. RAW DATA LAYER                                                           │
│    /dataset_1 (Read-Only CSVs, ODS, Images, Masks, YOLO Labels)            │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Streaming Extract / Chunking
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. STAGING DATA LAYER (stg_*)                                               │
│    Preserves raw source representations with lineage & audit metadata:      │
│    (source_dataset, source_file, source_row_identifier, ingestion_batch_id) │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Type Casting, Null Normalization,
                                       │ Regex Sanitization, Range Validation
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. CLEANED DATA LAYER                                                       │
│    Invalid records routed to `rejected_records` (bounds, formats, ranges)   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Unicode normalization, case folding,
                                       │ suffix stripping (e.g. "GP", "VILL")
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. STANDARDIZED DATA LAYER                                                  │
│    Consistent administrative codes, standard LGD/Census names & coordinates │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Phonetic (Double Metaphone) +
                                       │ Jaro-Winkler + Spatial Haversine gate│
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. ENTITY RESOLUTION LAYER (`entity_matches`)                               │
│    Links Habitations ↔ Villages ↔ SOI Toponyms with Confidence Scoring       │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ High-confidence promotion (≥ 0.88)   │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 6. CANONICAL DATA LAYER (Normalized MySQL 8 Schema)                         │
│    states → districts → blocks → villages → habitations → water_quality     │
│    geographic_features (SOI) & historical observations (2009–2012)          │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Aggregation, Index Computation,
                                       │ Deficit Weighting Formulae
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 7. ANALYTICS & GAP SCORING ENGINE                                           │
│    village_infrastructure_indicators & versioned village_gap_scores         │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Inference Pipelines (FastAPI + CV)   │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 8. AI / COMPUTER VISION LAYER                                               │
│    YOLOv8 Road Defect Detections & U-Net Satellite/Drone Segmentation       │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ REST / OpenAPI / Leaflet GeoJSON     │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 9. APPLICATION & PRESENTATION LAYER                                         │
│    Spring Boot 2.7 REST API, Leaflet GIS, React Frontend Dashboard          │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Dataset Ingestion Matrix & Detailed Ingestion Specs

| Dataset | Format / Size | Records / Elements | Ingestion Method | Target Staging Table | Target Canonical Entity |
|:---|:---|:---|:---|:---|:---|
| **1. Dataset3Class** | JPG + YOLO TXT (1.2 GB) | 4,462 image-label pairs | Manifest parser & validator | `stg_ai_road_annotations` | `ai_models`, `ai_detections` |
| **2. Indian Villages.ods** | ODS (1.6 MB) | 6,634 rows | Python `odf` / `pandas` chunker | `stg_villages_census` | `districts`, `blocks`, `villages` |
| **3. archive** | JPG + PNG (15 MB) | 74 image-mask pairs | Image metadata & mask profiler | `stg_ai_aerial_segmentation` | `ai_segmentation_results` |
| **4. archive-4** | 37 CSVs (4.1 MB) | 640 districts + 34 GDP series | Pandas vectorized parser | `stg_district_census`, `stg_education`, `stg_gdp` | `districts`, `district_indicators` |
| **5. archive-5** | 8 CSVs (1.05 GB) | 7,199,999 records (2009–2012) | Streaming chunked generator (50k/chunk) | `stg_habitations`, `stg_water_quality` | `blocks`, `villages`, `habitations`, `water_quality_records` |
| **6. archive-6** | PNG (180 MB) | 2,256 tiles & masks | Segmentation tile loader | `stg_ai_satellite_roads` | `ai_segmentation_results` |
| **7. soi_toponyms.csv** | CSV (943.4 MB) | 1,283,668 rows | Streaming chunked reader (25k/chunk) | `stg_soi_toponyms` | `geographic_features` |

---

## 4. Staging Table Specifications & Lineage Retention

All staging tables record complete data lineage to ensure auditability and re-ingestion idempotence:
- `source_dataset`: Identifier of the source collection (e.g., `archive-5`, `soi_toponyms`).
- `source_file`: Specific filename (e.g., `Basic_habitation_info_2011_04_01.csv`).
- `source_row_identifier`: Line/record number within source file.
- `ingestion_batch_id`: UUID linking the operation to `dataset_ingestion_batches`.
- `ingested_at`: UTC timestamp of ingestion.

### Staging Schemas:
1. `stg_soi_toponyms`: Preserves 50 raw columns including Roman, Hindi, Urdu, Bengali, Telugu, Marathi, Gujarati, Kannada, Malayalam, Tamil names, Sheet Number, Latitude, Longitude, and Feature Class.
2. `stg_habitations`: Preserves State, District, Block, Panchayat, Village, Habitation, SC/ST/Gen population, and coverage status code.
3. `stg_water_quality`: Preserves Habitation Name, Quality Parameter (Fluoride, Arsenic, Iron, Salinity, Nitrate), and date.
4. `stg_district_census`: Preserves Census 2011 118-column demographic, literacy, and household asset metrics.
5. `stg_education`: Preserves Elementary 2015-16 infrastructure, PTR, electrification, and sanitation indicators.
6. `stg_gdp`: Preserves annual state Gross State Domestic Product time-series.

---

## 5. Cleaning, Validation & Rejection Framework

Every record passing through the ingestion engine is validated against strict domain assertions before transformation:

```
                              Incoming Record
                                     │
                                     ▼
                      ┌──────────────────────────────┐
                      │    Coordinate Validation     │──► Invalid ──► [rejected_records]
                      │ Lat: [-90,+90], Lng: [-180,+180] │ (REASON: INVALID_COORDINATES)
                      └──────────────┬───────────────┘
                                     │ Valid
                                     ▼
                      ┌──────────────────────────────┐
                      │    Population Validation     │──► Negative ──► [rejected_records]
                      │  SC, ST, Gen, Total ≥ 0       │ (REASON: NEGATIVE_POPULATION)
                      └──────────────┬───────────────┘
                                     │ Valid
                                     ▼
                      ┌──────────────────────────────┐
                      │     String Sanitization      │──► Empty/Corrupt ──► [rejected_records]
                      │  Trim, Unicode Normalize NFKC │ (REASON: MISSING_REQUIRED_NAME)
                      └──────────────┬───────────────┘
                                     │ Valid
                                     ▼
                      ┌──────────────────────────────┐
                      │      Temporal Integrity      │──► Invalid ──► [rejected_records]
                      │   Year: [1990, 2030]         │ (REASON: INVALID_SOURCE_YEAR)
                      └──────────────┬───────────────┘
                                     │ Valid
                                     ▼
                              [Cleaned Layer]
```

### Table: `rejected_records`
- `id` (BIGINT PK AUTO_INCREMENT)
- `source_dataset` (VARCHAR(100))
- `source_file` (VARCHAR(255))
- `source_row` (BIGINT)
- `reason` (VARCHAR(255))
- `raw_reference` (TEXT / JSON)
- `created_at` (DATETIME)

---

## 6. Entity Resolution & Multi-Criteria Matching Algorithm

Cross-linking habitations, census administrative hierarchies, and Survey of India geospatial features requires multi-tier probabilistic matching:

### 1. Name Normalization Rules
- Convert to lowercase and trim whitespace.
- Normalize Unicode (NFKC) and strip diacritics.
- Strip common administrative suffixes: ` gp`, ` gram panchayat`, ` revenue village`, ` vlg`, ` tehsil`, ` block`.
- Remove non-alphanumeric punctuation except standard hyphens.

### 2. Multi-Tier Resolution Cascade
1. **Tier 1: Exact Hierarchy + Exact Name Match**
   - Condition: `State == State AND District == District AND Block == Block AND Village == Village`
   - Confidence: `1.00` $\rightarrow$ Status: `AUTO_MATCHED`
2. **Tier 2: Exact State/District + Phonetic (Double Metaphone) Match**
   - Condition: `Metaphone(Village_A) == Metaphone(Village_B) AND JaroWinkler(Village_A, Village_B) ≥ 0.90`
   - Confidence: `0.92` $\rightarrow$ Status: `AUTO_MATCHED`
3. **Tier 3: Fuzzy Name (0.75 ≤ Score < 0.90) with Spatial Proximity (< 10 km)**
   - Condition: `Haversine(Coord_A, Coord_B) < 10.0 km AND JaroWinkler ≥ 0.75`
   - Confidence: `0.80` $\rightarrow$ Status: `REVIEW_REQUIRED`
4. **Tier 4: Sub-threshold / Ambiguous Match (< 0.75 or Multiple Candidates)**
   - Status: `UNMATCHED` / `REVIEW_REQUIRED` (Never merged automatically)

### Table: `entity_matches`
- `id` (BIGINT PK AUTO_INCREMENT)
- `source_dataset` (VARCHAR(100))
- `source_record_id` (VARCHAR(255))
- `target_entity_type` (VARCHAR(50): `VILLAGE`, `DISTRICT`, `GEOGRAPHIC_FEATURE`)
- `target_entity_id` (BIGINT)
- `matching_method` (VARCHAR(100): `EXACT_HIERARCHY`, `PHONETIC_JARO_WINKLER`, `SPATIAL_PROXIMITY_BUFFER`)
- `confidence_score` (DECIMAL(5,4))
- `status` (ENUM: `AUTO_MATCHED`, `REVIEW_REQUIRED`, `CONFIRMED`, `REJECTED`, `UNMATCHED`)
- `created_at` (DATETIME)

---

## 7. Historical Habitation Data Handling

The habitation dataset (`archive-5`) contains 4 consecutive annual snapshots (2009, 2010, 2011, 2012). To preserve longitudinal trends in drinking water security:
1. `habitations` table stores master habitation identity (`village_id`, `habitation_name`).
2. Each historical observation is stored in `habitation_observations` (or versioned `habitations` rows) carrying `source_year` (2009, 2010, 2011, 2012).
3. Water quality incidents are linked to `habitation_id` with specific `parameter` (`Fluoride`, `Arsenic`, `Iron`, `Salinity`, `Nitrate`) and `source_year`.
4. This empowers the UI to render longitudinal graphs showing how water security transitioned from *Not Covered* $\rightarrow$ *Partially Covered* $\rightarrow$ *Fully Covered* over time.

---

## 8. GIS Spatial Query Strategy & Optimization

Serving 1.28M Survey of India points without client-side lag requires server-side geospatial indexing:
- **Database Indexing**: Spatial B-Tree / R-Tree composite indices on `(latitude, longitude)`, `(state_name, district_name)`, and `feature_type`.
- **Bounding Box Query API**:
  - `GET /api/gis/features?min_lat=...&max_lat=...&min_lng=...&max_lng=...&limit=500`
- **Hierarchical Clustering**: Server aggregates point clusters at lower zoom levels and delivers individual points only at zoom $\ge 12$.

---

## 9. Next Steps & Phase 3 Execution

1. Deploy Flyway migration scripts (`V1` to `V7`) to MySQL `gramdrishti_db`.
2. Execute Python streaming batch ingestion pipeline on all 7 datasets.
3. Validate imported record counts, log rejected records, and run entity matching.
4. Generate `/docs/DATA_QUALITY_REPORT.md` and verify database integrity metrics.
