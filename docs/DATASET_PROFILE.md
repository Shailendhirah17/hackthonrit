# GramDrishti AI — Comprehensive Dataset Profile Report

**Document Version:** 1.0.0  
**Profile Date:** 2026-08-28  
**Source Directory:** `/dataset_1/`

---

## 1. Executive Summary & Master Dataset Comparison Table

| # | Dataset / Subfolder | Primary Purpose | Record / Image Count | Geographic Granularity | AI Suitability | GIS Suitability | Analytics Suitability |
|:---:|:---|:---|:---:|:---:|:---:|:---:|:---:|
| **1** | `Dataset3Class` | Road defects, potholes, speed bumps, unpaved corridors | 4,462 JPG images + 4,462 YOLO TXT annotations | Point Image Level (Road Corridors) | **HIGH** (YOLOv8 Object Detection) | Medium (Geotagged site layer) | High (Defect density per km) |
| **2** | `Indian Villages.ods` | Census Demographics & Village Inhabitation Metrics | 6,634 Village/Sub-district Records | National / State / District / Sub-district | Low | Medium (Census code join) | **HIGH** (Demographic baseline) |
| **3** | `archive` | Aerial Land Cover & Infrastructure Semantic Segmentation | 74 JPG images + 148 PNG masks (5 classes) | Aerial / Drone Orthomosaic | **HIGH** (U-Net / DeepLabV3 Segmentation) | **HIGH** (Landcover polygon masks) | High (Road & water coverage %) |
| **4** | `archive-4` | District Census 2011, Elementary Education & State GDPs | 37 CSV Files (640 Districts, 800 Education Attributes, State GDP series) | District / State | Medium | **HIGH** (District boundary heatmaps) | **HIGH** (Socioeconomic & Education indices) |
| **5** | `archive-5` | Ministry of Drinking Water & Sanitation Habitation Water Access & Quality | 6.6+ Million Habitation Records (2009–2012) + 550k Quality Records | State / District / Block / Panchayat / Village / Habitation | Low | **HIGH** (Habitation point mapping) | **CRITICAL** (Drinking water deficit & contamination) |
| **6** | `archive-6` | High-Resolution Satellite Road Extraction & Segmentation | 2,256 PNG images (1,003 Generated Pairs + 100 Groundtruth Pairs) | Satellite Grid Tiles (608x608) | **HIGH** (Road Network Extraction) | **HIGH** (Satellite raster overlays) | High (All-weather connectivity index) |
| **7** | `soi_toponyms.csv` | Survey of India (SOI) Official Geographic Features & Toponyms | 1,283,668 Geographic Features (575k+ Villages, Rivers, Roads, Hills) | Precise Decimal Latitude / Longitude (WGS84) | Medium | **CRITICAL** (Official National Spatial Base) | **CRITICAL** (Geocoding & spatial buffer matching) |

---

## 2. In-Depth Dataset Profiles

### 2.1 Dataset 1: `Dataset3Class` (YOLO Road Obstacles & Surface Condition)
1. **Dataset Purpose**: Train and validate object detection models for rural road conditions, unpaved surfaces, severe potholes, and unmarked speed breakers.
2. **Files**: 8,924 files in root (`4,462` JPG images and `4,462` YOLO `.txt` label files).
3. **Record Count**: 4,462 image-annotation pairs.
4. **Dimensions & Format**: High resolution RGB JPG images (e.g. `1920x1088` and `1280x720`).
5. **Annotation Format**: Standard YOLO normalized bounding box format (`<class_id> <x_center> <y_center> <width> <height>`).
6. **Class Distribution**:
   - `Class 0`: Potholes (`PotHoles_*.jpg`)
   - `Class 1`: Speed Bumps / Unmarked Bumps (`Speed_*.jpg`, `UnMarkedBump_*.jpg`, `SB_*.jpg`)
   - `Class 2`: Unpaved / Ungraded Kutcha Mud Roads (`UnPavedRoad__*.jpg`, `UngradedRoad_*.jpg`)
7. **Missing Values**: 0 missing labels; all JPGs have corresponding TXT files.
8. **Duplicate Records**: Minimal; continuous frame sequences from vehicle dashcam / drone survey.
9. **Data Quality**: High; crisp bounding boxes and diverse illumination conditions (monsoon mud, dry gravel, asphalt).
10. **Potential Primary Key**: Image filename (e.g. `UnPavedRoad__1243.jpg`).
11. **Potential Foreign Keys**: `project_id`, `asset_id` in DAM.
12. **AI Suitability**: **100% Ready for YOLOv8 / YOLOv11** fine-tuning in `ai-service`.
13. **Recommended Usage**: Road quality score calculation, pothole density per kilometer, and PMGSY road classification.

---

### 2.2 Dataset 2: `Indian Villages.ods` (National Village Demographics)
1. **Dataset Purpose**: Baseline demographic and administrative hierarchy for inhabited Indian villages.
2. **Files**: `Indian Villages.ods` (1.6 MB).
3. **Record Count**: 6,634 rows.
4. **Column Count**: 9 columns.
5. **Important Columns**:
   - `State Code` (float64)
   - `District Code` (float64)
   - `Sub District Code` (float64)
   - `Level` (string: `INDIA`, `STATE`, `DISTRICT`, `SUB-DISTRICT`)
   - `Inhabited Village` (float64: Count of inhabited villages)
   - `Total Population` (float64)
   - `Male` (float64)
   - `Female` (float64)
   - `Name` (string: Administrative region name)
6. **Geographic Information**: Administrative hierarchy from Country down to Sub-district (Tehsil/Taluk).
7. **Missing Values**: Row 0 header artifact (1 null per column); rows 1–6634 are 100% complete.
8. **Duplicate Records**: 0 duplicates across hierarchical codes.
9. **Potential Primary Key**: Composite (`State Code`, `District Code`, `Sub District Code`, `Level`).
10. **Recommended Usage**: Normalizing national population totals, sex ratios, and inhabited village density per sub-district.

---

### 2.3 Dataset 3: `archive` (Aerial / Drone Semantic Segmentation)
1. **Dataset Purpose**: High-resolution landcover and infrastructure segmentation from aerial/UAV imagery.
2. **Files**: 223 files (`class_dict_seg.csv`, `train_image/` [60], `train_mask/` [60], `test_image/` [14], `test_mask/` [14], `pixel_based_mask/`).
3. **Record Count**: 74 image-mask pairs.
4. **Dimensions & Format**: `420x420` RGB JPG images and corresponding 3-channel RGB PNG ground truth masks.
5. **Class Dictionary (`class_dict_seg.csv`)**:
   - `urban`: RGB `(0, 255, 255)` (Cyan)
   - `water`: RGB `(0, 0, 255)` (Blue)
   - `forest`: RGB `(0, 255, 0)` (Green)
   - `argiculture`: RGB `(255, 255, 0)` (Yellow)
   - `road`: RGB `(255, 0, 255)` (Magenta)
6. **AI Suitability**: **Directly usable for PyTorch / Segmentation Models PyTorch (SMP)** (UNet, FPN, DeepLabV3+ with ResNet50 backbone).
7. **Recommended Usage**: Calculating percentage road access, water body proximity, and built-up area footprint from drone survey uploads in `FieldEvidencePage`.

---

### 2.4 Dataset 4: `archive-4` (District Census 2011, Education & State GDPs)
1. **Dataset Purpose**: Comprehensive district-level socioeconomic vulnerability, educational access, and economic performance data across India.
2. **Files**: 37 CSV files including:
   - `india-districts-census-2011.csv` (640 rows, 118 columns)
   - `elementary_2015_16.csv` (680 rows, 800 columns)
   - `india_census_housing-hlpca-full.csv` (156 columns)
   - 34 state GDP annual time-series files (`gdp_Maharashtra1.csv`, `gdp_Odisha1.csv`, `gdp_Rajasthan1.csv`, etc.)
3. **Key Demographic & Infrastructure Metrics**:
   - `Population`, `Male`, `Female`, `Literate`, `SC`, `ST`
   - `Households_with_Bicycle`, `Households_with_Car_Jeep_Van`, `Households_with_Radio_Transistor`, `Households_with_Television`, `Households_with_Telephone_Mobile_Phone`
   - `Electricity_Lighting`, `Tapwater_from_treated_source`, `LPG_PNG_Cooking`
   - `elementary_2015_16.csv`: School count, pupil-teacher ratio (PTR), drinking water availability in schools, electricity connection in schools, computer labs.
4. **Geographic Granularity**: District level with Census 2011 `District code` and `State Name`.
5. **Potential Primary Key**: `District code` (Census 2011 standard).
6. **Recommended Usage**: Establishing baseline district vulnerability indices and weighting factors for rural deficit scoring.

---

### 2.5 Dataset 5: `archive-5` (Ministry of Drinking Water & Sanitation Habitations)
1. **Dataset Purpose**: Official Government of India annual habitation-level water security, coverage status, and chemical contamination records.
2. **Files**: 8 massive CSV datasets (2009–2012) + `habitations.7z`:
   - `Basic_habitation_info_2009_04_01.csv` (1,661,091 records, 231.7 MB)
   - `Basic_habitation_info_2010_04_01.csv` (1,658,356 records, 231.5 MB)
   - `Basic_habitation_info_2011_04_01.csv` (1,664,219 records, 232.2 MB)
   - `Basic_habitation_info_2012_04_01.csv` (1,666,108 records, 232.2 MB)
   - `Water_quality_affected_habitation_2009_04_01.csv` (180,001 records, 16.2 MB)
   - `Water_quality_affected_habitation_2010_04_01.csv` (144,583 records, 13.0 MB)
   - `Water_quality_affected_habitation_2011_04_01.csv` (121,501 records, 10.9 MB)
   - `Water_quality_affected_habitation_2012_04_01.csv` (104,160 records, 9.4 MB)
3. **Total Record Count**: **7,199,999 records** (over 1 GB of raw tabular government data).
4. **Important Columns**:
   - `State Name`, `District Name`, `Block Name`, `Panchayat Name`, `Village Name`, `Habitation Name`
   - `SC Current Population`, `ST Current Population`, `GENERAL Current Population`
   - `SC Covered Population`, `ST Covered Population`, `GENERAL Covered Population`
   - `Status` (`Fully Covered [FC]`, `Partially Covered [PC]`, `Not Covered [NC]`)
   - `Quality Parameter` (`Fluoride`, `Arsenic`, `Iron`, `Salinity`, `Nitrate`)
5. **Potential Primary Key**: Composite (`State Name`, `District Name`, `Block Name`, `Panchayat Name`, `Village Name`, `Habitation Name`, `Year`).
6. **Analytics & Impact**: **Crucial for Jal Jeevan Mission gap scoring**; reveals exact habitations with unpotable water and chemical contamination.

---

### 2.6 Dataset 6: `archive-6` (Satellite Road Network Segmentation)
1. **Dataset Purpose**: Extract and segment rural road corridors from satellite orthomosaics.
2. **Files**: 2,256 PNG images (`images_generated/` [1,003], `groundtruth_generated/` [1,003], `groundtruth/` [100]).
3. **Dimensions & Format**: `608x608` 3-channel RGB PNG images and 1-channel binary road masks.
4. **AI Suitability**: **High**; training convolutional segmentation models (ResNet-UNet) for automated all-weather road vectorization.
5. **Recommended Usage**: Estimating distance from village center to nearest visible road corridor directly from satellite tiles.

---

### 2.7 Dataset 7: `soi_toponyms.csv` (Survey of India Official Toponymy Database)
1. **Dataset Purpose**: Master geospatial gazetteer of India containing exact coordinates, names in 18 Indian languages, and feature classifications from the national mapping agency.
2. **Files**: `soi_toponyms.csv` (943.4 MB).
3. **Record Count**: **1,283,668 rows**.
4. **Column Count**: 50 columns.
5. **Important Columns**:
   - `feature` / `feature_1` (`VILLAGE`, `HAMLET`, `RIVER`, `CANAL`, `ROAD NAME`, `RAILWAY STATION`, `WATER_TANK_SURVEYED`, `FORT`)
   - `text` / `roman` (Official Romanized English name)
   - `hindi`, `bengali`, `gujarati`, `kannada`, `malayalam`, `marathi`, `punjabi`, `tamil`, `telugu`, `oriya`, `assamese`, `urdu` (Multilingual names)
   - `longitude` (Decimal degrees WGS84, e.g. `81.1092485`)
   - `latitude` (Decimal degrees WGS84, e.g. `27.6195035`)
   - `responsibi` (Survey of India regional directorate: `OGDC`, `RGDC`, `UK&WEST UP`, `EUPGDC`, `HPGDC`, `J&K GDC`)
   - `osm_sheet_`, `everest_sh` (Topographical sheet identifier)
6. **Data Quality**: **Exceptional geospatial ground truth**; 100% valid latitude/longitude for all feature points across India.
7. **Potential Primary Key**: `unique_id` / `objectid_1` / `fid`.
8. **GIS & Analytics**: **The definitive spatial anchor** for mapping habitations, measuring distances to physical infrastructure, and powering the Leaflet GIS engine.

---

## 3. Data Relationships & Entity Matching Strategy

```
                                  ┌──────────────────────────┐
                                  │   States & Districts     │ (from Census 2011)
                                  └────────────┬─────────────┘
                                               │ (District Code / State Name)
                                               ▼
                                  ┌──────────────────────────┐
                                  │         Blocks           │ (from Basic Habitation Info)
                                  └────────────┬─────────────┘
                                               │ (Block Name / Panchayat)
                                               ▼
                                  ┌──────────────────────────┐
                                  │   Villages & Toponyms    │ (from SOI Toponyms + Census ODS)
                                  │ (Lat, Lng, Population)   │
                                  └────────────┬─────────────┘
                                               │ (Village ID / Coords)
                        ┌──────────────────────┴──────────────────────┐
                        ▼                                             ▼
          ┌──────────────────────────┐                  ┌──────────────────────────┐
          │  Water Quality & Access  │                  │ Road Surface & CV Deficit│
          │ (archive-5 Habitations)  │                  │ (Dataset3Class + DeepGlob│
          └──────────────────────────┘                  └──────────────────────────┘
```

1. **Spatial Join (High Confidence)**: Toponyms from `soi_toponyms.csv` with exact `(latitude, longitude)` provide the spatial geometry for villages in `Indian Villages.ods`.
2. **Administrative Hierarchy Matching**: `State Name` $\rightarrow$ `District Name` $\rightarrow$ `Block Name` $\rightarrow$ `Village Name` correlates `archive-5` (Drinking water status) with `archive-4` (Census 2011 socioeconomic indicators).
3. **Computer Vision Augmentation**: `Dataset3Class` (YOLO road obstacles) and `archive-6` (satellite road extraction) feed directly into the AI Microservice for live feature inference.
