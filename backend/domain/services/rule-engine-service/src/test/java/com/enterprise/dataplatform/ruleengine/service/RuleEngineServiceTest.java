package com.enterprise.dataplatform.ruleengine.service;

import com.enterprise.dataplatform.ruleengine.domain.model.*;
import com.enterprise.dataplatform.ruleengine.domain.entity.RuleExecutionLog;
import com.enterprise.dataplatform.ruleengine.repository.RuleExecutionLogRepository;
import com.enterprise.dataplatform.ruleengine.service.impl.RuleEngineServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 规则引擎服务单元测试
 * 测试RuleEngineServiceImpl的核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("规则引擎服务测试")
class RuleEngineServiceTest {

    @Mock
    private KieContainer kieContainer;

    @Mock
    private DroolsConfig droolsConfig;

    @Mock
    private KieSession qualitySession;

    @Mock
    private KieSession complianceSession;

    @Mock
    private KieSession valueSession;

    @Mock
    private KieSession lifecycleSession;

    @Mock
    private KieSession governanceSession;

    @Mock
    private RuleExecutionLogRepository executionLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RuleEngineServiceImpl ruleEngineService;

    private AssetEvaluation testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new AssetEvaluation();
        testAsset.setAssetId("TEST-001");
        testAsset.setAssetName("测试数据资产");
        testAsset.setAssetType("TABLE");
        testAsset.setQualityIssues(2);
        testAsset.setCompleteness(0.95);
        testAsset.setAccuracy(0.90);
        testAsset.setConsistency(0.88);
        testAsset.setTimeliness(0.85);
        testAsset.setUniqueness(0.92);
        testAsset.setDailyAccessCount(100.0);
        testAsset.setDataSizeMb(500.0);
        testAsset.setStandardCode("DS-001");
        testAsset.setOwner("admin");
        testAsset.setBusinessDomain("FINANCE");
        testAsset.setSensitivityLevel("HIGH");
    }

    @Test
    @DisplayName("测试质量评分评估 - 正常流程")
    void testEvaluateQualityScore_Success() throws Exception {
        // Given
        when(qualitySession.insert(any(AssetEvaluation.class))).thenAnswer(invocation -> {
            AssetEvaluation asset = invocation.getArgument(0);
            asset.setQualityScore(85.5);
            asset.setQualityIssues(2);
            asset.setEvaluationSummary("质量评分评估完成");
            asset.setTriggeredRules(Arrays.asList("Rule-001", "Rule-002"));
            return null;
        });
        when(qualitySession.fireAllRules()).thenReturn(2);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        QualityScoreResult result = ruleEngineService.evaluateQualityScore(testAsset);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getAssetId());
        assertEquals(85.5, result.getQualityScore());
        assertEquals("GOOD", result.getQualityLevel());
        assertNotNull(result.getTriggeredRules());
        assertEquals(2, result.getTriggeredRules().size());

        verify(qualitySession, times(1)).insert(any(AssetEvaluation.class));
        verify(qualitySession, times(1)).fireAllRules();
        verify(executionLogRepository, times(1)).save(any(RuleExecutionLog.class));
    }

    @Test
    @DisplayName("测试质量评分评估 - KieSession为null时使用KieContainer")
    void testEvaluateQualityScore_SessionNull_UsesKieContainer() throws Exception {
        // Given - qualitySession为null
        KieSession newSession = mock(KieSession.class);
        when(qualitySession).thenReturn(null);
        when(kieContainer.newKieSession("qualitySession")).thenReturn(newSession);
        when(newSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(newSession.fireAllRules()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 重新初始化service以使用新的mock
        RuleEngineServiceImpl service = new RuleEngineServiceImpl();
        service.setKieContainer(kieContainer);
        service.setQualitySession(null);
        service.setExecutionLogRepository(executionLogRepository);
        service.setObjectMapper(objectMapper);

        // When
        QualityScoreResult result = service.evaluateQualityScore(testAsset);

        // Then
        assertNotNull(result);
        verify(kieContainer, times(1)).newKieSession("qualitySession");
    }

    @Test
    @DisplayName("测试合规检查 - 正常流程")
    void testCheckCompliance_Success() throws Exception {
        // Given
        when(complianceSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(complianceSession.fireAllRules()).thenReturn(3);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ComplianceResult result = ruleEngineService.checkCompliance(testAsset);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getAssetId());
        assertEquals("COMPLIANT", result.getComplianceStatus());
        assertNotNull(result.getTriggeredRules());

        verify(complianceSession, times(1)).insert(any(AssetEvaluation.class));
        verify(complianceSession, times(1)).fireAllRules();
    }

    @Test
    @DisplayName("测试合规检查 - 合规详情为空时不会空指针")
    void testCheckCompliance_NullComplianceDetails_NoNPE() throws Exception {
        // Given
        testAsset.setComplianceDetails(null);
        when(complianceSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(complianceSession.fireAllRules()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ComplianceResult result = ruleEngineService.checkCompliance(testAsset);

        // Then
        assertNotNull(result);
        assertNotNull(result.getViolationDetails());
        // 不应抛出NullPointerException
    }

    @Test
    @DisplayName("测试价值评估 - 正常流程")
    void testEvaluateValue_Success() throws Exception {
        // Given
        when(valueSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(valueSession.fireAllRules()).thenReturn(2);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ValueScoreResult result = ruleEngineService.evaluateValue(testAsset);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getAssetId());
        assertEquals(0, result.getValueScore());
        assertNotNull(result.getValueLevel());

        verify(valueSession, times(1)).insert(any(AssetEvaluation.class));
        verify(valueSession, times(1)).fireAllRules();
    }

    @Test
    @DisplayName("测试生命周期评估 - 正常流程")
    void testEvaluateLifecycle_Success() throws Exception {
        // Given
        when(lifecycleSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(lifecycleSession.fireAllRules()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LifecycleResult result = ruleEngineService.evaluateLifecycle(testAsset);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getAssetId());
        assertNotNull(result.getRecommendedPhase());
        assertNotNull(result.getAction());

        verify(lifecycleSession, times(1)).insert(any(AssetEvaluation.class));
        verify(lifecycleSession, times(1)).fireAllRules();
    }

    @Test
    @DisplayName("测试治理评估 - 正常流程")
    void testEvaluateGovernance_Success() throws Exception {
        // Given
        when(governanceSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(governanceSession.fireAllRules()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GovernanceRuleResult result = ruleEngineService.evaluateGovernance(testAsset);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getAssetId());

        verify(governanceSession, times(1)).insert(any(AssetEvaluation.class));
        verify(governanceSession, times(1)).fireAllRules();
    }

    @Test
    @DisplayName("测试综合评估 - 依次执行所有规则")
    void testEvaluateAll_Success() throws Exception {
        // Given
        when(qualitySession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(qualitySession.fireAllRules()).thenReturn(1);
        when(complianceSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(complianceSession.fireAllRules()).thenReturn(1);
        when(valueSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(valueSession.fireAllRules()).thenReturn(1);
        when(lifecycleSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(lifecycleSession.fireAllRules()).thenReturn(1);
        when(governanceSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(governanceSession.fireAllRules()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Map<String, Object> results = ruleEngineService.evaluateAll(testAsset);

        // Then
        assertNotNull(results);
        assertTrue(results.containsKey("quality"));
        assertTrue(results.containsKey("compliance"));
        assertTrue(results.containsKey("value"));
        assertTrue(results.containsKey("lifecycle"));
        assertTrue(results.containsKey("governance"));
        assertTrue(results.containsKey("summary"));

        // 验证每个会话都被调用了
        verify(qualitySession, times(1)).insert(any(AssetEvaluation.class));
        verify(complianceSession, times(1)).insert(any(AssetEvaluation.class));
        verify(valueSession, times(1)).insert(any(AssetEvaluation.class));
        verify(lifecycleSession, times(1)).insert(any(AssetEvaluation.class));
        verify(governanceSession, times(1)).insert(any(AssetEvaluation.class));
    }

    @Test
    @DisplayName("测试规则测试 - 按分类执行")
    void testTestRule_ByCategory() throws Exception {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("assetId", "TEST-002");
        input.put("assetName", "测试资产2");
        input.put("qualityIssues", 3);

        when(qualitySession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(qualitySession.fireAllRules()).thenReturn(1);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Map<String, Object> result = ruleEngineService.testRule("QUALITY", input);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("result"));
        assertTrue(result.get("result") instanceof QualityScoreResult);
    }

    @Test
    @DisplayName("测试规则测试 - 不支持的分类抛出异常")
    void testTestRule_UnsupportedCategory_ThrowsException() {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("assetId", "TEST-003");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ruleEngineService.testRule("UNSUPPORTED", input);
        });
    }

    @Test
    @DisplayName("测试执行日志查询 - 按资产ID查询")
    void testGetExecutionLogs_ByAssetId() {
        // Given
        List<RuleExecutionLog> logs = Arrays.asList(new RuleExecutionLog(), new RuleExecutionLog());
        when(executionLogRepository.findByAssetId("TEST-001")).thenReturn(logs);

        // When
        List<RuleExecutionLog> result = ruleEngineService.getExecutionLogs("TEST-001");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(executionLogRepository, times(1)).findByAssetId("TEST-001");
    }

    @Test
    @DisplayName("测试执行日志查询 - 空资产ID查询全部")
    void testGetExecutionLogs_AllLogs() {
        // Given
        List<RuleExecutionLog> logs = Arrays.asList(new RuleExecutionLog());
        when(executionLogRepository.findAll()).thenReturn(logs);

        // When
        List<RuleExecutionLog> result = ruleEngineService.getExecutionLogs(null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(executionLogRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试执行日志查询 - 空字符串资产ID查询全部")
    void testGetExecutionLogs_EmptyAssetId() {
        // Given
        List<RuleExecutionLog> logs = Arrays.asList(new RuleExecutionLog());
        when(executionLogRepository.findAll()).thenReturn(logs);

        // When
        List<RuleExecutionLog> result = ruleEngineService.getExecutionLogs("");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(executionLogRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试规则重载")
    void testReloadRules() {
        // When
        ruleEngineService.reloadRules();

        // Then
        verify(droolsConfig, times(1)).reloadRules(any());
    }

    @Test
    @DisplayName("测试输入映射 - 完整输入")
    void testMapToAssetEvaluation_CompleteInput() {
        // Given
        Map<String, Object> input = new HashMap<>();
        input.put("assetId", "MAP-001");
        input.put("assetName", "映射测试资产");
        input.put("assetType", "VIEW");
        input.put("qualityIssues", 5);
        input.put("completeness", 0.8);
        input.put("accuracy", 0.75);
        input.put("consistency", 0.70);
        input.put("timeliness", 0.65);
        input.put("uniqueness", 0.85);
        input.put("dailyAccessCount", 50.0);
        input.put("dataSizeMb", 100.0);
        input.put("standardCode", "DS-002");
        input.put("owner", "testuser");
        input.put("businessDomain", "SALES");
        input.put("sensitivityLevel", "MEDIUM");

        // When - 通过testRule间接测试mapToAssetEvaluation
        Map<String, Object> result = ruleEngineService.testRule("QUALITY", input);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("result"));
    }

    @Test
    @DisplayName("测试质量等级判定 - 边界值")
    void testQualityLevel_BoundaryValues() {
        // Given
        when(qualitySession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 测试EXCELLENT边界 (>=90)
        testAsset.setQualityScore(95);
        when(qualitySession.fireAllRules()).thenReturn(1);
        QualityScoreResult result1 = ruleEngineService.evaluateQualityScore(testAsset);
        assertEquals("EXCELLENT", result1.getQualityLevel());

        // 测试GOOD边界 (>=75)
        testAsset.setQualityScore(80);
        QualityScoreResult result2 = ruleEngineService.evaluateQualityScore(testAsset);
        assertEquals("GOOD", result2.getQualityLevel());

        // 测试FAIR边界 (>=60)
        testAsset.setQualityScore(65);
        QualityScoreResult result3 = ruleEngineService.evaluateQualityScore(testAsset);
        assertEquals("FAIR", result3.getQualityLevel());

        // 测试POOR边界 (<60)
        testAsset.setQualityScore(50);
        QualityScoreResult result4 = ruleEngineService.evaluateQualityScore(testAsset);
        assertEquals("POOR", result4.getQualityLevel());
    }

    @Test
    @DisplayName("测试价值等级判定 - 边界值")
    void testValueLevel_BoundaryValues() {
        // Given
        when(valueSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 测试HIGH边界 (>=80)
        testAsset.setValueScore(85);
        when(valueSession.fireAllRules()).thenReturn(1);
        ValueScoreResult result1 = ruleEngineService.evaluateValue(testAsset);
        assertEquals("HIGH", result1.getValueLevel());

        // 测试MEDIUM边界 (>=60)
        testAsset.setValueScore(65);
        ValueScoreResult result2 = ruleEngineService.evaluateValue(testAsset);
        assertEquals("MEDIUM", result2.getValueLevel());

        // 测试LOW边界 (<60)
        testAsset.setValueScore(55);
        ValueScoreResult result3 = ruleEngineService.evaluateValue(testAsset);
        assertEquals("LOW", result3.getValueLevel());
    }

    @Test
    @DisplayName("测试生命周期动作判定")
    void testLifecycleAction() {
        // Given
        when(lifecycleSession.insert(any(AssetEvaluation.class))).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(executionLogRepository.save(any(RuleExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 测试RETIRED -> RETIRE
        testAsset.setLifecyclePhase("RETIRED");
        when(lifecycleSession.fireAllRules()).thenReturn(1);
        LifecycleResult result1 = ruleEngineService.evaluateLifecycle(testAsset);
        assertEquals("RETIRE", result1.getAction());

        // 测试FROZEN -> FREEZE
        testAsset.setLifecyclePhase("FROZEN");
        LifecycleResult result2 = ruleEngineService.evaluateLifecycle(testAsset);
        assertEquals("FREEZE", result2.getAction());

        // 测试COLD -> ARCHIVE
        testAsset.setLifecyclePhase("COLD");
        LifecycleResult result3 = ruleEngineService.evaluateLifecycle(testAsset);
        assertEquals("ARCHIVE", result3.getAction());

        // 测试其他 -> KEEP
        testAsset.setLifecyclePhase("ACTIVE");
        LifecycleResult result4 = ruleEngineService.evaluateLifecycle(testAsset);
        assertEquals("KEEP", result4.getAction());
    }
}
