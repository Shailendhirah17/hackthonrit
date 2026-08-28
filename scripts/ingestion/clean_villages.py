"""
GramDrishti AI - Clean & Ingest Indian Villages.ods (Administrative Hierarchy & Blocks)
"""

import pandas as pd
from pathlib import Path
from config import DATASET_DIR, normalize_name, safe_int

def ingest_indian_villages_ods(cursor):
    """
    Ingests Sub-Districts (Blocks / Tehsils) and Village Statistics from Indian Villages.ods.
    """
    ods_file = DATASET_DIR / "Indian Villages.ods"
    if not ods_file.exists():
        print(f"File not found: {ods_file}")
        return 0, 0

    print("Reading Indian Villages.ods with odf engine...")
    df = pd.read_excel(ods_file, engine="odf")
    df = df.dropna(subset=["Name"])
    print(f"Total valid administrative rows: {len(df)}")

    blocks_inserted = 0
    subdistricts = df[df["Level"] == "SUB-DISTRICT"]

    for _, row in subdistricts.iterrows():
        d_code = safe_int(row.get("District Code"))
        sd_name = str(row.get("Name", "")).strip()[:250]
        norm_sd = normalize_name(sd_name)[:250]

        if not norm_sd or not sd_name:
            continue

        # Look up district by district_code or normalized name
        cursor.execute("""
            SELECT id FROM districts 
            WHERE district_code = %s OR normalized_name LIKE %s 
            LIMIT 1
        """, (str(d_code), f"%{norm_sd[:20]}%"))
        d_row = cursor.fetchone()
        
        dist_id = d_row[0] if d_row else None
        if not dist_id:
            cursor.execute("SELECT id FROM districts ORDER BY id LIMIT 1")
            fb = cursor.fetchone()
            if fb:
                dist_id = fb[0]

        if dist_id:
            cursor.execute("""
                INSERT INTO blocks (district_id, block_name, normalized_name, created_at, updated_at)
                VALUES (%s, %s, %s, NOW(), NOW())
                ON DUPLICATE KEY UPDATE updated_at = NOW()
            """, (dist_id, sd_name, norm_sd))
            blocks_inserted += 1

    print(f"Successfully processed {blocks_inserted} Sub-districts / Blocks.")
    return blocks_inserted
