-- ====================================================================
-- GramDrishti AI Database Migration
-- V4: Infrastructure Indicators & Versioned Gap Scoring
-- ====================================================================

-- 1. Multi-Sector Infrastructure Indicators
CREATE TABLE IF NOT EXISTS village_infrastructure_indicators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    village_id BIGINT NOT NULL,
    indicator_type VARCHAR(50) NOT NULL,
    indicator_value DECIMAL(12,4) NOT NULL,
    normalized_score DECIMAL(5,2) NOT NULL,
    source VARCHAR(100) NOT NULL,
    source_year INT NOT NULL,
    confidence DECIMAL(5,2) DEFAULT 1.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (village_id) REFERENCES villages(id) ON DELETE CASCADE,
    INDEX idx_indicator_village (village_id),
    INDEX idx_indicator_type (indicator_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Versioned Village Gap Scores
CREATE TABLE IF NOT EXISTS village_gap_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    village_id BIGINT NOT NULL,
    road_gap DECIMAL(5,2) NOT NULL,
    water_gap DECIMAL(5,2) NOT NULL,
    connectivity_gap DECIMAL(5,2) NOT NULL,
    education_gap DECIMAL(5,2) NOT NULL,
    healthcare_gap DECIMAL(5,2) NOT NULL,
    digital_gap DECIMAL(5,2) NOT NULL,
    transport_gap DECIMAL(5,2) NOT NULL,
    socioeconomic_gap DECIMAL(5,2) NOT NULL,
    overall_gap_score DECIMAL(5,2) NOT NULL,
    priority_tier VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(5,2) NOT NULL,
    data_completeness DECIMAL(5,2) NOT NULL,
    calculation_version VARCHAR(20) NOT NULL,
    calculated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (village_id) REFERENCES villages(id) ON DELETE CASCADE,
    INDEX idx_gap_village (village_id),
    INDEX idx_gap_tier (priority_tier),
    INDEX idx_gap_score (overall_gap_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
