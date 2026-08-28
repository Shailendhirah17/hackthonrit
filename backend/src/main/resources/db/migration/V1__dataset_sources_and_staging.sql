-- ====================================================================
-- GramDrishti AI Database Migration
-- V1: Dataset Sources, Ingestion Batches, Rejection Log & Staging Tables
-- ====================================================================

-- 1. Dataset Source Registry
CREATE TABLE IF NOT EXISTS dataset_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dataset_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    source_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    record_count BIGINT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    version VARCHAR(50) DEFAULT '1.0',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Ingestion Batch Audit
CREATE TABLE IF NOT EXISTS dataset_ingestion_batches (
    id VARCHAR(64) PRIMARY KEY,
    dataset_source_id BIGINT NOT NULL,
    started_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    records_processed BIGINT DEFAULT 0,
    records_inserted BIGINT DEFAULT 0,
    records_updated BIGINT DEFAULT 0,
    records_rejected BIGINT DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    error_message TEXT NULL,
    FOREIGN KEY (dataset_source_id) REFERENCES dataset_sources(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Rejected Records Log
CREATE TABLE IF NOT EXISTS rejected_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_dataset VARCHAR(100) NOT NULL,
    source_file VARCHAR(255) NOT NULL,
    source_row BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    raw_reference LONGTEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rejected_dataset (source_dataset),
    INDEX idx_rejected_reason (reason)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Staging: Survey of India Toponyms
CREATE TABLE IF NOT EXISTS stg_soi_toponyms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    objectid_1 VARCHAR(50) NULL,
    feature VARCHAR(100) NULL,
    feature_1 VARCHAR(100) NULL,
    text VARCHAR(255) NULL,
    roman VARCHAR(255) NULL,
    hindi VARCHAR(255) NULL,
    bengali VARCHAR(255) NULL,
    gujarati VARCHAR(255) NULL,
    kannada VARCHAR(255) NULL,
    malayalam VARCHAR(255) NULL,
    marathi VARCHAR(255) NULL,
    punjabi VARCHAR(255) NULL,
    tamil VARCHAR(255) NULL,
    telugu VARCHAR(255) NULL,
    oriya VARCHAR(255) NULL,
    assamese VARCHAR(255) NULL,
    urdu VARCHAR(255) NULL,
    latitude VARCHAR(50) NULL,
    longitude VARCHAR(50) NULL,
    responsibi VARCHAR(100) NULL,
    osm_sheet_ VARCHAR(100) NULL,
    source_dataset VARCHAR(100) DEFAULT 'soi_toponyms.csv',
    source_file VARCHAR(255) DEFAULT 'soi_toponyms.csv',
    source_row_identifier BIGINT NULL,
    ingestion_batch_id VARCHAR(64) NULL,
    ingested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stg_soi_feature (feature),
    INDEX idx_stg_soi_batch (ingestion_batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Staging: Habitations
CREATE TABLE IF NOT EXISTS stg_habitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_name VARCHAR(150) NULL,
    district_name VARCHAR(150) NULL,
    block_name VARCHAR(150) NULL,
    panchayat_name VARCHAR(150) NULL,
    village_name VARCHAR(200) NULL,
    habitation_name VARCHAR(200) NULL,
    sc_population VARCHAR(50) NULL,
    st_population VARCHAR(50) NULL,
    general_population VARCHAR(50) NULL,
    sc_covered VARCHAR(50) NULL,
    st_covered VARCHAR(50) NULL,
    general_covered VARCHAR(50) NULL,
    coverage_status VARCHAR(50) NULL,
    source_year INT NULL,
    source_dataset VARCHAR(100) DEFAULT 'archive-5',
    source_file VARCHAR(255) NULL,
    source_row_identifier BIGINT NULL,
    ingestion_batch_id VARCHAR(64) NULL,
    ingested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stg_hab_state_dist (state_name, district_name),
    INDEX idx_stg_hab_year (source_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Staging: Water Quality
CREATE TABLE IF NOT EXISTS stg_water_quality (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_name VARCHAR(150) NULL,
    district_name VARCHAR(150) NULL,
    block_name VARCHAR(150) NULL,
    panchayat_name VARCHAR(150) NULL,
    village_name VARCHAR(200) NULL,
    habitation_name VARCHAR(200) NULL,
    quality_parameter VARCHAR(100) NULL,
    source_year INT NULL,
    source_dataset VARCHAR(100) DEFAULT 'archive-5',
    source_file VARCHAR(255) NULL,
    source_row_identifier BIGINT NULL,
    ingestion_batch_id VARCHAR(64) NULL,
    ingested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stg_wq_param (quality_parameter),
    INDEX idx_stg_wq_year (source_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Staging: District Census 2011
CREATE TABLE IF NOT EXISTS stg_district_census (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    district_code VARCHAR(50) NULL,
    state_name VARCHAR(150) NULL,
    district_name VARCHAR(150) NULL,
    population VARCHAR(50) NULL,
    male VARCHAR(50) NULL,
    female VARCHAR(50) NULL,
    literate VARCHAR(50) NULL,
    sc VARCHAR(50) NULL,
    st VARCHAR(50) NULL,
    raw_payload JSON NULL,
    source_dataset VARCHAR(100) DEFAULT 'archive-4',
    source_file VARCHAR(255) NULL,
    source_row_identifier BIGINT NULL,
    ingestion_batch_id VARCHAR(64) NULL,
    ingested_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
