-- Apply once before deploying V1 ingestion. Existing rows remain nullable.
ALTER TABLE scan_results ADD COLUMN ai_raw_response LONGTEXT NULL;
ALTER TABLE ai_scan_results ADD COLUMN object_id VARCHAR(255) NULL;
