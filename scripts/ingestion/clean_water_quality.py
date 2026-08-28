"""
GramDrishti AI - Clean & Ingest Water Quality Contamination Records (Archive-5)
"""

import pandas as pd
from pathlib import Path
from config import DATASET_DIR, CHUNK_SIZES, normalize_name
from validate_data import log_rejected_batch

WQ_YEAR_MAP = {
    "Water_quality_affected_habitation_2009_04_01.csv": 2009,
    "Water_quality_affected_habitation_2010_04_01.csv": 2010,
    "Water_quality_affected_habitation_2011_04_01.csv": 2011,
    "Water_quality_affected_habitation_2012_04_01.csv": 2012,
}

def stream_water_quality_records(cursor, limit_chunks_per_file=None):
    """
    Streams water quality affected habitation datasets and records specific chemical contamination parameters
    (Fluoride, Arsenic, Iron, Salinity, Nitrate).
    Uses in-memory dictionary caching for instantaneous O(1) habitation lookups.
    """
    wq_dir = DATASET_DIR / "archive-5" / "Water quality affected habitation"
    if not wq_dir.exists():
        print(f"Directory not found: {wq_dir}")
        return 0, 0

    chunk_size = CHUNK_SIZES.get("water_quality", 25000)

    print("Pre-caching habitations mapping for fast O(1) lookup...")
    cursor.execute("SELECT id, normalized_name, source_year FROM habitations")
    hab_cache_year = {}
    hab_cache_name = {}
    for h_id, h_norm, h_yr in cursor.fetchall():
        hab_cache_year[(h_norm, h_yr)] = h_id
        if h_norm not in hab_cache_name:
            hab_cache_name[h_norm] = h_id
    print(f"Cached {len(hab_cache_year)} habitations in memory.")

    total_wq_inserted = 0
    total_rejected = 0
    contaminated_hab_ids = set()

    wq_insert_sql = """
        INSERT INTO water_quality_records 
        (habitation_id, parameter, status, value, unit, source_year, created_at)
        VALUES (%s, %s, 'CONTAMINATED', NULL, 'mg/L', %s, NOW())
    """

    for file_name, s_year in WQ_YEAR_MAP.items():
        file_path = wq_dir / file_name
        if not file_path.exists():
            continue

        print(f"\nProcessing {file_name} (Year: {s_year}) in chunks of {chunk_size}...")
        reader = pd.read_csv(
            file_path,
            chunksize=chunk_size,
            encoding="latin1",
            low_memory=False
        )

        chunk_idx = 0
        file_valid = 0
        file_rejected = 0

        for chunk in reader:
            chunk_idx += 1
            valid_batch = []
            rejected_batch = []

            for idx, row in chunk.iterrows():
                h_name = str(row.get("Habitation Name", "")).strip()[:140]
                norm_h = normalize_name(h_name)[:140]
                param = str(row.get("Quality Parameter", "")).strip()[:45] or "Unknown Contaminant"

                if not h_name:
                    rejected_batch.append((
                        "archive-5",
                        file_name,
                        idx,
                        "EMPTY_HABITATION_NAME_IN_WATER_QUALITY",
                        str(row.to_dict())
                    ))
                    continue

                hab_id = hab_cache_year.get((norm_h, s_year)) or hab_cache_name.get(norm_h)

                if hab_id:
                    valid_batch.append((hab_id, param, s_year))
                    contaminated_hab_ids.add(hab_id)
                else:
                    rejected_batch.append((
                        "archive-5",
                        file_name,
                        idx,
                        f"UNRESOLVED_HABITATION: {h_name}",
                        str(row.to_dict())
                    ))

            if valid_batch:
                cursor.executemany(wq_insert_sql, valid_batch)
                file_valid += len(valid_batch)
                total_wq_inserted += len(valid_batch)

            if rejected_batch:
                log_rejected_batch(cursor, rejected_batch)
                file_rejected += len(rejected_batch)
                total_rejected += len(rejected_batch)

            print(f"[{file_name}] Chunk {chunk_idx}: Linked {len(valid_batch)} water contamination records (File Total: {file_valid})")

            if limit_chunks_per_file and chunk_idx >= limit_chunks_per_file:
                print(f"Reached chunk limit for {file_name}")
                break

    # Bulk update contaminated status
    if contaminated_hab_ids:
        print(f"Updating water_quality_status for {len(contaminated_hab_ids)} affected habitations...")
        # Update in batches of 5000
        hab_list = list(contaminated_hab_ids)
        for i in range(0, len(hab_list), 5000):
            batch = hab_list[i:i+5000]
            fmt_str = ','.join(['%s'] * len(batch))
            cursor.execute(f"UPDATE habitations SET water_quality_status = 'CONTAMINATED' WHERE id IN ({fmt_str})", batch)

    print(f"\nWater Quality Ingestion Complete: {total_wq_inserted} valid records linked, {total_rejected} rejected.")
    return total_wq_inserted, total_rejected
