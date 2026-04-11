package com.enterprise.dataplatform.analytics.service;

import com.enterprise.dataplatform.analytics.config.AnalyticsProperties;
import com.enterprise.dataplatform.analytics.dto.UserBehaviorResponse;
import com.enterprise.dataplatform.analytics.entity.UserBehavior;
import com.enterprise.dataplatform.analytics.repository.UserBehaviorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for User Behavior analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBehaviorService {

    private final UserBehaviorRepository repository;
    private final AnalyticsProperties properties;

    /**
     * Get user behavior analysis
     */
    public UserBehaviorResponse getUserBehavior(String userId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        Optional<UserBehaviorRepository.UserSummary> summaryOpt = 
                repository.getUserSummary(userId, startTime, endTime);

        if (summaryOpt.isEmpty()) {
            return buildEmptyBehaviorResponse(userId, startTime, endTime);
        }

        UserBehaviorRepository.UserSummary summary = summaryOpt.get();
        List<UserBehaviorRepository.ActionStats> actionStats = 
                repository.getUserActionStats(startTime, endTime);

        // Filter for specific user
        List<UserBehaviorRepository.ActionStats> userStats = actionStats.stream()
                .filter(s -> userId.equals(s.userId()))
                .collect(Collectors.toList());

        // Get top actions
        List<UserBehaviorResponse.ActionSummary> topActions = userStats.stream()
                .sorted(Comparator.comparing(UserBehaviorRepository.ActionStats::actionCount).reversed())
                .map(s -> UserBehaviorResponse.ActionSummary.builder()
                        .actionType(s.actionType())
                        .count(s.actionCount())
                        .totalDuration(s.totalDuration())
                        .avgDuration((long) s.avgDuration())
                        .successRate(calculateSuccessRate(s.actionType(), userId, startTime, endTime))
                        .build())
                .collect(Collectors.toList());

        // Get top assets
        List<UserBehavior> behaviors = repository.findByUserAndTimeRange(userId, startTime, endTime);
        Map<String, Long> assetAccessCount = behaviors.stream()
                .filter(b -> userId.equals(b.getUserId()))
                .collect(Collectors.groupingBy(
                        UserBehavior::getAssetId,
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Long::longValue
                        )
                ));

        List<UserBehaviorResponse.AssetAccess> topAssets = assetAccessCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    UserBehavior firstBehavior = behaviors.stream()
                            .filter(b -> e.getKey().equals(b.getAssetId()))
                            .findFirst()
                            .orElse(null);
                    return UserBehaviorResponse.AssetAccess.builder()
                            .assetId(e.getKey())
                            .assetName(firstBehavior != null ? firstBehavior.getAssetName() : "Unknown")
                            .assetType(firstBehavior != null ? firstBehavior.getAssetType() : "Unknown")
                            .accessCount(e.getValue())
                            .totalDuration(behaviors.stream()
                                    .filter(b -> e.getKey().equals(b.getAssetId()))
                                    .mapToLong(b -> b.getDuration() != null ? b.getDuration() : 0)
                                    .sum())
                            .build();
                })
                .collect(Collectors.toList());

        // Build action type counts
        Map<String, Long> actionTypeCounts = userStats.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorRepository.ActionStats::actionType,
                        Collectors.summingLong(UserBehaviorRepository.ActionStats::actionCount)
                ));

        // Calculate behavior insights
        Map<String, Object> insights = calculateBehaviorInsights(userStats, summary);

        return UserBehaviorResponse.builder()
                .userId(userId)
                .userName(userStats.isEmpty() ? "Unknown" : userStats.get(0).userName())
                .department(userStats.isEmpty() ? "Unknown" : userStats.get(0).department())
                .startTime(startTime)
                .endTime(endTime)
                .totalActions(summary.totalActions())
                .uniqueAssetsAccessed(summary.uniqueAssets())
                .activeMinutes(summary.totalDuration() / 60)
                .actionTypeCounts(actionTypeCounts)
                .topActions(topActions)
                .topAssets(topAssets)
                .behaviorInsights(insights)
                .build();
    }

    /**
     * Get all users behavior summary
     */
    public List<UserBehaviorResponse> getAllUsersBehavior(int days, int limit) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        List<UserBehaviorRepository.ActionStats> allStats = 
                repository.getUserActionStats(startTime, endTime);

        // Group by user
        Map<String, List<UserBehaviorRepository.ActionStats>> byUser = allStats.stream()
                .collect(Collectors.groupingBy(UserBehaviorRepository.ActionStats::userId));

        List<UserBehaviorResponse> responses = new ArrayList<>();
        int count = 0;

        for (Map.Entry<String, List<UserBehaviorRepository.ActionStats>> entry : byUser.entrySet()) {
            if (count >= limit) break;

            List<UserBehaviorRepository.ActionStats> userStats = entry.getValue();
            UserBehaviorRepository.ActionStats firstStat = userStats.get(0);

            long totalActions = userStats.stream()
                    .mapToLong(UserBehaviorRepository.ActionStats::actionCount)
                    .sum();
            long totalDuration = userStats.stream()
                    .mapToLong(UserBehaviorRepository.ActionStats::totalDuration)
                    .sum();
            long uniqueAssets = userStats.stream()
                    .mapToLong(UserBehaviorRepository.ActionStats::uniqueAssets)
                    .sum();

            Map<String, Long> actionCounts = userStats.stream()
                    .collect(Collectors.groupingBy(
                            UserBehaviorRepository.ActionStats::actionType,
                            Collectors.summingLong(UserBehaviorRepository.ActionStats::actionCount)
                    ));

            responses.add(UserBehaviorResponse.builder()
                    .userId(entry.getKey())
                    .userName(firstStat.userName())
                    .department(firstStat.department())
                    .startTime(startTime)
                    .endTime(endTime)
                    .totalActions(totalActions)
                    .uniqueAssetsAccessed(uniqueAssets)
                    .activeMinutes(totalDuration / 60)
                    .actionTypeCounts(actionCounts)
                    .build());

            count++;
        }

        // Sort by total actions descending
        responses.sort((a, b) -> Long.compare(b.getTotalActions(), a.getTotalActions()));

        return responses;
    }

    /**
     * Save user behavior record
     */
    public void saveUserBehavior(UserBehavior behavior) {
        if (behavior.getTimestamp() == null) {
            behavior.setTimestamp(LocalDateTime.now());
        }
        if (behavior.getDate() == null) {
            behavior.setDate(LocalDate.now());
        }
        repository.save(behavior);
        log.debug("Saved user behavior: user={}, action={}", behavior.getUserId(), behavior.getActionType());
    }

    /**
     * Batch save user behaviors
     */
    public void batchSaveUserBehaviors(List<UserBehavior> behaviors) {
        repository.batchSave(behaviors);
        log.info("Batch saved {} user behavior records", behaviors.size());
    }

    /**
     * Get session behaviors
     */
    public List<UserBehavior> getSessionBehaviors(String sessionId) {
        return repository.findBySession(sessionId);
    }

    /**
     * Get behavior summary statistics
     */
    public Map<String, Object> getBehaviorSummary(LocalDateTime startTime, LocalDateTime endTime) {
        List<UserBehaviorRepository.ActionStats> allStats = 
                repository.getUserActionStats(startTime, endTime);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", allStats.size());
        summary.put("totalActions", allStats.stream()
                .mapToLong(UserBehaviorRepository.ActionStats::actionCount)
                .sum());
        summary.put("totalDuration", allStats.stream()
                .mapToLong(UserBehaviorRepository.ActionStats::totalDuration)
                .sum());
        summary.put("uniqueUsers", allStats.stream()
                .map(UserBehaviorRepository.ActionStats::userId)
                .distinct()
                .count());
        summary.put("avgDurationPerAction", allStats.stream()
                .mapToDouble(UserBehaviorRepository.ActionStats::avgDuration)
                .average().orElse(0));

        // Group by action type
        Map<String, Long> byActionType = allStats.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorRepository.ActionStats::actionType,
                        Collectors.summingLong(UserBehaviorRepository.ActionStats::actionCount)
                ));
        summary.put("byActionType", byActionType);

        // Group by department
        Map<String, Long> byDepartment = allStats.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorRepository.ActionStats::department,
                        Collectors.summingLong(UserBehaviorRepository.ActionStats::actionCount)
                ));
        summary.put("byDepartment", byDepartment);

        summary.put("timeRange", Map.of("start", startTime, "end", endTime));

        return summary;
    }

    private double calculateSuccessRate(String actionType, String userId, 
                                         LocalDateTime startTime, LocalDateTime endTime) {
        List<UserBehavior> behaviors = repository.findByUserAndTimeRange(userId, startTime, endTime);
        List<UserBehavior> typeBehaviors = behaviors.stream()
                .filter(b -> actionType.equals(b.getActionType()))
                .toList();

        if (typeBehaviors.isEmpty()) return 0.0;

        long successCount = typeBehaviors.stream()
                .filter(b -> "SUCCESS".equalsIgnoreCase(b.getResultStatus()))
                .count();

        return (double) successCount / typeBehaviors.size();
    }

    private Map<String, Object> calculateBehaviorInsights(
            List<UserBehaviorRepository.ActionStats> stats,
            UserBehaviorRepository.UserSummary summary) {
        Map<String, Object> insights = new HashMap<>();

        // Activity level
        String activityLevel;
        if (summary.totalActions() > 1000) {
            activityLevel = "HIGH";
        } else if (summary.totalActions() > 100) {
            activityLevel = "MEDIUM";
        } else {
            activityLevel = "LOW";
        }
        insights.put("activityLevel", activityLevel);

        // Most frequent action
        String topAction = stats.stream()
                .max(Comparator.comparing(UserBehaviorRepository.ActionStats::actionCount))
                .map(UserBehaviorRepository.ActionStats::actionType)
                .orElse("NONE");
        insights.put("topAction", topAction);

        // Average session duration (estimated)
        double avgSessionDuration = summary.avgDuration() * 10; // Rough estimate
        insights.put("estimatedAvgSessionMinutes", avgSessionDuration / 60);

        // Diversity score (ratio of unique assets to total actions)
        double diversityScore = summary.totalActions() > 0 ?
                (double) summary.uniqueAssets() / summary.totalActions() : 0;
        insights.put("assetDiversityScore", diversityScore);

        return insights;
    }

    private UserBehaviorResponse buildEmptyBehaviorResponse(String userId, 
                                                              LocalDateTime startTime, 
                                                              LocalDateTime endTime) {
        return UserBehaviorResponse.builder()
                .userId(userId)
                .userName("Unknown")
                .department("Unknown")
                .startTime(startTime)
                .endTime(endTime)
                .totalActions(0L)
                .uniqueAssetsAccessed(0L)
                .activeMinutes(0L)
                .actionTypeCounts(Map.of())
                .topActions(List.of())
                .topAssets(List.of())
                .behaviorInsights(Map.of())
                .build();
    }
}
