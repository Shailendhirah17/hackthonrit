"""
GramDrishti AI - Clean & Ingest Basic Habitation Information (Archive-5)
"""

import re
import pandas as pd
from pathlib import Path
from config import DATASET_DIR, CHUNK_SIZES, normalize_name, safe_int
from validate_data import validate_population, validate_year, log_rejected_batch

YEAR_FILE_MAP = {
    "Basic_habitation_info_2009_04_01.csv": 2009,
    "Basic_habitation_info_2010_04_01.csv": 2010,
    "Basic_habitation_info_2011_04_01.csv": 2011,
    "Basic_habitation_info_2012_04_01.csv": 2012,
}

def stream_basic_habitations(cursor, limit_chunks_per_file=None):
    """
    Streams multi-year basic habitation files from archive-5 in chunks.
    Preserves longitudinal trends across 2009, 2010, 2011, 2012 without overwriting historical audits.
    """
    hab_dir = DATASET_DIR / "archive-5" / "Basic habitation information"
    if not hab_dir.exists():
        print(f"Directory not found: {hab_dir}")
        return 0, 0

    chunk_size = CHUNK_SIZES.get("habitations", 50000)

    # Pre-cache states, districts, blocks, and villages
    cursor.execute("SELECT id, normalized_name FROM states")
    states_cache = {row[1]: row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, state_id, normalized_name FROM districts")
    districts_cache = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, district_id, normalized_name FROM blocks")
    blocks_cache = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    cursor.execute("SELECT id, block_id, normalized_name FROM villages")
    villages_cache = {(row[1], row[2]): row[0] for row in cursor.fetchall()}

    total_habitations_inserted = 0
    total_rejected = 0

    hab_insert_sql = """
        INSERT INTO habitations 
        (village_id, habitation_name, normalized_name, sc_population, st_population, general_population, total_population, coverage_status, water_quality_status, source_year, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
    """

    for file_name, s_year in YEAR_FILE_MAP.items():
        file_path = hab_dir / file_name
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
                s_name = str(row.get("State Name", "")).strip()[:140].upper()
                d_name = str(row.get("District Name", "")).strip()[:140]
                b_name = str(row.get("Block Name", "")).strip()[:140]
                v_name = str(row.get("Village Name", "")).strip()[:140]
                h_name = str(row.get("Habitation Name", "")).strip()[:140]

                if not h_name or not v_name:
                    rejected_batch.append((
                        "archive-5",
                        file_name,
                        idx,
                        "EMPTY_VILLAGE_OR_HABITATION_NAME",
                        str(row.to_dict())
                    ))
                    continue

                norm_s = normalize_name(s_name)[:140]
                norm_d = normalize_name(d_name)[:140]
                norm_b = normalize_name(b_name)[:140]
                norm_v = normalize_name(v_name)[:140]
                norm_h = normalize_name(h_name)[:140]

                # 1. State lookup / auto-provision
                state_id = states_cache.get(norm_s)
                if not state_id:
                    code = norm_s[:2].upper() if len(norm_s) >= 2 else "IN"
                    cursor.execute("""
                        INSERT INTO states (state_code, state_name, normalized_name, created_at, updated_at)
                        VALUES (%s, %s, %s, NOW(), NOW())
                        ON DUPLICATE KEY UPDATE updated_at=NOW()
                    """, (code, s_name, norm_s))
                    cursor.execute("SELECT id FROM states WHERE normalized_name = %s", (norm_s,))
                    st_row = cursor.fetchone()
                    state_id = st_row[0] if st_row else 1
                    states_cache[norm_s] = state_id

                # 2. District lookup / auto-provision
                dist_key = (state_id, norm_d)
                dist_id = districts_cache.get(dist_key)
                if not dist_id:
                    d_code = f"D-{state_id}-{len(districts_cache) + 1}"
                    cursor.execute("""
                        INSERT INTO districts (district_code, state_id, district_name, normalized_name, created_at, updated_at)
                        VALUES (%s, %s, %s, %s, NOW(), NOW())
                        ON DUPLICATE KEY UPDATE updated_at=NOW()
                    """, (d_code, state_id, d_name, norm_d))
                    cursor.execute("SELECT id FROM districts WHERE state_id = %s AND normalized_name = %s", (state_id, norm_d))
                    dt_row = cursor.fetchone()
                    dist_id = dt_row[0] if dt_row else 1
                    districts_cache[dist_key] = dist_id

                # 3. Block lookup / auto-provision
                block_key = (dist_id, norm_b)
                block_id = blocks_cache.get(block_key)
                if not block_id:
                    cursor.execute("""
                        INSERT INTO blocks (district_id, block_name, normalized_name, created_at, updated_at)
                        VALUES (%s, %s, %s, NOW(), NOW())
                        ON DUPLICATE KEY UPDATE updated_at=NOW()
                    """, (dist_id, b_name, norm_b))
                    cursor.execute("SELECT id FROM blocks WHERE district_id = %s AND normalized_name = %s", (dist_id, norm_b))
                    bk_row = cursor.fetchone()
                    block_id = bk_row[0] if bk_row else 1
                    blocks_cache[block_key] = block_id

                # Population validation
                sc_pop = safe_int(row.get("SC Current Population"))
                st_pop = safe_int(row.get("ST Current Population"))
                gen_pop = safe_int(row.get("GENERAL Current Population"))
                tot_pop = sc_pop + st_pop + gen_pop

                # 4. Village lookup / auto-provision
                v_key = (block_id, norm_v)
                v_id = villages_cache.get(v_key)
                if not v_id:
                    v_code = f"GD-{state_id}-{dist_id}-{len(villages_cache) + 1}"
                    cursor.execute("""
                        INSERT INTO villages 
                        (census_code, block_id, state, district, block, village_name, normalized_name, population, sc_population, st_population, primary_data_source, gap_score, adequacy_score, priority, is_deleted, created_at, updated_at)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 'archive-5', 50.0, 50.0, 'MEDIUM', 0, NOW(), NOW())
                        ON DUPLICATE KEY UPDATE updated_at=NOW()
                    """, (v_code, block_id, s_name, d_name, b_name, v_name, norm_v, tot_pop, sc_pop, st_pop))
                    cursor.execute("SELECT id FROM villages WHERE block_id = %s AND normalized_name = %s", (block_id, norm_v))
                    vg_row = cursor.fetchone()
                    v_id = vg_row[0] if vg_row else 1
                    villages_cache[v_key] = v_id

                cov_status = str(row.get("Status", "")).strip() or "Not Available"

                valid_batch.append((
                    v_id,
                    h_name,
                    norm_h,
                    sc_pop,
                    st_pop,
                    gen_pop,
                    tot_pop,
                    cov_status,
                    "SAFE",
                    s_year
                ))

            if valid_batch:
                cursor.executemany(hab_insert_sql, valid_batch)
                file_valid += len(valid_batch)
                total_habitations_inserted += len(valid_batch)

            if rejected_batch:
                log_rejected_batch(cursor, rejected_batch)
                file_rejected += len(rejected_batch)
                total_rejected += len(rejected_batch)

            print(f"[{file_name}] Chunk {chunk_idx}: Inserted {len(valid_batch)} habitations (File Valid: {file_valid})")

            if limit_chunks_per_file and chunk_idx >= limit_chunks_per_file:
                print(f"Reached chunk limit for {file_name}")
                break

    print(f"\nHabitation Ingestion Complete: {total_habitations_inserted} valid, {total_rejected} rejected.")
    return total_habitations_inserted, total_rejected
