"""
GramDrishti AI - Data Ingestion Configuration & Normalization Utilities
"""

import os
import re
import unicodedata
from pathlib import Path

# Paths
BASE_DIR = Path(__file__).resolve().parent.parent.parent
DATASET_DIR = BASE_DIR / "dataset_1"
PROCESSED_DIR = DATASET_DIR / "processed"
VALIDATION_DIR = DATASET_DIR / "validation"
REPORTS_DIR = DATASET_DIR / "reports"
DOCS_DIR = BASE_DIR / "docs"

# Database Configuration
DB_CONFIG = {
    "host": os.getenv("MYSQL_HOST", "localhost"),
    "port": int(os.getenv("MYSQL_PORT", 3306)),
    "user": os.getenv("MYSQL_USER", "gramdrishti_user"),
    "password": os.getenv("MYSQL_PASSWORD", "gramdrishti_secret_pass"),
    "database": os.getenv("MYSQL_DATABASE", "gramdrishti_db"),
    "charset": "utf8mb4",
    "autocommit": False
}

# Streaming Chunk Sizes
CHUNK_SIZES = {
    "soi_toponyms": 25000,
    "habitations": 50000,
    "water_quality": 25000,
    "census": 10000
}

# Administrative Name Normalization
SUFFIX_PATTERN = re.compile(
    r'\b(gram panchayat|gp|revenue village|vlg|village|tehsil|taluk|block|district|dist|sub district)\b',
    re.IGNORECASE
)

def normalize_name(name: str) -> str:
    """Standardizes geographic and administrative names for entity resolution."""
    if not name or not isinstance(name, str):
        return ""
    # Normalize unicode to NFKC
    n = unicodedata.normalize('NFKC', name)
    # Lowercase
    n = n.lower()
    # Strip administrative suffixes
    n = SUFFIX_PATTERN.sub('', n)
    # Replace punctuation and special characters with spaces
    n = re.sub(r'[^a-z0-9\s\-]', ' ', n)
    # Collapse multiple whitespace
    n = re.sub(r'\s+', ' ', n).strip()
    return n

def safe_int(val, default=0):
    try:
        if val is None or val == '' or str(val).lower() == 'nan':
            return default
        return int(float(str(val).strip()))
    except (ValueError, TypeError):
        return default

def safe_float(val, default=None):
    try:
        if val is None or val == '' or str(val).lower() == 'nan':
            return default
        return float(str(val).strip())
    except (ValueError, TypeError):
        return default
