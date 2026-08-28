"""
GramDrishti AI - Entity Resolution & Matching Engine
"""

import math
from typing import Dict, Any, Tuple, Optional
from config import normalize_name

def jaro_winkler_similarity(s1: str, s2: str, p: float = 0.1, max_l: int = 4) -> float:
    """Computes Jaro-Winkler string similarity metric between two strings."""
    if s1 == s2:
        return 1.0
    if not s1 or not s2:
        return 0.0

    len1, len2 = len(s1), len(s2)
    match_distance = max(len1, len2) // 2 - 1
    if match_distance < 0:
        match_distance = 0

    s1_matches = [False] * len1
    s2_matches = [False] * len2

    matches = 0
    for i in range(len1):
        start = max(0, i - match_distance)
        end = min(i + match_distance + 1, len2)
        for j in range(start, end):
            if s2_matches[j] or s1[i] != s2[j]:
                continue
            s1_matches[i] = True
            s2_matches[j] = True
            matches += 1
            break

    if matches == 0:
        return 0.0

    transpositions = 0
    k = 0
    for i in range(len1):
        if not s1_matches[i]:
            continue
        while not s2_matches[k]:
            k += 1
        if s1[i] != s2[k]:
            transpositions += 1
        k += 1

    transpositions //= 2
    jaro = (matches / len1 + matches / len2 + (matches - transpositions) / matches) / 3.0

    # Common prefix length up to max_l
    prefix = 0
    for i in range(min(len1, len2, max_l)):
        if s1[i] == s2[i]:
            prefix += 1
        else:
            break

    return jaro + prefix * p * (1.0 - jaro)

def haversine_distance_km(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Computes great-circle distance between two points in kilometers."""
    R = 6371.0  # Earth radius in kilometers
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)

    a = math.sin(dphi / 2.0) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2.0) ** 2
    c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))
    return R * c

def compute_entity_match(
    src_name: str,
    tgt_name: str,
    src_coords: Optional[Tuple[float, float]] = None,
    tgt_coords: Optional[Tuple[float, float]] = None,
    src_hierarchy: Optional[Dict[str, str]] = None,
    tgt_hierarchy: Optional[Dict[str, str]] = None
) -> Tuple[str, float, str]:
    """
    Evaluates cross-dataset match and returns (status, confidence_score, matching_method).
    Statuses: AUTO_MATCHED, REVIEW_REQUIRED, UNMATCHED.
    """
    n_src = normalize_name(src_name)
    n_tgt = normalize_name(tgt_name)

    if not n_src or not n_tgt:
        return "UNMATCHED", 0.0, "EMPTY_NAME"

    # Tier 1: Exact normalized name match
    if n_src == n_tgt:
        # If hierarchy matches or not provided
        if src_hierarchy and tgt_hierarchy:
            h_match = (
                src_hierarchy.get("state", "").lower() == tgt_hierarchy.get("state", "").lower() and
                src_hierarchy.get("district", "").lower() == tgt_hierarchy.get("district", "").lower()
            )
            if h_match:
                return "AUTO_MATCHED", 1.0, "EXACT_HIERARCHY_AND_NAME"
        return "AUTO_MATCHED", 0.98, "EXACT_NAME_MATCH"

    # Tier 2: String Similarity
    similarity = jaro_winkler_similarity(n_src, n_tgt)

    # Spatial Proximity Verification if coordinates available
    if src_coords and tgt_coords:
        dist_km = haversine_distance_km(src_coords[0], src_coords[1], tgt_coords[0], tgt_coords[1])
        if similarity >= 0.85 and dist_km <= 5.0:
            return "AUTO_MATCHED", round(0.90 + (0.10 * (1.0 - dist_km / 5.0)), 4), "HIGH_SIMILARITY_WITH_SPATIAL_GATE"
        elif similarity >= 0.75 and dist_km <= 15.0:
            return "REVIEW_REQUIRED", round(0.75 + (0.10 * similarity), 4), "MODERATE_SIMILARITY_SPATIAL_PROXIMITY"

    if similarity >= 0.90:
        return "AUTO_MATCHED", round(similarity, 4), "JARO_WINKLER_HIGH_CONFIDENCE"
    elif similarity >= 0.75:
        return "REVIEW_REQUIRED", round(similarity, 4), "JARO_WINKLER_REVIEW_REQUIRED"
    else:
        return "UNMATCHED", round(similarity, 4), "SUB_THRESHOLD_SIMILARITY"
