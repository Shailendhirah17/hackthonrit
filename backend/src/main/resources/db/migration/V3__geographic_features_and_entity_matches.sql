-- ====================================================================
-- GramDrishti AI Database Migration
-- V3: Survey of India Geographic Features & Entity Resolution Matches
-- ====================================================================

-- 1. Survey of India Geographic Features
CREATE TABLE IF NOT EXISTS geographic_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_type VARCHAR(100) NOT NULL,
    feature_name VARCHAR(255) NOT NULL,
    normalized_name VARCHAR(255) NOT NULL,
    state_name VARCHAR(100) NULL,
    district_name VARCHAR(150) NULL,
    block_name VARCHAR(150) NULL,
    latitude DECIMAL(10,7) NOT NULL,
    longitude DECIMAL(10,7) NOT NULL,
    source VARCHAR(50) DEFAULT 'SURVEY_OF_INDIA',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_geo_feature_type (feature_type),
    INDEX idx_geo_norm (normalized_name),
    INDEX idx_geo_coords (latitude, longitude),
    INDEX idx_geo_state_dist (state_name, district_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Entity Matches Table
CREATE TABLE IF NOT EXISTS entity_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_dataset VARCHAR(100) NOT NULL,
    source_record_id VARCHAR(255) NOT NULL,
    target_entity_type VARCHAR(50) NOT NULL,
    target_entity_id BIGINT NOT NULL,
    matching_method VARCHAR(100) NOT NULL,
    confidence_score DECIMAL(5,4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_match_target (target_entity_type, target_entity_id),
    INDEX idx_match_status (status),
    INDEX idx_match_confidence (confidence_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
