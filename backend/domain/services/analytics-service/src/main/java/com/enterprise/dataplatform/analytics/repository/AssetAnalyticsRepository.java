package com.enterprise.dataplatform.analytics.repository;

import com.enterprise.dataplatform.analytics.entity.AssetAnalytics;
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
 * Repository for Asset Analytics data access via ClickHouse
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AssetAnalyticsRepository {

    private final JdbcTemplate clickHouseJdbcTemplate;

    private static final String INSERT_SQL = """
            INSERT INTO edams_analytics.asset_analytics (
                id, asset_id, asset_name, asset_type, owner_id, department,
                access_count, download_count, share_count, comment_count,
                quality_score, value_score, freshness_score, completeness_score,
                tags, created_at, updated_at, date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ASSET_ID_SQL = """
            SELECT id, asset_id, asset_name, asset_type, owner_id, department,
                   access_count, download_count, share_count, comment_count,
                   quality_score, value_score, freshness_score, completeness_score,
                   tags, created_at, updated_at, date
            FROM edams_analytics.asset_analytics
            WHERE asset_id = ?
            ORDER BY created_at DESC
            LIMIT 1
            """;

    private static final String FIND_TOP_ACCESSED_SQL = """
            SELECT asset_id, asset_name, asset_type, sum(access_count) as total_access,
                   sum(download_count) as total_downloads, sum(share_count) as total_shares,
                   avg(quality_score) as avg_quality, avg(value_score) as avg_value
            FROM edams_analytics.asset_analytics
            WHERE date >= ? AND date <= ?
            GROUP BY asset_id, asset_name, asset_type
            ORDER BY total_access DESC
            LIMIT ?
            """;

    private static final String FIND_BY_TIME_RANGE_SQL = """
            SELECT id, asset_id, asset_name, asset_type, owner_id, department,
                   access_count, download_count, share_count, comment_count,
                   quality_score, value_score, freshness_score, completeness_score,
                   tags, created_at, updated_at, date
            FROM edams_analytics.asset_analytics
            WHERE date >= ? AND date <= ?
            ORDER BY created_at DESC
            """;

    private static final String GET_HEATMAP_DATA_SQL = """
            SELECT asset_id, asset_name, asset_type,
                   toStartOfHour(created_at) as hour,
                   sum(access_count) as access_count,
                   sum(download_count) as downloads,
                   sum(share_count) as shares,
                   avg(quality_score) as avg_quality,
                   avg(value_score) as avg_value
            FROM edams_analytics.asset_analytics
            WHERE date >= ? AND date <= ?
            GROUP BY asset_id, asset_name, asset_type, hour
            ORDER BY hour DESC
            """;

    private static final RowMapper<AssetAnalytics> ROW_MAPPER = (rs, rowNum) -> AssetAnalytics.builder()
            .id(rs.getLong("id"))
            .assetId(rs.getString("asset_id"))
            .assetName(rs.getString("asset_name"))
            .assetType(rs.getString("asset_type"))
            .ownerId(rs.getString("owner_id"))
            .department(rs.getString("department"))
            .accessCount(rs.getInt("access_count"))
            .downloadCount(rs.getInt("download_count"))
            .shareCount(rs.getInt("share_count"))
            .commentCount(rs.getInt("comment_count"))
            .qualityScore(rs.getFloat("quality_score"))
            .valueScore(rs.getFloat("value_score"))
            .freshnessScore(rs.getFloat("freshness_score"))
            .completenessScore(rs.getFloat("completeness_score"))
            .createdAt(rs.getTimestamp("created_at") != null ? 
                rs.getTimestamp("created_at").toLocalDateTime() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? 
                rs.getTimestamp("updated_at").toLocalDateTime() : null)
            .date(rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null)
            .build();

    /**
     * Insert or update asset analytics record
     */
    public void save(AssetAnalytics analytics) {
        try {
            clickHouseJdbcTemplate.update(INSERT_SQL,
                    analytics.getId(),
                    analytics.getAssetId(),
                    analytics.getAssetName(),
                    analytics.getAssetType(),
                    analytics.getOwnerId(),
                    analytics.getDepartment(),
                    analytics.getAccessCount(),
                    analytics.getDownloadCount(),
                    analytics.getShareCount(),
                    analytics.getCommentCount(),
                    analytics.getQualityScore(),
                    analytics.getValueScore(),
                    analytics.getFreshnessScore(),
                    analytics.getCompletenessScore(),
                    String.join(",", analytics.getTags() != null ? analytics.getTags() : List.of()),
                    analytics.getCreatedAt() != null ? analytics.getCreatedAt() : LocalDateTime.now(),
                    analytics.getUpdatedAt() != null ? analytics.getUpdatedAt() : LocalDateTime.now(),
                    analytics.getDate() != null ? analytics.getDate() : LocalDate.now()
            );
            log.debug("Saved asset analytics for asset_id: {}", analytics.getAssetId());
        } catch (Exception e) {
            log.error("Failed to save asset analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save asset analytics", e);
        }
    }

    /**
     * Batch insert asset analytics records
     */
    public void batchSave(List<AssetAnalytics> analyticsList) {
        try {
            for (AssetAnalytics analytics : analyticsList) {
                save(analytics);
            }
            log.info("Batch saved {} asset analytics records", analyticsList.size());
        } catch (Exception e) {
            log.error("Failed to batch save asset analytics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch save asset analytics", e);
        }
    }

    /**
     * Find asset analytics by asset ID
     */
    public Optional<AssetAnalytics> findByAssetId(String assetId) {
        try {
            List<AssetAnalytics> results = clickHouseJdbcTemplate.query(FIND_BY_ASSET_ID_SQL, ROW_MAPPER, assetId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.error("Failed to find asset analytics by assetId: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Find top accessed assets within time range
     */
    public List<AssetAnalytics> findTopAccessed(LocalDate startDate, LocalDate endDate, int limit) {
        try {
            return clickHouseJdbcTemplate.query(FIND_TOP_ACCESSED_SQL, ROW_MAPPER, startDate, endDate, limit);
        } catch (Exception e) {
            log.error("Failed to find top accessed assets: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Find asset analytics within time range
     */
    public List<AssetAnalytics> findByTimeRange(LocalDate startDate, LocalDate endDate) {
        try {
            return clickHouseJdbcTemplate.query(FIND_BY_TIME_RANGE_SQL, ROW_MAPPER, startDate, endDate);
        } catch (Exception e) {
            log.error("Failed to find asset analytics by time range: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get heatmap data for visualization
     */
    public List<HeatmapData> getHeatmapData(LocalDate startDate, LocalDate endDate) {
        try {
            return clickHouseJdbcTemplate.query(GET_HEATMAP_DATA_SQL,
                    (rs, rowNum) -> new HeatmapData(
                            rs.getString("asset_id"),
                            rs.getString("asset_name"),
                            rs.getString("asset_type"),
                            rs.getTimestamp("hour").toLocalDateTime(),
                            rs.getInt("access_count"),
                            rs.getInt("downloads"),
                            rs.getInt("shares"),
                            rs.getFloat("avg_quality"),
                            rs.getFloat("avg_value")
                    ), startDate, endDate);
        } catch (Exception e) {
            log.error("Failed to get heatmap data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Heatmap data record
     */
    public record HeatmapData(
            String assetId,
            String assetName,
            String assetType,
            LocalDateTime hour,
            int accessCount,
            int downloads,
            int shares,
            float avgQuality,
            float avgValue
    ) {}
}
