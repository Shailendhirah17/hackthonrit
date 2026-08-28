"""
GramDrishti AI - Data Validation & Rejection Logging
"""

import json
from typing import Optional, Tuple, Any

def validate_coordinates(lat: Any, lng: Any) -> Tuple[bool, Optional[str], Optional[float], Optional[float]]:
    """Validates WGS84 coordinates."""
    if lat is None or lng is None or str(lat).strip() == "" or str(lng).strip() == "":
        return False, "MISSING_COORDINATES", None, None
    try:
        f_lat = float(str(lat).strip())
        f_lng = float(str(lng).strip())
    except (ValueError, TypeError):
        return False, "INVALID_NUMERIC_COORDINATE_FORMAT", None, None

    if not (-90.0 <= f_lat <= 90.0):
        return False, f"LATITUDE_OUT_OF_BOUNDS: {f_lat}", None, None
    if not (-180.0 <= f_lng <= 180.0):
        return False, f"LONGITUDE_OUT_OF_BOUNDS: {f_lng}", None, None
    
    # Specific bounds for India (+6.0 to +38.0 Lat, +68.0 to +98.0 Lng)
    if not (5.0 <= f_lat <= 40.0 and 65.0 <= f_lng <= 100.0):
        return False, f"OUTSIDE_INDIAN_GEOGRAPHIC_BOUNDS: ({f_lat}, {f_lng})", None, None

    return True, None, f_lat, f_lng

def validate_population(pop: Any) -> Tuple[bool, Optional[str], int]:
    """Validates population integers."""
    if pop is None or str(pop).strip() == "" or str(pop).lower() == "nan":
        return True, None, 0
    try:
        val = int(float(str(pop).strip()))
        if val < 0:
            return False, f"NEGATIVE_POPULATION: {val}", 0
        return True, None, val
    except (ValueError, TypeError):
        return False, f"INVALID_POPULATION_FORMAT: {pop}", 0

def validate_year(year: Any) -> Tuple[bool, Optional[str], int]:
    """Validates source year."""
    try:
        y = int(float(str(year).strip()))
        if 1990 <= y <= 2030:
            return True, None, y
        return False, f"YEAR_OUT_OF_RANGE: {y}", y
    except (ValueError, TypeError):
        return False, f"INVALID_YEAR_FORMAT: {year}", 0

def log_rejected_batch(cursor, rejected_list):
    """Inserts a batch of rejected records into `rejected_records`."""
    if not rejected_list:
        return
    sql = """
        INSERT INTO rejected_records 
        (source_dataset, source_file, source_row, reason, raw_reference, created_at)
        VALUES (%s, %s, %s, %s, %s, NOW())
    """
    cursor.executemany(sql, rejected_list)
