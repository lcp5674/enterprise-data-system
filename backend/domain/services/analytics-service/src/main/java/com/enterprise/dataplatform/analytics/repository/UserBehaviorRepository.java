package com.enterprise.dataplatform.analytics.repository;

import com.enterprise.dataplatform.analytics.entity.UserBehavior;
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
 * Repository for User Behavior data access via ClickHouse
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserBehaviorRepository {

    private final JdbcTemplate clickHouseJdbcTemplate;

    private static final String INSERT_SQL = """
            INSERT INTO edams_analytics.user_behavior (
                id, user_id, user_name, department, action_type,
                asset_id, asset_name, asset_type, duration, result_status,
                session_id, ip_address, user_agent, error_message,
                metadata, timestamp, date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_USER_AND_TIME_SQL = """
            SELECT id, user_id, user_name, department, action_type,
                   asset_id, asset_name, asset_type, duration, result_status,
                   session_id, ip_address, user_agent, error_message,
                   metadata, timestamp, date
            FROM edams_analytics.user_behavior
            WHERE user_id = ? AND timestamp >= ? AND timestamp <= ?
            ORDER BY timestamp DESC
            """;

    private static final String FIND_BY_SESSION_SQL = """
            SELECT id, user_id, user_name, department, action_type,
                   asset_id, asset_name, asset_type, duration, result_status,
                   session_id, ip_address, user_agent, error_message,
                   metadata, timestamp, date
            FROM edams_analytics.user_behavior
            WHERE session_id = ?
            ORDER BY timestamp DESC
            """;

    private static final String GET_USER_ACTION_STATS_SQL = """
            SELECT user_id, user_name, department, action_type,
                   count() as action_count,
                   sum(duration) as total_duration,
                   avg(duration) as avg_duration,
                   count(distinct asset_id) as unique_assets
            FROM edams_analytics.user_behavior
            WHERE timestamp >= ? AND timestamp <= ?
            GROUP BY user_id, user_name, department, action_type
            ORDER BY action_count DESC
            """;

    private static final String GET_USER_BEHAVIOR_SUMMARY_SQL = """
            SELECT user_id,
                   count() as total_actions,
                   count(distinct asset_id) as unique_assets,
                   sum(duration) as total_duration,
                   avg(duration) as avg_duration
            FROM edams_analytics.user_behavior
            WHERE user_id = ? AND timestamp >= ? AND timestamp <= ?
            GROUP BY user_id
            """;

    private static final RowMapper<UserBehavior> ROW_MAPPER = (rs, rowNum) -> UserBehavior.builder()
            .id(rs.getLong("id"))
            .userId(rs.getString("user_id"))
            .userName(rs.getString("user_name"))
            .department(rs.getString("department"))
            .actionType(rs.getString("action_type"))
            .assetId(rs.getString("asset_id"))
            .assetName(rs.getString("asset_name"))
            .assetType(rs.getString("asset_type"))
            .duration(rs.getInt("duration"))
            .resultStatus(rs.getString("result_status"))
            .sessionId(rs.getString("session_id"))
            .ipAddress(rs.getString("ip_address"))
            .userAgent(rs.getString("user_agent"))
            .errorMessage(rs.getString("error_message"))
            .metadata(rs.getString("metadata"))
            .timestamp(rs.getTimestamp("timestamp") != null ? 
                rs.getTimestamp("timestamp").toLocalDateTime() : null)
            .date(rs.getDate("date") != null ? rs.getDate("date").toLocalDate() : null)
            .build();

    /**
     * Save user behavior record
     */
    public void save(UserBehavior behavior) {
        try {
            clickHouseJdbcTemplate.update(INSERT_SQL,
                    behavior.getId(),
                    behavior.getUserId(),
                    behavior.getUserName(),
                    behavior.getDepartment(),
                    behavior.getActionType(),
                    behavior.getAssetId(),
                    behavior.getAssetName(),
                    behavior.getAssetType(),
                    behavior.getDuration(),
                    behavior.getResultStatus(),
                    behavior.getSessionId(),
                    behavior.getIpAddress(),
                    behavior.getUserAgent(),
                    behavior.getErrorMessage(),
                    behavior.getMetadata(),
                    behavior.getTimestamp() != null ? behavior.getTimestamp() : LocalDateTime.now(),
                    behavior.getDate() != null ? behavior.getDate() : LocalDate.now()
            );
            log.debug("Saved user behavior for user_id: {}, action: {}", 
                    behavior.getUserId(), behavior.getActionType());
        } catch (Exception e) {
            log.error("Failed to save user behavior: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user behavior", e);
        }
    }

    /**
     * Batch save user behavior records
     */
    public void batchSave(List<UserBehavior> behaviors) {
        for (UserBehavior behavior : behaviors) {
            save(behavior);
        }
        log.info("Batch saved {} user behavior records", behaviors.size());
    }

    /**
     * Find behaviors by user and time range
     */
    public List<UserBehavior> findByUserAndTimeRange(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return clickHouseJdbcTemplate.query(FIND_BY_USER_AND_TIME_SQL, ROW_MAPPER, userId, startTime, endTime);
        } catch (Exception e) {
            log.error("Failed to find user behaviors: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Find behaviors by session
     */
    public List<UserBehavior> findBySession(String sessionId) {
        try {
            return clickHouseJdbcTemplate.query(FIND_BY_SESSION_SQL, ROW_MAPPER, sessionId);
        } catch (Exception e) {
            log.error("Failed to find session behaviors: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get user action statistics
     */
    public List<ActionStats> getUserActionStats(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            return clickHouseJdbcTemplate.query(GET_USER_ACTION_STATS_SQL,
                    (rs, rowNum) -> new ActionStats(
                            rs.getString("user_id"),
                            rs.getString("user_name"),
                            rs.getString("department"),
                            rs.getString("action_type"),
                            rs.getLong("action_count"),
                            rs.getLong("total_duration"),
                            rs.getDouble("avg_duration"),
                            rs.getLong("unique_assets")
                    ), startTime, endTime);
        } catch (Exception e) {
            log.error("Failed to get user action stats: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get user behavior summary
     */
    public Optional<UserSummary> getUserSummary(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<UserSummary> results = clickHouseJdbcTemplate.query(GET_USER_BEHAVIOR_SUMMARY_SQL,
                    (rs, rowNum) -> new UserSummary(
                            rs.getString("user_id"),
                            rs.getLong("total_actions"),
                            rs.getLong("unique_assets"),
                            rs.getLong("total_duration"),
                            rs.getDouble("avg_duration")
                    ), userId, startTime, endTime);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.error("Failed to get user summary: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Action statistics record
     */
    public record ActionStats(
            String userId,
            String userName,
            String department,
            String actionType,
            long actionCount,
            long totalDuration,
            double avgDuration,
            long uniqueAssets
    ) {}

    /**
     * User summary record
     */
    public record UserSummary(
            String userId,
            long totalActions,
            long uniqueAssets,
            long totalDuration,
            double avgDuration
    ) {}
}
