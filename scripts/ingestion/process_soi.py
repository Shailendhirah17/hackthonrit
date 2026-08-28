"""
GramDrishti AI - Process & Stream Survey of India (SOI) Toponyms
"""

import pandas as pd
from pathlib import Path
from config import DATASET_DIR, CHUNK_SIZES, normalize_name
from validate_data import validate_coordinates, log_rejected_batch

def stream_soi_toponyms(cursor, limit_chunks=None):
    """
    Streams soi_toponyms.csv in chunks of 25k records without loading the entire 943 MB file into memory.
    Validates WGS84 coordinates and inserts into `geographic_features`.
    """
    soi_file = DATASET_DIR / "soi_toponyms.csv"
    if not soi_file.exists():
        print(f"File not found: {soi_file}")
        return 0, 0

    chunk_size = CHUNK_SIZES.get("soi_toponyms", 25000)
    print(f"Streaming {soi_file.name} in chunks of {chunk_size} records...")

    total_valid = 0
    total_rejected = 0
    chunk_count = 0

    insert_sql = """
        INSERT INTO geographic_features 
        (feature_type, feature_name, normalized_name, state_name, district_name, block_name, latitude, longitude, source, created_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, 'SURVEY_OF_INDIA', NOW())
    """

    reader = pd.read_csv(
        soi_file,
        chunksize=chunk_size,
        encoding="latin1",
        low_memory=False
    )

    for chunk in reader:
        chunk_count += 1
        valid_batch = []
        rejected_batch = []

        for idx, row in chunk.iterrows():
            feat_type = str(row.get("feature", "")).strip().upper() or "GEOGRAPHIC_FEATURE"
            name = str(row.get("text") or row.get("roman") or "").strip()
            norm_name = normalize_name(name)

            if not name:
                rejected_batch.append((
                    "soi_toponyms.csv",
                    "soi_toponyms.csv",
                    idx,
                    "EMPTY_FEATURE_NAME",
                    str(row.to_dict())
                ))
                continue

            lat = row.get("latitude")
            lng = row.get("longitude")
            is_valid, reason, f_lat, f_lng = validate_coordinates(lat, lng)

            if not is_valid:
                rejected_batch.append((
                    "soi_toponyms.csv",
                    "soi_toponyms.csv",
                    idx,
                    reason,
                    str(row.to_dict())
                ))
            else:
                valid_batch.append((
                    feat_type,
                    name,
                    norm_name,
                    None,
                    None,
                    None,
                    f_lat,
                    f_lng
                ))

        if valid_batch:
            cursor.executemany(insert_sql, valid_batch)
            total_valid += len(valid_batch)

        if rejected_batch:
            log_rejected_batch(cursor, rejected_batch)
            total_rejected += len(rejected_batch)

        print(f"Chunk {chunk_count}: Processed {len(chunk)} records (Valid: {len(valid_batch)}, Rejected: {len(rejected_batch)}) | Total Valid: {total_valid}")

        if limit_chunks and chunk_count >= limit_chunks:
            print(f"Reached chunk limit ({limit_chunks}).")
            break

    print(f"Completed SOI Toponyms ingestion: {total_valid} valid, {total_rejected} rejected.")
    return total_valid, total_rejected
