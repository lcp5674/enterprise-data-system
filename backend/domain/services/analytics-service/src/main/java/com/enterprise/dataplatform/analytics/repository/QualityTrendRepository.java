package com.enterprise.dataplatform.analytics.repository;

import com.enterprise.dataplatform.analytics.entity.QualityTrend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Quality Trend data access via ClickHouse
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class QualityTrendRepository {

    private final JdbcTemplate clickHouseJdbcTemplate;

    private static final String INSERT_SQL = """
            INSERT INTO edams_analytics.quality_trend (
                id, check_time, asset_id, asset_name, check_type,
                total_checks, passed_checks, failed_checks,
                pass_rate, avg_score, min_score, max_score,
                avg_completeness, avg_freshness, avg_accuracy, avg_consistency,
                dimension_scores, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ASSET_AND_TIME_SQL = """
            SELECT id, check_time, asset_id, asset_name, check_type,
                   total_checks, passed_checks, failed_checks,
                   pass_rate, avg_score, min_score, max_score,
                   avg_completeness, avg_freshness, avg_accuracy, avg_consistency,
                   dimension_scores, created_at
            FROM edams_analytics.quality_trend
            WHERE asset_id = ? AND check_time >= ? AND check_time <= ?
            ORDER BY check_time DESC
            """;

    private static final String FIND_BY_TIME_RANGE_SQL = """
            SELECT id, check_time, asset_id, asset_name, check_type,
                   total_checks, passed_checks, failed_checks,
                   pass_rate, avg_score, min_score, max_score,
                   avg_completeness, avg_freshness, avg_accuracy, avg_consistency,
                   dimension_scores, created_at
            FROM edams_analytics.quality_trend
            WHERE check_time >= ? AND check_time <= ?
            ORDER BY check_time DESC
            """;

    private static final String GET_TREND_AGGREGATION_SQL = """
            SELECT check_type,
                   toStartOfHour(check_time) as hour,
                   count() as total_assets,
                   sum(total_checks) as total_checks,
                   sum(passed_checks) as passed_checks,
                   sum(failed_checks) as failed_checks,
                   avg(pass_rate) as avg_pass_rate,
                   avg(avg_score) as avg_score,
                   min(min_score) as min_score,
                   max(max_score) as max_score
            FROM edams_analytics.quality_trend
            WHERE check_time >= ? AND check_time <= ?
            GROUP BY check_type, hour
            ORDER BY hour DESC
            """;

    private static final String GET_LATEST_TREND_SQL = """
            SELECT id, check_time, asset_id, asset_name, check_type,
                   total_checks, passed_checks, failed_checks,
                   pass_rate, avg_score, min_score, max_score,
                   avg_completeness, avg_freshness, avg_accuracy, avg_consistency,
                   dimension_scores, created_at
            FROM edams_analytics.quality_trend
            WHERE check_type = ?
            ORDER BY check_time DESC
            LIMIT 1
            """;

    private static final RowMapper<QualityTrend> ROW_MAPPER = (rs, rowNum) -> QualityTrend.builder()
            .id(rs.getLong("id"))
            .checkTime(rs.getTimestamp("check_time") != null ? 
                rs.getTimestamp("check_time").toLocalDateTime() : null)
            .assetId(rs.getString("asset_id"))
            .assetName(rs.getString("asset_name"))
            .checkType(rs.getString("check_type"))
            .totalChecks(rs.getInt("total_checks"))
            .passedChecks(rs.getInt("passed_checks"))
            .failedChecks(rs.getInt("failed_checks"))
            .passRate(rs.getFloat("pass_rate"))
            .avgScore(rs.getFloat("avg_score"))
            .minScore(rs.getFloat("min_score"))
            .maxScore(rs.getFloat("max_score"))
            .avgCompleteness(rs.getFloat("avg_completeness"))
            .avgFreshness(rs.getFloat("avg_freshness"))
            .avgAccuracy(rs.getFloat("avg_accuracy"))
            .avgConsistency(rs.getFloat("avg_consistency"))
            .createdAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null)
            .build();

    /**
     * Save quality trend record
     */
    public void save(QualityTrend trend) {
        try {
            clickHouseJdbcTemplate.update(INSERT_SQL,
                    trend.getId(),
                    trend.getCheckTime() != null ? trend.getCheckTime() : LocalDateTime.now(),
                    trend.getAssetId(),
                    trend.getAssetName(),
                    trend.getCheckType(),
                    trend.getTotalChecks(),
                    trend.getPassedChecks(),
                    trend.getFailedChecks(),
                    trend.getPassRate(),
                    trend.getAvgScore(),
                    trend.getMinScore(),
                    trend.getMaxScore(),
                    trend.getAvgCompleteness(),
                    trend.getAvgFreshness(),
                    trend.getAvgAccuracy(),
                    trend.getAvgConsistency(),
                    trend.getDimensionScores() != null ? String.join(",", trend.getDimensionScores()) : "",
                    trend.getCreatedAt() != null ? trend.getCreatedAt() : LocalDateTime.now()
            );
            log.debug("Saved quality trend for asset_id: {}", trend.getAssetId());
        } catch (Exception e) {
            log.error("Failed to save quality trend: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save quality trend", e);
        }
    }

    /**
     * Batch save quality trend records
     */
    public void batchSave(List<QualityTrend> trends) {
        for (QualityTrend trend : trends) {
            save(trend);
        }
        log.info("Batch saved {} quality trend records", trends.size());
    }

    /**
     * Find quality trends by asset and time range
     */
    public List<QualityTrend> findByAssetAndTimeRange(String assetId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return clickHouseJdbcTemplate.query(FIND_BY_ASSET_AND_TIME_SQL, ROW_MAPPER, assetId, startTime, endTime);
        } catch (Exception e) {
            log.error("Failed to find quality trends: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Find quality trends by time range
     */
    public List<QualityTrend> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return clickHouseJdbcTemplate.query(FIND_BY_TIME_RANGE_SQL, ROW_MAPPER, startTime, endTime);
        } catch (Exception e) {
            log.error("Failed to find quality trends by time range: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get trend aggregation data
     */
    public List<TrendAggregation> getTrendAggregation(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return clickHouseJdbcTemplate.query(GET_TREND_AGGREGATION_SQL,
                    (rs, rowNum) -> new TrendAggregation(
                            rs.getString("check_type"),
                            rs.getTimestamp("hour").toLocalDateTime(),
                            rs.getInt("total_assets"),
                            rs.getInt("total_checks"),
                            rs.getInt("passed_checks"),
                            rs.getInt("failed_checks"),
                            rs.getFloat("avg_pass_rate"),
                            rs.getFloat("avg_score"),
                            rs.getFloat("min_score"),
                            rs.getFloat("max_score")
                    ), startTime, endTime);
        } catch (Exception e) {
            log.error("Failed to get trend aggregation: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get latest trend by check type
     */
    public Optional<QualityTrend> findLatestByCheckType(String checkType) {
        try {
            List<QualityTrend> results = clickHouseJdbcTemplate.query(GET_LATEST_TREND_SQL, ROW_MAPPER, checkType);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.error("Failed to find latest trend: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Trend aggregation data record
     */
    public record TrendAggregation(
            String checkType,
            LocalDateTime hour,
            int totalAssets,
            int totalChecks,
            int passedChecks,
            int failedChecks,
            float avgPassRate,
            float avgScore,
            float minScore,
            float maxScore
    ) {}
}
