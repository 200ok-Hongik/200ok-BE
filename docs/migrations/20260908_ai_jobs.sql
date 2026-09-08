-- Optional RabbitMQ scan worker persistence (local and deployment MySQL).
CREATE TABLE IF NOT EXISTS ai_analysis_jobs (
    id VARCHAR(255) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    filename VARCHAR(255),
    content_type VARCHAR(255),
    image LONGBLOB,
    result_json LONGTEXT,
    error_message VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_ai_jobs_pending (status, created_at)
);
