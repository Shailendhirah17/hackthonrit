-- ====================================================================
-- GramDrishti AI Database Migration
-- V5: AI Computer Vision Models, Jobs, Detections & Segmentation
-- ====================================================================

-- 1. AI Models Catalog
CREATE TABLE IF NOT EXISTS ai_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    version VARCHAR(50) NOT NULL,
    framework VARCHAR(50) NOT NULL,
    classes JSON NOT NULL,
    accuracy DECIMAL(5,4) NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. AI Inference Jobs
CREATE TABLE IF NOT EXISTS ai_jobs (
    id VARCHAR(64) PRIMARY KEY,
    project_id BIGINT NULL,
    model_id BIGINT NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress INT DEFAULT 0,
    started_at DATETIME NULL,
    completed_at DATETIME NULL,
    error_message TEXT NULL,
    FOREIGN KEY (model_id) REFERENCES ai_models(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. AI Object Detections (YOLO Road Obstacles, Potholes, Speed Bumps)
CREATE TABLE IF NOT EXISTS ai_detections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL,
    image_reference VARCHAR(500) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    confidence DECIMAL(5,4) NOT NULL,
    x_center DECIMAL(7,6) NOT NULL,
    y_center DECIMAL(7,6) NOT NULL,
    width DECIMAL(7,6) NOT NULL,
    height DECIMAL(7,6) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES ai_jobs(id) ON DELETE CASCADE,
    INDEX idx_detection_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. AI Semantic Segmentation Results (Aerial Landcover & Satellite Roads)
CREATE TABLE IF NOT EXISTS ai_segmentation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(64) NOT NULL,
    image_reference VARCHAR(500) NOT NULL,
    class_name VARCHAR(100) NOT NULL,
    pixel_count BIGINT NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES ai_jobs(id) ON DELETE CASCADE,
    INDEX idx_seg_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
