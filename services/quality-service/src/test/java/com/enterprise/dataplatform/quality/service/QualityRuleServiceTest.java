package com.enterprise.dataplatform.quality.service;

import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.QualityRuleRequest;
import com.enterprise.dataplatform.quality.repository.QualityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityRuleServiceTest {

    @Mock
    private QualityRuleRepository ruleRepository;

    @InjectMocks
    private QualityRuleService ruleService;

    private QualityRule testRule;

    @BeforeEach
    void setUp() {
        testRule = QualityRule.builder()
                .id(1L)
                .ruleName("完整性检测")
                .ruleCode("RULE_001")
                .ruleType("COMPLETENESS")
                .description("检测数据完整性")
                .targetAssetType("TABLE")
                .targetAssetSubType("COLUMN")
                .checkSql("SELECT COUNT(*) FROM ${table}")
                .threshold(0.95)
                .operator("GTE")
                .errorCode("E001")
                .errorMessage("数据完整性低于95%")
                .severity("HIGH")
                .enabled(true)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createRule_shouldCreateSuccessfully() {
        QualityRuleRequest request = QualityRuleRequest.builder()
                .ruleName("新规则")
                .ruleCode("RULE_002")
                .ruleType("ACCURACY")
                .targetAssetType("TABLE")
                .build();

        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        QualityRule result = ruleService.createRule(request);

        assertNotNull(result);
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    void getRuleById_shouldReturnRuleWhenExists() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));

        Optional<QualityRule> result = ruleService.getRuleById(1L);

        assertTrue(result.isPresent());
        assertEquals(testRule.getRuleCode(), result.get().getRuleCode());
    }

    @Test
    void getRuleById_shouldReturnEmptyWhenNotExists() {
        when(ruleRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<QualityRule> result = ruleService.getRuleById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateRule_shouldUpdateSuccessfully() {
        QualityRuleRequest request = QualityRuleRequest.builder()
                .ruleName("更新的规则")
                .ruleCode("RULE_001")
                .ruleType("COMPLETENESS")
                .targetAssetType("TABLE")
                .threshold(0.90)
                .build();

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        QualityRule result = ruleService.updateRule(1L, request);

        assertNotNull(result);
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    void deleteRule_shouldDeleteSuccessfully() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        doNothing().when(ruleRepository).delete(any(QualityRule.class));

        assertDoesNotThrow(() -> ruleService.deleteRule(1L));

        verify(ruleRepository, times(1)).delete(any(QualityRule.class));
    }

    @Test
    void getRulesByType_shouldReturnMatchingRules() {
        when(ruleRepository.findByRuleType("COMPLETENESS")).thenReturn(List.of(testRule));

        List<QualityRule> results = ruleService.getRulesByType("COMPLETENESS");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getRulesByAssetType_shouldReturnMatchingRules() {
        when(ruleRepository.findByTargetAssetType("TABLE")).thenReturn(List.of(testRule));

        List<QualityRule> results = ruleService.getRulesByAssetType("TABLE");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getEnabledRules_shouldReturnEnabledRules() {
        when(ruleRepository.findByEnabled(true)).thenReturn(List.of(testRule));

        List<QualityRule> results = ruleService.getEnabledRules();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getEnabled());
    }

    @Test
    void searchRules_shouldReturnMatchingRules() {
        when(ruleRepository.searchByKeyword("完整性")).thenReturn(List.of(testRule));

        List<QualityRule> results = ruleService.searchRules("完整性");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void enableRule_shouldEnableSuccessfully() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        QualityRule result = ruleService.enableRule(1L);

        assertNotNull(result);
        assertTrue(result.getEnabled());
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    void disableRule_shouldDisableSuccessfully() {
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        QualityRule result = ruleService.disableRule(1L);

        assertNotNull(result);
        assertFalse(result.getEnabled());
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    void getRuleTypes_shouldReturnTypeList() {
        List<Map<String, Object>> types = ruleService.getRuleTypes();

        assertNotNull(types);
        assertFalse(types.isEmpty());
    }
}
