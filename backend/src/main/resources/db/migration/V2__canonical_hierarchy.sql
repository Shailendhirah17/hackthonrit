-- ====================================================================
-- GramDrishti AI Database Migration
-- V2: Canonical Administrative Hierarchy
-- ====================================================================

-- 1. States Table
CREATE TABLE IF NOT EXISTS states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_code VARCHAR(10) NOT NULL UNIQUE,
    state_name VARCHAR(100) NOT NULL UNIQUE,
    normalized_name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_state_norm (normalized_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Districts Table
CREATE TABLE IF NOT EXISTS districts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    district_code VARCHAR(20) NOT NULL UNIQUE,
    state_id BIGINT NOT NULL,
    district_name VARCHAR(150) NOT NULL,
    normalized_name VARCHAR(150) NOT NULL,
    population BIGINT NULL,
    literacy_rate DECIMAL(5,2) NULL,
    female_literacy_rate DECIMAL(5,2) NULL,
    sc_population BIGINT NULL,
    st_population BIGINT NULL,
    gdp DECIMAL(18,2) NULL,
    gdp_year INT NULL,
    school_count INT NULL,
    education_index DECIMAL(5,2) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (state_id) REFERENCES states(id) ON DELETE RESTRICT,
    INDEX idx_district_state (state_id),
    INDEX idx_district_norm (normalized_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Blocks Table
CREATE TABLE IF NOT EXISTS blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    district_id BIGINT NOT NULL,
    block_name VARCHAR(150) NOT NULL,
    normalized_name VARCHAR(150) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (district_id) REFERENCES districts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_district_block (district_id, normalized_name),
    INDEX idx_block_district (district_id),
    INDEX idx_block_norm (normalized_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Villages Table
CREATE TABLE IF NOT EXISTS villages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    village_code VARCHAR(50) NULL UNIQUE,
    block_id BIGINT NOT NULL,
    village_name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    population BIGINT DEFAULT 0,
    male_population BIGINT DEFAULT 0,
    female_population BIGINT DEFAULT 0,
    sc_population BIGINT DEFAULT 0,
    st_population BIGINT DEFAULT 0,
    road_index DECIMAL(5,2) NULL,
    water_index DECIMAL(5,2) NULL,
    connectivity_index DECIMAL(5,2) NULL,
    education_index DECIMAL(5,2) NULL,
    healthcare_index DECIMAL(5,2) NULL,
    socioeconomic_index DECIMAL(5,2) NULL,
    infrastructure_gap_score DECIMAL(5,2) NULL,
    priority_tier VARCHAR(20) DEFAULT 'MEDIUM',
    score_confidence DECIMAL(5,2) NULL,
    data_completeness DECIMAL(5,2) DEFAULT 0.0,
    primary_data_source VARCHAR(100) NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    FOREIGN KEY (block_id) REFERENCES blocks(id) ON DELETE CASCADE,
    INDEX idx_village_block (block_id),
    INDEX idx_village_norm (normalized_name),
    INDEX idx_village_coords (latitude, longitude),
    INDEX idx_village_priority (priority_tier),
    INDEX idx_village_gap (infrastructure_gap_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Habitations Table
CREATE TABLE IF NOT EXISTS habitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    village_id BIGINT NOT NULL,
    habitation_name VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    sc_population BIGINT DEFAULT 0,
    st_population BIGINT DEFAULT 0,
    general_population BIGINT DEFAULT 0,
    total_population BIGINT DEFAULT 0,
    coverage_status VARCHAR(50) NULL,
    water_quality_status VARCHAR(50) NULL,
    source_year INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (village_id) REFERENCES villages(id) ON DELETE CASCADE,
    INDEX idx_habitation_village (village_id),
    INDEX idx_habitation_year (source_year),
    INDEX idx_habitation_status (coverage_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Water Quality Records Table
CREATE TABLE IF NOT EXISTS water_quality_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    habitation_id BIGINT NOT NULL,
    parameter VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    value DECIMAL(10,4) NULL,
    unit VARCHAR(20) DEFAULT 'mg/L',
    source_year INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (habitation_id) REFERENCES habitations(id) ON DELETE CASCADE,
    INDEX idx_water_habitation (habitation_id),
    INDEX idx_water_parameter (parameter),
    INDEX idx_water_year (source_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
