# GramDrishti AI — AI & Computer Vision Dataset Engineering Plan

**Document Version:** 2.0.0  
**Phase:** Computer Vision Pipeline Planning (Pre-Training Phase)  
**Status:** Approved  
**Author:** AI/ML Engineering Group, GramDrishti AI  
**Location:** `/docs/AI_DATASET_PLAN.md`

---

## 1. Scope & Objective

This document formalizes the dataset engineering, annotation validation, train/val/test partitioning, model architecture selection, and evaluation criteria for the three computer vision datasets integrated in GramDrishti AI:
1. **Dataset3Class**: Vehicle dashcam/drone imagery for road surface defect and obstacle detection (4,462 image/YOLO annotation pairs).
2. **archive (Aerial Landcover)**: Aerial UAV orthomosaics for 5-class semantic infrastructure segmentation (74 image/mask pairs).
3. **archive-6 (Satellite Road Extraction)**: High-resolution satellite tiles for binary road vectorization and connectivity mapping (2,256 image/mask pairs).

---

## 2. Dataset 1: `Dataset3Class` (Road Defects & Obstacle Detection)

### 2.1 Dataset Structure & Class Schema
- **Total Samples**: 4,462 image-annotation pairs (`.jpg` + `.txt`).
- **Image Resolution**: $1920 \times 1088$ and $1280 \times 720$ RGB.
- **Classes**:
  - `Class 0`: **Potholes** (`PotHoles_*.jpg`) — Structural surface craters and depressions.
  - `Class 1`: **Speed Bumps / Unmarked Bumps** (`Speed_*.jpg`, `UnMarkedBump_*.jpg`, `SB_*.jpg`) — Speed reducers and hazardous unmarked humps.
  - `Class 2`: **Unpaved Kutcha Roads** (`UnPavedRoad__*.jpg`, `UngradedRoad_*.jpg`) — Non-bituminous, dirt, gravel, and unpaved corridors.

### 2.2 Dataset Splitting Strategy
Stratified splitting grouped by source video sequence to avoid temporal leakage between adjacent frames:
- **Train Set (70%)**: 3,124 images
- **Validation Set (15%)**: 669 images
- **Test Set (15%)**: 669 images

### 2.3 Annotation Validation & Quality Assurance
- **Bounding Box Bounds Check**: Ensure all coordinates satisfy $0.0 \le x_{center}, y_{center}, w, h \le 1.0$.
- **Zero-Area Box Filtering**: Discard any label with $w \le 0.001$ or $h \le 0.001$.
- **Class ID Validation**: Enforce class ID $\in \{0, 1, 2\}$.

### 2.4 Data Augmentation Strategy
- Horizontal Flip ($p=0.5$).
- HSV Color Jitter (Hue $\pm 0.015$, Saturation $\pm 0.7$, Value $\pm 0.4$) to simulate varying lighting and monsoon conditions.
- Mosaic Augmentation ($p=0.8$) during early training epochs.
- Random Scale / Crop ($0.8 \times$ to $1.2 \times$).

### 2.5 Model Selection & Evaluation Metrics
- **Recommended Architecture**: **YOLOv8m / YOLOv11m** (Ultralytics PyTorch) exported to ONNX for low-latency GPU/CPU inference in `ai-service`.
- **Target Metrics**:
  - Potholes: Precision $\ge 0.82$, Recall $\ge 0.78$, mAP@50 $\ge 0.80$.
  - Speed Bumps: Precision $\ge 0.85$, Recall $\ge 0.80$, mAP@50 $\ge 0.83$.
  - Unpaved Roads: Precision $\ge 0.88$, Recall $\ge 0.85$, mAP@50 $\ge 0.87$.
  - Overall mAP@0.5:0.95 $\ge 0.58$.

---

## 3. Dataset 2: `archive` (Aerial Landcover Semantic Segmentation)

### 3.1 Dataset Structure & Color Encoding
- **Total Samples**: 74 image-mask pairs ($420 \times 420$ px).
- **RGB Color Map (`class_dict_seg.csv`)**:
  - `urban`: Cyan `(0, 255, 255)` — Built-up structures, buildings, settlement footprint.
  - `water`: Blue `(0, 0, 255)` — Ponds, canals, rivers, tanks.
  - `forest`: Green `(0, 255, 0)` — Dense tree canopy, vegetation.
  - `agriculture`: Yellow `(255, 255, 0)` — Cultivated farmland, crop fields.
  - `road`: Magenta `(255, 0, 255)` — Asphalt and dirt corridors.

### 3.2 Dataset Splitting Strategy
- **Train Set**: 60 images (Existing `train_image/` + `train_mask/`)
- **Validation/Test Set**: 14 images (Existing `test_image/` + `test_mask/`)

### 3.3 Model Selection & Training Protocol
- **Model Family**: **U-Net with ResNet50 / EfficientNet-B3 Backbone** (Segmentation Models PyTorch).
- **Loss Function**: Combined Focal Loss + Dice Loss ($\mathcal{L} = 0.5 \mathcal{L}_{Focal} + 0.5 \mathcal{L}_{Dice}$) to counteract class imbalance (roads occupy $< 5\%$ of pixel area).
- **Target Metrics**: Mean Intersection over Union ($\text{mIoU} \ge 0.72$), Road IoU $\ge 0.65$.

---

## 4. Dataset 3: `archive-6` (Satellite Road Network Segmentation)

### 4.1 Dataset Structure
- **Total Pairs**: 1,003 synthetic + 100 manual groundtruth pairs ($608 \times 608$ px).
- **Target Class**: Binary Road Mask (0 = Background, 255 = Road).

### 4.2 Splitting & Evaluation
- **Train Set**: 800 pairs (Synthetic + 70 Groundtruth)
- **Validation Set**: 150 pairs
- **Test Set**: 153 pairs (including 30 pristine human-verified groundtruth tiles)
- **Recommended Architecture**: **DeepLabV3+ with ResNet-101 / MiT-B2 SegFormer**.
- **Target Metrics**: Road F1-Score $\ge 0.82$, Precision $\ge 0.85$, IoU $\ge 0.70$.

---

## 5. Inference & Deployment Architecture

```
                      Raw Evidence Image / Drone / Satellite Tile
                                          │
                                          ▼
                      ┌───────────────────────────────────────┐
                      │    GramDrishti AI Microservice        │
                      │    FastAPI + ONNX Runtime Engine      │
                      └───────────────────┬───────────────────┘
                                          │
                   ┌──────────────────────┴──────────────────────┐
                   ▼                                             ▼
     ┌───────────────────────────┐                 ┌───────────────────────────┐
     │ YOLOv8 Road Obstacle Net  │                 │ U-Net Landcover Segmenter │
     │ (Potholes, Bumps, Kutcha) │                 │ (Road, Water, Built-up %) │
     └─────────────┬─────────────┘                 └─────────────┬─────────────┘
                   │                                             │
                   └──────────────────────┬──────────────────────┘
                                          │
                                          ▼
                      ┌───────────────────────────────────────┐
                      │ Insert Detections & Pixel Percentages │
                      │ into MySQL `ai_detections` / `ai_jobs`│
                      └───────────────────────────────────────┘
```
