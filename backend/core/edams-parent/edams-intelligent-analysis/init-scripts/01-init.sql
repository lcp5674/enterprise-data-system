-- EDAMS Intelligent Analysis Database Schema
-- Auto-generated initialization script

CREATE DATABASE IF NOT EXISTS edams_analysis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE edams_analysis;

-- Local Model Configuration Table
CREATE TABLE IF NOT EXISTS local_model_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL COMMENT 'Model name',
    model_type VARCHAR(50) NOT NULL COMMENT 'Model type: OLLAMA, LOCALAI, OPENAI_COMPATIBLE',
    base_url VARCHAR(500) NOT NULL COMMENT 'API base URL',
    api_key VARCHAR(500) DEFAULT NULL COMMENT 'API key (optional)',
    model_version VARCHAR(50) DEFAULT NULL COMMENT 'Model version',
    max_tokens INT DEFAULT 4096 COMMENT 'Max tokens for response',
    temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT 'Temperature for generation',
    timeout_seconds INT DEFAULT 120 COMMENT 'Request timeout in seconds',
    enabled BOOLEAN DEFAULT TRUE COMMENT 'Whether this config is enabled',
    is_default BOOLEAN DEFAULT FALSE COMMENT 'Is default configuration',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT NULL,
    updated_by VARCHAR(100) DEFAULT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_enabled (enabled),
    INDEX idx_is_default (is_default),
    INDEX idx_model_type (model_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Local LLM Model Configuration';

-- Analysis Task Table
CREATE TABLE IF NOT EXISTS analysis_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(200) NOT NULL COMMENT 'Task name',
    task_description TEXT DEFAULT NULL COMMENT 'Task description',
    datasource_id BIGINT NOT NULL COMMENT 'Data source ID',
    datasource_type VARCHAR(50) NOT NULL COMMENT 'Data source type',
    target_databases TEXT DEFAULT NULL COMMENT 'Target databases (JSON array)',
    target_tables TEXT DEFAULT NULL COMMENT 'Target tables (JSON array)',
    metadata_tables TEXT DEFAULT NULL COMMENT 'Tables containing metadata (JSON array)',
    analysis_scope VARCHAR(50) DEFAULT 'FULL' COMMENT 'Analysis scope: FULL, SCHEMA_ONLY, DATA_SAMPLE',
    execution_mode VARCHAR(50) DEFAULT 'MANUAL' COMMENT 'Execution mode: MANUAL, ASYNC, SCHEDULED',
    cron_expression VARCHAR(100) DEFAULT NULL COMMENT 'Cron expression for scheduled tasks',
    model_config_id BIGINT DEFAULT NULL COMMENT 'Model configuration ID',
    batch_size INT DEFAULT 10 COMMENT 'Batch size for processing',
    status VARCHAR(50) DEFAULT 'PENDING' COMMENT 'Task status: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED',
    progress INT DEFAULT 0 COMMENT 'Progress percentage (0-100)',
    error_message TEXT DEFAULT NULL COMMENT 'Error message if failed',
    total_tables INT DEFAULT 0 COMMENT 'Total tables to analyze',
    analyzed_tables INT DEFAULT 0 COMMENT 'Tables already analyzed',
    started_at TIMESTAMP NULL DEFAULT NULL,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_status (status),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_execution_mode (execution_mode),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Analysis Task Management';

-- Analysis Result Table
CREATE TABLE IF NOT EXISTS analysis_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT 'Parent task ID',
    table_name VARCHAR(200) NOT NULL COMMENT 'Analyzed table name',
    database_name VARCHAR(100) NOT NULL COMMENT 'Database name',
    table_comment TEXT DEFAULT NULL COMMENT 'Table comment/description',
    table_type VARCHAR(50) DEFAULT 'TABLE' COMMENT 'Table type: TABLE, VIEW',
    row_count BIGINT DEFAULT NULL COMMENT 'Estimated row count',
    data_volume_mb DECIMAL(10,2) DEFAULT NULL COMMENT 'Data volume in MB',
    columns_count INT DEFAULT NULL COMMENT 'Number of columns',
    subject_classification VARCHAR(200) DEFAULT NULL COMMENT 'Subject classification result',
    business_domain VARCHAR(200) DEFAULT NULL COMMENT 'Business domain',
    data_category VARCHAR(100) DEFAULT NULL COMMENT 'Data category',
    importance_level VARCHAR(50) DEFAULT 'MEDIUM' COMMENT 'Importance level: HIGH, MEDIUM, LOW',
    confidence_score DECIMAL(5,4) DEFAULT NULL COMMENT 'AI confidence score (0-1)',
    analysis_summary TEXT DEFAULT NULL COMMENT 'AI-generated analysis summary',
    recommendations TEXT DEFAULT NULL COMMENT 'Recommendations from AI',
    extracted_indicators TEXT DEFAULT NULL COMMENT 'Extracted indicator definitions (JSON)',
    extracted_lineages TEXT DEFAULT NULL COMMENT 'Extracted lineage relations (JSON)',
    llm_raw_response TEXT DEFAULT NULL COMMENT 'Raw LLM response for debugging',
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_database_name (database_name),
    INDEX idx_table_name (table_name),
    INDEX idx_subject_classification (subject_classification),
    INDEX idx_confidence_score (confidence_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Analysis Results';

-- Insert default model configuration
INSERT INTO local_model_config (model_name, model_type, base_url, max_tokens, temperature, enabled, is_default)
VALUES ('Default Ollama', 'OLLAMA', 'http://host.docker.internal:11434/api/generate', 4096, 0.7, TRUE, TRUE);

-- Grant permissions
GRANT ALL PRIVILEGES ON edams_analysis.* TO 'root'@'%';
FLUSH PRIVILEGES;
