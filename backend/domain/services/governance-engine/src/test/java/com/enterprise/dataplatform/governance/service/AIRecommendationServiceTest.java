package com.enterprise.dataplatform.governance.service;

import com.enterprise.dataplatform.governance.domain.entity.GovernancePolicy;
import com.enterprise.dataplatform.governance.domain.entity.GovernanceTask;
import com.enterprise.dataplatform.governance.domain.entity.TaskExecution;
import com.enterprise.dataplatform.governance.dto.request.AIRecommendationRequest;
import com.enterprise.dataplatform.governance.dto.response.AIRecommendationResponse;
import com.enterprise.dataplatform.governance.repository.AIRecommendationRepository;
import com.enterprise.dataplatform.governance.repository.GovernancePolicyRepository;
import com.enterprise.dataplatform.governance.repository.GovernanceTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AIRecommendationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI推荐服务测试")
class AIRecommendationServiceTest {

    @Mock
    private AIRecommendationRepository recommendationRepository;

    @Mock
    private GovernancePolicyRepository policyRepository;

    @Mock
    private GovernanceTaskRepository taskRepository;

    @InjectMocks
    private AIRecommendationService aiRecommendationService;

    private GovernancePolicy testPolicy;
    private GovernanceTask testTask;

    @BeforeEach
    void setUp() {
        testPolicy = GovernancePolicy.builder()
                .id(1L)
                .policyCode("POL-001")
                .policyName("测试策略")
                .policyType("QUALITY")
                .status("ACTIVE")
                .build();

        testTask = GovernanceTask.builder()
                .id(1L)
                .taskCode("TASK-001")
                .taskName("测试任务")
                .taskType("ORCHESTRATION")
                .taskStatus("PENDING")
                .policy(testPolicy)
                .upstreamTasks(new ArrayList<>())
                .downstreamTasks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("生成质量改进建议 - 正常场景")
    void testGenerateQualityRecommendation() {
        // Given
        AIRecommendationRequest request = AIRecommendationRequest.builder()
                .assetId("ASSET-001")
                .qualityScore(65.0)
                .issues(List.of("缺失主键", "数据重复"))
                .build();

        // When
        AIRecommendationResponse response = aiRecommendationService.generateQualityRecommendation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAssetId()).isEqualTo("ASSET-001");
        assertThat(response.getRecommendations()).isNotEmpty();
    }

    @Test
    @DisplayName("生成数据分类建议 - 正常场景")
    void testGenerateClassificationRecommendation() {
        // Given
        AIRecommendationRequest request = AIRecommendationRequest.builder()
                .assetId("ASSET-002")
                .description("用户信息表，包含姓名、手机号、身份证号")
                .sampleData("示例数据")
                .build();

        // When
        AIRecommendationResponse response = aiRecommendationService.generateClassificationRecommendation(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAssetId()).isEqualTo("ASSET-002");
    }

    @Test
    @DisplayName("生成血缘补充建议")
    void testGenerateLineageSuggestion() {
        // Given
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AIRecommendationResponse response = aiRecommendationService.generateLineageSuggestion("ASSET-001");

        // Then
        assertThat(response).isNotNull();
        verify(recommendationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("分析数据质量问题根因")
    void testAnalyzeRootCause() {
        // Given
        List<String> issues = List.of("数据缺失", "格式不一致", "重复记录");
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AIRecommendationResponse response = aiRecommendationService.analyzeRootCause("ASSET-001", issues);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRootCauseAnalysis()).isNotNull();
    }

    @Test
    @DisplayName("建议治理任务")
    void testSuggestGovernanceTasks() {
        // Given
        when(taskRepository.findByTaskStatus("PENDING")).thenReturn(List.of(testTask));
        when(taskRepository.findByTaskType("ORCHESTRATION")).thenReturn(List.of(testTask));
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AIRecommendationResponse response = aiRecommendationService.suggestGovernanceTasks("ASSET-001");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendedTasks()).isNotNull();
    }

    @Test
    @DisplayName("评估资产敏感等级")
    void testAssessSensitivityLevel() {
        // Given
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AIRecommendationResponse response = aiRecommendationService.assessSensitivityLevel("ASSET-001", "用户信息表");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSensitivityLevel()).isNotNull();
    }

    @Test
    @DisplayName("生成数据资产健康报告")
    void testGenerateHealthReport() {
        // Given
        when(taskRepository.findAll()).thenReturn(List.of(testTask));
        when(recommendationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AIRecommendationResponse response = aiRecommendationService.generateHealthReport();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOverallHealthScore()).isNotNull();
    }

    @Test
    @DisplayName("获取AI推荐历史")
    void testGetRecommendationHistory() {
        // Given
        when(recommendationRepository.findTop50ByOrderByCreatedTimeDesc()).thenReturn(new ArrayList<>());

        // When
        List<AIRecommendationResponse> history = aiRecommendationService.getRecommendationHistory();

        // Then
        assertThat(history).isNotNull();
    }
}
