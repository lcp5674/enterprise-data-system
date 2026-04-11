package com.enterprise.dataplatform.analytics.service;

import com.enterprise.dataplatform.analytics.config.AnalyticsProperties;
import com.enterprise.dataplatform.analytics.dto.UserBehaviorResponse;
import com.enterprise.dataplatform.analytics.entity.UserBehavior;
import com.enterprise.dataplatform.analytics.repository.UserBehaviorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserBehaviorService
 */
@ExtendWith(MockitoExtension.class)
class UserBehaviorServiceTest {

    @Mock
    private UserBehaviorRepository repository;

    @Mock
    private AnalyticsProperties properties;

    @Mock
    private AnalyticsProperties.BehaviorConfig behaviorConfig;

    @InjectMocks
    private UserBehaviorService service;

    @BeforeEach
    void setUp() {
        when(properties.getBehavior()).thenReturn(behaviorConfig);
        when(behaviorConfig.getSessionTimeoutMinutes()).thenReturn(30);
        when(behaviorConfig.getMaxEventsPerSession()).thenReturn(1000);
        when(behaviorConfig.getAnalysisWindowDays()).thenReturn(7);
    }

    @Test
    void testGetUserBehavior_EmptyData() {
        String userId = "user-001";
        int days = 7;

        when(repository.getUserSummary(eq(userId), any(), any()))
                .thenReturn(Optional.empty());

        UserBehaviorResponse response = service.getUserBehavior(userId, days);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("Unknown", response.getUserName());
        assertEquals(0L, response.getTotalActions());
    }

    @Test
    void testGetUserBehavior_WithData() {
        String userId = "user-001";
        int days = 7;

        UserBehaviorRepository.UserSummary mockSummary = new UserBehaviorRepository.UserSummary(
                userId, 100L, 20L, 3600L, 36.0
        );

        List<UserBehaviorRepository.ActionStats> mockStats = List.of(
                new UserBehaviorRepository.ActionStats(
                        userId, "John Doe", "Engineering",
                        "VIEW", 50L, 1800L, 36.0, 10L
                ),
                new UserBehaviorRepository.ActionStats(
                        userId, "John Doe", "Engineering",
                        "DOWNLOAD", 30L, 1200L, 40.0, 8L
                )
        );

        when(repository.getUserSummary(eq(userId), any(), any()))
                .thenReturn(Optional.of(mockSummary));
        when(repository.getUserActionStats(any(), any()))
                .thenReturn(mockStats);
        when(repository.findByUserAndTimeRange(eq(userId), any(), any()))
                .thenReturn(new ArrayList<>());

        UserBehaviorResponse response = service.getUserBehavior(userId, days);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("John Doe", response.getUserName());
        assertEquals("Engineering", response.getDepartment());
        assertEquals(100L, response.getTotalActions());
        assertEquals(20L, response.getUniqueAssetsAccessed());
        assertEquals(60L, response.getActiveMinutes());
        assertEquals(2, response.getTopActions().size());
    }

    @Test
    void testSaveUserBehavior() {
        UserBehavior behavior = UserBehavior.builder()
                .id(1L)
                .userId("user-001")
                .userName("John Doe")
                .actionType("VIEW")
                .assetId("asset-001")
                .duration(30)
                .resultStatus("SUCCESS")
                .build();

        service.saveUserBehavior(behavior);

        verify(repository, times(1)).save(behavior);
        assertNotNull(behavior.getTimestamp());
        assertNotNull(behavior.getDate());
    }

    @Test
    void testGetSessionBehaviors() {
        String sessionId = "session-001";

        List<UserBehavior> mockBehaviors = List.of(
                UserBehavior.builder()
                        .sessionId(sessionId)
                        .userId("user-001")
                        .actionType("VIEW")
                        .build(),
                UserBehavior.builder()
                        .sessionId(sessionId)
                        .userId("user-001")
                        .actionType("DOWNLOAD")
                        .build()
        );

        when(repository.findBySession(sessionId)).thenReturn(mockBehaviors);

        List<UserBehavior> behaviors = service.getSessionBehaviors(sessionId);

        assertNotNull(behaviors);
        assertEquals(2, behaviors.size());
        assertEquals(sessionId, behaviors.get(0).getSessionId());
    }

    @Test
    void testGetAllUsersBehavior() {
        int days = 7;
        int limit = 10;

        List<UserBehaviorRepository.ActionStats> mockStats = List.of(
                new UserBehaviorRepository.ActionStats(
                        "user-001", "John Doe", "Engineering",
                        "VIEW", 100L, 3600L, 36.0, 20L
                ),
                new UserBehaviorRepository.ActionStats(
                        "user-002", "Jane Smith", "Marketing",
                        "VIEW", 80L, 2400L, 30.0, 15L
                ),
                new UserBehaviorRepository.ActionStats(
                        "user-001", "John Doe", "Engineering",
                        "DOWNLOAD", 50L, 2000L, 40.0, 10L
                )
        );

        when(repository.getUserActionStats(any(), any())).thenReturn(mockStats);

        List<UserBehaviorResponse> responses = service.getAllUsersBehavior(days, limit);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        // user-001 should be first (150 total actions)
        assertEquals("user-001", responses.get(0).getUserId());
        assertEquals(150L, responses.get(0).getTotalActions());
    }

    @Test
    void testGetBehaviorSummary() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<UserBehaviorRepository.ActionStats> mockStats = List.of(
                new UserBehaviorRepository.ActionStats(
                        "user-001", "John Doe", "Engineering",
                        "VIEW", 100L, 3600L, 36.0, 20L
                ),
                new UserBehaviorRepository.ActionStats(
                        "user-002", "Jane Smith", "Marketing",
                        "DOWNLOAD", 50L, 2000L, 40.0, 10L
                )
        );

        when(repository.getUserActionStats(startTime, endTime)).thenReturn(mockStats);

        var summary = service.getBehaviorSummary(startTime, endTime);

        assertNotNull(summary);
        assertEquals(2, summary.get("totalRecords"));
        assertEquals(150L, summary.get("totalActions"));
        assertEquals(5600L, summary.get("totalDuration"));
        assertEquals(2L, summary.get("uniqueUsers"));
    }

    @Test
    void testBatchSaveUserBehaviors() {
        List<UserBehavior> behaviors = List.of(
                UserBehavior.builder()
                        .userId("user-001")
                        .actionType("VIEW")
                        .build(),
                UserBehavior.builder()
                        .userId("user-002")
                        .actionType("DOWNLOAD")
                        .build()
        );

        service.batchSaveUserBehaviors(behaviors);

        verify(repository, times(1)).batchSave(behaviors);
    }
}
