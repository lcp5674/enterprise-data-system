-- ClickHouse Initialization Script for EDAMS Analytics
-- This script runs on first container start

-- Create database
CREATE DATABASE IF NOT EXISTS edams_analytics;

-- Asset Analytics Table
CREATE TABLE IF NOT EXISTS edams_analytics.asset_analytics (
    id UInt64,
    asset_id String,
    asset_name String,
    asset_type String,
    owner_id String,
    department String,
    access_count UInt32,
    download_count UInt32,
    share_count UInt32,
    comment_count UInt32,
    quality_score Float32,
    value_score Float32,
    freshness_score Float32,
    completeness_score Float32,
    tags Array(String),
    created_at DateTime,
    updated_at DateTime,
    date Date DEFAULT toDate(created_at)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (date, asset_id)
TTL date + INTERVAL 180 DAY
SETTINGS index_granularity = 8192;

-- Quality Trend Table
CREATE TABLE IF NOT EXISTS edams_analytics.quality_trend (
    id UInt64,
    check_time DateTime,
    asset_id String,
    asset_name String,
    check_type String,
    total_checks UInt32,
    passed_checks UInt32,
    failed_checks UInt32,
    pass_rate Float32,
    avg_score Float32,
    min_score Float32,
    max_score Float32,
    avg_completeness Float32,
    avg_freshness Float32,
    avg_accuracy Float32,
    avg_consistency Float32,
    dimension_scores Array(String),
    created_at DateTime DEFAULT now()
) ENGINE = ReplacingMergeTree()
PARTITION BY toYYYYMM(check_time)
ORDER BY (asset_id, check_time)
TTL check_time + INTERVAL 365 DAY
SETTINGS index_granularity = 8192;

-- User Behavior Table
CREATE TABLE IF NOT EXISTS edams_analytics.user_behavior (
    id UInt64,
    user_id String,
    user_name String,
    department String,
    action_type String,
    asset_id String,
    asset_name String,
    asset_type String,
    duration UInt32,
    result_status String,
    session_id String,
    ip_address String,
    user_agent String,
    error_message String,
    metadata String,
    timestamp DateTime DEFAULT now(),
    date Date DEFAULT toDate(timestamp)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (date, user_id, timestamp)
TTL date + INTERVAL 90 DAY
SETTINGS index_granularity = 8192;

-- Data Quality Metrics Table
CREATE TABLE IF NOT EXISTS edams_analytics.data_quality_metrics (
    id UInt64,
    metric_time DateTime,
    metric_category String,
    metric_name String,
    metric_value Float64,
    metric_unit String,
    threshold_value Float64,
    alert_level String,
    dimensions String,
    created_at DateTime DEFAULT now()
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(metric_time)
ORDER BY (metric_category, metric_name, metric_time)
TTL metric_time + INTERVAL 180 DAY
SETTINGS index_granularity = 8192;

-- Asset Catalog Stats Table
CREATE TABLE IF NOT EXISTS edams_analytics.asset_catalog_stats (
    id UInt64,
    stat_time DateTime,
    asset_type String,
    total_count UInt32,
    active_count UInt32,
    archived_count UInt32,
    total_size_bytes UInt64,
    avg_size_bytes Float64,
    total_tags UInt32,
    avg_tags_per_asset Float32,
    created_at DateTime DEFAULT now()
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(stat_time)
ORDER BY (asset_type, stat_time)
TTL stat_time + INTERVAL 365 DAY
SETTINGS index_granularity = 8192;

-- Analytics Summary Table
CREATE TABLE IF NOT EXISTS edams_analytics.analytics_summary (
    id UInt64,
    summary_type String,
    summary_date Date,
    dimension_type String,
    dimension_value String,
    metric_name String,
    metric_value Float64,
    metric_count UInt64,
    min_value Float64,
    max_value Float64,
    avg_value Float64,
    p50_value Float64,
    p90_value Float64,
    p99_value Float64,
    created_at DateTime DEFAULT now()
) ENGINE = SummingMergeTree()
ORDER BY (summary_type, summary_date, dimension_type, dimension_value, metric_name)
TTL summary_date + INTERVAL 365 DAY
SETTINGS index_granularity = 8192;

-- Insert sample data for testing
INSERT INTO edams_analytics.asset_analytics VALUES
(1, 'asset-001', 'Customer Table', 'TABLE', 'user-001', 'Sales', 150, 50, 20, 10, 85.5, 90.0, 80.0, 88.0, ['customer', 'sales'], now(), now(), today()),
(2, 'asset-002', 'Sales Report', 'REPORT', 'user-002', 'Analytics', 200, 80, 30, 15, 92.0, 95.0, 85.0, 90.0, ['report', 'sales'], now(), now(), today()),
(3, 'asset-003', 'Product Catalog', 'DATABASE', 'user-003', 'Inventory', 100, 30, 10, 5, 78.0, 82.0, 75.0, 80.0, ['product', 'catalog'], now(), now(), today());

INSERT INTO edams_analytics.quality_trend VALUES
(1, now(), 'asset-001', 'Customer Table', 'completeness', 100, 95, 5, 95.0, 88.0, 75.0, 95.0, 90.0, 85.0, 88.0, 85.0, '[]', now()),
(2, now(), 'asset-002', 'Sales Report', 'accuracy', 80, 76, 4, 95.0, 92.0, 85.0, 98.0, 90.0, 92.0, 93.0, 92.0, '[]', now()),
(3, now(), 'asset-003', 'Product Catalog', 'freshness', 60, 54, 6, 90.0, 85.0, 70.0, 95.0, 88.0, 82.0, 85.0, 80.0, '[]', now());

INSERT INTO edams_analytics.user_behavior VALUES
(1, 'user-001', 'John Doe', 'Engineering', 'VIEW', 'asset-001', 'Customer Table', 'TABLE', 30, 'SUCCESS', 'session-001', '192.168.1.1', 'Chrome', '', '', now(), today()),
(2, 'user-001', 'John Doe', 'Engineering', 'DOWNLOAD', 'asset-002', 'Sales Report', 'REPORT', 60, 'SUCCESS', 'session-001', '192.168.1.1', 'Chrome', '', '', now(), today()),
(3, 'user-002', 'Jane Smith', 'Marketing', 'VIEW', 'asset-002', 'Sales Report', 'REPORT', 45, 'SUCCESS', 'session-002', '192.168.1.2', 'Firefox', '', '', now(), today()),
(4, 'user-002', 'Jane Smith', 'Marketing', 'SHARE', 'asset-002', 'Sales Report', 'REPORT', 15, 'SUCCESS', 'session-002', '192.168.1.2', 'Firefox', '', '', now(), today()),
(5, 'user-003', 'Bob Wilson', 'Sales', 'VIEW', 'asset-001', 'Customer Table', 'TABLE', 25, 'SUCCESS', 'session-003', '192.168.1.3', 'Safari', '', '', now(), today());

-- Create materialized views for aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS edams_analytics.asset_heatmap_mv
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (asset_type, asset_id, hour)
AS SELECT
    asset_type,
    asset_id,
    asset_name,
    toStartOfHour(created_at) AS hour,
    count() AS access_count,
    sum(download_count) AS total_downloads,
    sum(share_count) AS total_shares,
    avg(quality_score) AS avg_quality_score,
    avg(value_score) AS avg_value_score
FROM edams_analytics.asset_analytics
GROUP BY asset_type, asset_id, asset_name, hour;

CREATE MATERIALIZED VIEW IF NOT EXISTS edams_analytics.quality_trend_hourly_mv
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(check_hour)
ORDER BY (check_type, check_hour)
AS SELECT
    check_type,
    toStartOfHour(check_time) AS check_hour,
    count() AS total_assets,
    sum(total_checks) AS total_checks,
    sum(passed_checks) AS passed_checks,
    sum(failed_checks) AS failed_checks,
    avg(pass_rate) AS avg_pass_rate,
    avg(avg_score) AS avg_score
FROM edams_analytics.quality_trend
GROUP BY check_type, check_hour;

CREATE MATERIALIZED VIEW IF NOT EXISTS edams_analytics.user_behavior_hourly_mv
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(hour)
ORDER BY (user_id, action_type, hour)
AS SELECT
    user_id,
    user_name,
    department,
    action_type,
    toStartOfHour(timestamp) AS hour,
    count() AS action_count,
    sum(duration) AS total_duration,
    avg(duration) AS avg_duration
FROM edams_analytics.user_behavior
GROUP BY user_id, user_name, department, action_type, hour;
