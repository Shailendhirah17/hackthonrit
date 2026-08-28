"""
GramDrishti AI - Dataset Source Registry Initializer
"""

import os
from pathlib import Path
from config import DATASET_DIR

DATASETS = [
    {
        "name": "Dataset3Class",
        "description": "YOLO Road Defect & Obstacle Dataset (Potholes, Speed Bumps, Unpaved Kutcha Roads)",
        "source_type": "COMPUTER_VISION_LABELS",
        "file_path": str(DATASET_DIR / "Dataset3Class"),
        "record_count": 4462,
        "version": "1.0.0"
    },
    {
        "name": "Indian Villages.ods",
        "description": "Census of India Administrative Baseline & Inhabited Village Statistics",
        "source_type": "TABULAR_ODS",
        "file_path": str(DATASET_DIR / "Indian Villages.ods"),
        "record_count": 6634,
        "version": "2011.1"
    },
    {
        "name": "archive",
        "description": "UAV/Drone Aerial Orthomosaic 5-Class Semantic Landcover Segmentation",
        "source_type": "AERIAL_IMAGERY_MASKS",
        "file_path": str(DATASET_DIR / "archive"),
        "record_count": 74,
        "version": "1.0.0"
    },
    {
        "name": "archive-4",
        "description": "District Census 2011, Elementary Education (2015-16) & State GDP Series",
        "source_type": "MULTI_CSV_SOCIOECONOMIC",
        "file_path": str(DATASET_DIR / "archive-4"),
        "record_count": 640,
        "version": "2015-16"
    },
    {
        "name": "archive-5",
        "description": "Ministry of Drinking Water & Sanitation Habitation Security & Contamination (2009-2012)",
        "source_type": "NATIONAL_HABITATION_SURVEY",
        "file_path": str(DATASET_DIR / "archive-5"),
        "record_count": 7199999,
        "version": "2009-2012"
    },
    {
        "name": "archive-6",
        "description": "Satellite Imagery High-Resolution Binary Road Network Segmentation",
        "source_type": "SATELLITE_TILES_MASKS",
        "file_path": str(DATASET_DIR / "archive-6"),
        "record_count": 2256,
        "version": "1.0.0"
    },
    {
        "name": "soi_toponyms.csv",
        "description": "Survey of India (SOI) Official Geospatial Gazetteer & Multilingual Toponyms",
        "source_type": "NATIONAL_GIS_GAZETTEER",
        "file_path": str(DATASET_DIR / "soi_toponyms.csv"),
        "record_count": 1283668,
        "version": "WGS84-V2"
    }
]

def register_all_sources(cursor):
    """Registers or updates all 7 datasets in `dataset_sources`."""
    sql = """
        INSERT INTO dataset_sources 
        (dataset_name, description, source_type, file_path, record_count, status, version, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, 'ACTIVE', %s, NOW(), NOW())
        ON DUPLICATE KEY UPDATE 
            description=VALUES(description),
            file_path=VALUES(file_path),
            record_count=VALUES(record_count),
            version=VALUES(version),
            updated_at=NOW()
    """
    for ds in DATASETS:
        cursor.execute(sql, (
            ds["name"],
            ds["description"],
            ds["source_type"],
            ds["file_path"],
            ds["record_count"],
            ds["version"]
        ))
    print("Registered all 7 dataset sources in dataset_sources.")
