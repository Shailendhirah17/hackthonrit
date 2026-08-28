"""
GramDrishti AI - Entity Resolution Execution Script
"""

from entity_matching import jaro_winkler_similarity
from config import normalize_name

def execute_village_to_soi_matching(cursor, limit=5000):
    """
    Matches villages in database with Survey of India geographic features.
    Attaches spatial coordinates for high-confidence matches.
    Uses pre-cached hash map for high-speed candidate retrieval.
    """
    print(f"\n--- Running Entity Resolution on Villages (Batch Limit: {limit}) ---")

    # Fetch villages without coordinates or pending resolution
    cursor.execute("""
        SELECT v.id, v.village_name, v.normalized_name, d.district_name, s.state_name
        FROM villages v
        JOIN blocks b ON v.block_id = b.id
        JOIN districts d ON b.district_id = d.id
        JOIN states s ON d.state_id = s.id
        LIMIT %s
    """, (limit,))
    villages = cursor.fetchall()
    print(f"Evaluating {len(villages)} villages against Survey of India geographic features...")

    print("Pre-caching geographic features for instant matching...")
    cursor.execute("SELECT id, feature_name, normalized_name, latitude, longitude FROM geographic_features LIMIT 100000")
    geo_rows = cursor.fetchall()
    geo_map = {}
    for g_id, g_name, g_norm, g_lat, g_lng in geo_rows:
        if g_norm not in geo_map:
            geo_map[g_norm] = []
        geo_map[g_norm].append((g_id, g_name, g_norm, g_lat, g_lng))
    print(f"Cached {len(geo_map)} distinct geographic feature names.")

    matches_inserted = 0
    auto_matched = 0
    review_required = 0
    unmatched = 0

    match_batch = []
    village_coord_updates = []

    match_sql = """
        INSERT INTO entity_matches 
        (source_dataset, source_record_id, target_entity_type, target_entity_id, matching_method, confidence_score, status, created_at)
        VALUES (%s, %s, 'VILLAGE', %s, %s, %s, %s, NOW())
    """

    for v_id, v_name, v_norm, d_name, s_name in villages:
        candidates = geo_map.get(v_norm, [])

        if not candidates:
            # Check for prefix candidates
            if len(v_norm) >= 4:
                prefix = v_norm[:4]
                for k, v_list in geo_map.items():
                    if k.startswith(prefix):
                        candidates.extend(v_list)
                        if len(candidates) >= 5:
                            break

        if not candidates:
            match_batch.append((
                "soi_toponyms.csv",
                f"V_{v_id}",
                v_id,
                "NO_CANDIDATES_FOUND",
                0.0,
                "UNMATCHED"
            ))
            unmatched += 1
            continue

        best_status = "UNMATCHED"
        best_score = 0.0
        best_method = "NONE"
        best_lat = None
        best_lng = None

        for c_id, c_name, c_norm, c_lat, c_lng in candidates[:10]:
            sim = jaro_winkler_similarity(v_norm, c_norm)
            if sim > best_score:
                best_score = sim
                best_lat = c_lat
                best_lng = c_lng
                if sim >= 0.88:
                    best_status = "AUTO_MATCHED"
                    best_method = "JARO_WINKLER_HIGH_CONFIDENCE"
                elif sim >= 0.75:
                    best_status = "REVIEW_REQUIRED"
                    best_method = "JARO_WINKLER_REVIEW_REQUIRED"
                else:
                    best_status = "UNMATCHED"
                    best_method = "SUB_THRESHOLD"

        match_batch.append((
            "soi_toponyms.csv",
            f"V_{v_id}",
            v_id,
            best_method,
            round(best_score, 4),
            best_status
        ))
        matches_inserted += 1

        if best_status == "AUTO_MATCHED":
            auto_matched += 1
            if best_lat and best_lng:
                village_coord_updates.append((best_lat, best_lng, v_id))
        elif best_status == "REVIEW_REQUIRED":
            review_required += 1
        else:
            unmatched += 1

    if match_batch:
        cursor.executemany(match_sql, match_batch)

    if village_coord_updates:
        print(f"Promoting coordinates for {len(village_coord_updates)} AUTO_MATCHED villages...")
        cursor.executemany("""
            UPDATE villages 
            SET latitude = %s, longitude = %s, updated_at = NOW() 
            WHERE id = %s
        """, village_coord_updates)

    print(f"Entity Resolution Complete: {matches_inserted} evaluated.")
    print(f" - AUTO_MATCHED (Promoted): {auto_matched}")
    print(f" - REVIEW_REQUIRED: {review_required}")
    print(f" - UNMATCHED: {unmatched}")
    return matches_inserted, auto_matched, review_required, unmatched
