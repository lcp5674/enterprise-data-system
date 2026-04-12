-- 水印记录表
CREATE TABLE IF NOT EXISTS watermark_record (
    id           BIGSERIAL PRIMARY KEY,
    asset_id     BIGINT NOT NULL,
    watermark_id VARCHAR(64) NOT NULL UNIQUE,
    type         VARCHAR(50) NOT NULL,
    content      TEXT,
    algorithm    VARCHAR(50),
    strength     INTEGER DEFAULT 50,
    status       VARCHAR(20) DEFAULT 'ACTIVE',
    created_by   BIGINT,
    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_watermark_asset_id ON watermark_record(asset_id);
CREATE INDEX idx_watermark_id ON watermark_record(watermark_id);
CREATE INDEX idx_watermark_status ON watermark_record(status);

-- 溯源记录表
CREATE TABLE IF NOT EXISTS trace_record (
    id           BIGSERIAL PRIMARY KEY,
    watermark_id VARCHAR(64) NOT NULL,
    asset_id     BIGINT,
    event_type   VARCHAR(50),
    user_id      BIGINT,
    ip_address   VARCHAR(64),
    event_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    extra_info   JSONB
);

CREATE INDEX idx_trace_watermark_id ON trace_record(watermark_id);
CREATE INDEX idx_trace_event_time ON trace_record(event_time);
