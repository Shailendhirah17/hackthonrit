"""
GramDrishti AI - Clean & Ingest Census 2011, Education & State GDPs
"""

import os
import re
import pandas as pd
from pathlib import Path
from config import DATASET_DIR, normalize_name, safe_int, safe_float
from validate_data import validate_population

STATE_CODE_MAP = {
    "JAMMU AND KASHMIR": "JK", "HIMACHAL PRADESH": "HP", "PUNJAB": "PB", "CHANDIGARH": "CH",
    "UTTARAKHAND": "UK", "HARYANA": "HR", "NCT OF DELHI": "DL", "DELHI": "DL",
    "RAJASTHAN": "RJ", "UTTAR PRADESH": "UP", "BIHAR": "BR", "SIKKIM": "SK",
    "ARUNACHAL PRADESH": "AR", "NAGALAND": "NL", "MANIPUR": "MN", "MIZORAM": "MZ",
    "TRIPURA": "TR", "MEGHALAYA": "ML", "ASSAM": "AS", "WEST BENGAL": "WB",
    "JHARKHAND": "JH", "ODISHA": "OR", "CHHATTISGARH": "CT", "MADHYA PRADESH": "MP",
    "GUJARAT": "GJ", "DAMAN AND DIU": "DD", "DADRA AND NAGAR HAVELI": "DN",
    "MAHARASHTRA": "MH", "ANDHRA PRADESH": "AP", "KARNATAKA": "KA", "GOA": "GA",
    "LAKSHADWEEP": "LD", "KERALA": "KL", "TAMIL NADU": "TN", "PUDUCHERRY": "PY",
    "ANDAMAN AND NICOBAR ISLANDS": "AN", "TELANGANA": "TG", "LADAKH": "LA"
}

def ingest_census_districts(cursor):
    """
    Ingests states and districts from india-districts-census-2011.csv,
    elementary_2015_16.csv, and state GDP files.
    """
    p4 = DATASET_DIR / "archive-4"
    census_file = p4 / "india-districts-census-2011.csv"
    if not census_file.exists():
        print(f"Census file not found: {census_file}")
        return 0, 0

    df_census = pd.read_csv(census_file)
    print(f"Loaded {len(df_census)} district rows from Census 2011.")

    # 1. Ingest States
    state_names = df_census["State Name"].dropna().unique()
    state_id_map = {}

    for s_name in state_names:
        clean_s = str(s_name).strip().upper()
        norm_s = normalize_name(clean_s)
        code = STATE_CODE_MAP.get(clean_s, clean_s[:2].upper())

        cursor.execute("""
            INSERT INTO states (state_code, state_name, normalized_name, created_at, updated_at)
            VALUES (%s, %s, %s, NOW(), NOW())
            ON DUPLICATE KEY UPDATE updated_at=NOW()
        """, (code, clean_s, norm_s))

        cursor.execute("SELECT id FROM states WHERE state_name = %s", (clean_s,))
        row = cursor.fetchone()
        if row:
            state_id_map[clean_s] = row[0]

    print(f"Synchronized {len(state_id_map)} states.")

    # 2. Ingest Districts
    districts_inserted = 0
    for _, row in df_census.iterrows():
        d_code = str(row.get("District code", "")).strip()
        s_name = str(row.get("State Name", "")).strip().upper()
        d_name = str(row.get("District name", "")).strip()
        norm_d = normalize_name(d_name)
        state_id = state_id_map.get(s_name)

        if not state_id or not d_name:
            continue

        pop = safe_int(row.get("Population"))
        sc_pop = safe_int(row.get("SC"))
        st_pop = safe_int(row.get("ST"))
        literate = safe_int(row.get("Literate"))
        female_lit = safe_int(row.get("Female_Literate"))
        female_pop = safe_int(row.get("Female"))

        lit_rate = round((literate / pop * 100.0), 2) if pop > 0 else None
        f_lit_rate = round((female_lit / female_pop * 100.0), 2) if female_pop > 0 else None

        cursor.execute("""
            INSERT INTO districts 
            (district_code, state_id, district_name, normalized_name, population, literacy_rate, female_literacy_rate, sc_population, st_population, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                population=VALUES(population),
                literacy_rate=VALUES(literacy_rate),
                female_literacy_rate=VALUES(female_literacy_rate),
                sc_population=VALUES(sc_population),
                st_population=VALUES(st_population),
                updated_at=NOW()
        """, (d_code, state_id, d_name, norm_d, pop, lit_rate, f_lit_rate, sc_pop, st_pop))
        districts_inserted += 1

    print(f"Synchronized {districts_inserted} districts from Census 2011.")

    # 3. Augment Elementary Education indicators
    elem_file = p4 / "elementary_2015_16.csv"
    if elem_file.exists():
        try:
            df_elem = pd.read_csv(elem_file, low_memory=False)
            print(f"Processing {len(df_elem)} elementary education records...")
            # Look for school counts and education indices
            for _, r in df_elem.iterrows():
                d_name = str(r.get("DISTRICT NAME", "")).strip()
                norm_d = normalize_name(d_name)
                # Find total schools
                tot_schools = safe_int(r.get("TOTAL SCHOOLS") or r.get("TOTAL ALL SCHOOLS") or r.get("TOTAL_SCHOOLS"))
                if tot_schools > 0 and norm_d:
                    cursor.execute("""
                        UPDATE districts 
                        SET school_count = %s, education_index = %s, updated_at = NOW()
                        WHERE normalized_name = %s
                    """, (tot_schools, min(100.0, max(10.0, tot_schools / 50.0)), norm_d))
            print("Elementary education indicators augmented.")
        except Exception as e:
            print(f"Note on elementary dataset parsing: {e}")

    return len(state_id_map), districts_inserted
