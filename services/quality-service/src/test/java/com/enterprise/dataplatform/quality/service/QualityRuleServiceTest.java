package com.enterprise.dataplatform.quality.service;

import com.enterprise.dataplatform.quality.domain.entity.QualityRule;
import com.enterprise.dataplatform.quality.dto.request.QualityRuleRequest;
import com.enterprise.dataplatform.quality.dto.response.QualityRuleResponse;
import com.enterprise.dataplatform.quality.repository.QualityRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * QualityRuleService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("质量规则服务测试")
class QualityRuleServiceTest {

    @Mock
    private QualityRuleRepository ruleRepository;

    @InjectMocks
    private QualityRuleService qualityRuleService;

    private QualityRule testRule;
    private QualityRuleRequest testRequest;

    @BeforeEach
    void setUp() {
        testRule = QualityRule.builder()
                .id(1L)
                .ruleCode("RULE-001")
                .ruleName("测试规则")
                .description("规则描述")
                .ruleType("COMPLETENESS")
                .ruleCategory("NULL_CHECK")
                .qualityDimension("完整性")
                .severityLevel("HIGH")
                .thresholdExpression("NOT NULL")
                .expectedValue("100%")
                .alertThreshold(5.0)
                .errorThreshold(10.0)
                .enabled(true)
                .status("DRAFT")
                .priority(1)
                .version(1)
                .creator("admin")
                .createTime(LocalDateTime.now())
                .build();

        testRequest = QualityRuleRequest.builder()
                .ruleCode("RULE-001")
                .ruleName("测试规则")
                .description("规则描述")
                .ruleType("COMPLETENESS")
                .ruleCategory("NULL_CHECK")
                .qualityDimension("完整性")
                .severityLevel("HIGH")
                .thresholdExpression("NOT NULL")
                .expectedValue("100%")
                .alertThreshold(5.0)
                .errorThreshold(10.0)
                .priority(1)
                .build();
    }

    @Test
    @DisplayName("创建质量规则 - 成功")
    void testCreateRule_Success() {
        // Given
        when(ruleRepository.existsByRuleCode("RULE-001")).thenReturn(false);
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        // When
        QualityRuleResponse response = qualityRuleService.createRule(testRequest, "admin");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRuleCode()).isEqualTo("RULE-001");
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    @DisplayName("创建质量规则 - 规则编码已存在")
    void testCreateRule_DuplicateCode() {
        // Given
        when(ruleRepository.existsByRuleCode("RULE-001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> qualityRuleService.createRule(testRequest, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("规则编码已存在");
    }

    @Test
    @DisplayName("更新质量规则 - 成功")
    void testUpdateRule_Success() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        QualityRuleRequest updateRequest = QualityRuleRequest.builder()
                .ruleName("更新后的规则名称")
                .description("更新后的描述")
                .ruleType("COMPLETENESS")
                .severityLevel("HIGH")
                .build();

        // When
        QualityRuleResponse response = qualityRuleService.updateRule(1L, updateRequest, "updater");

        // Then
        assertThat(response).isNotNull();
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    @DisplayName("更新质量规则 - 规则不存在")
    void testUpdateRule_NotFound() {
        // Given
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> qualityRuleService.updateRule(999L, testRequest, "updater"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("规则不存在");
    }

    @Test
    @DisplayName("发布质量规则 - 成功")
    void testPublishRule_Success() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        // When
        QualityRuleResponse response = qualityRuleService.publishRule(1L, "publisher");

        // Then
        assertThat(response).isNotNull();
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    @DisplayName("启用质量规则")
    void testSetRuleEnabled_Enable() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        // When
        QualityRuleResponse response = qualityRuleService.setRuleEnabled(1L, true, "updater");

        // Then
        assertThat(response).isNotNull();
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    @DisplayName("禁用质量规则")
    void testSetRuleEnabled_Disable() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));
        when(ruleRepository.save(any(QualityRule.class))).thenReturn(testRule);

        // When
        QualityRuleResponse response = qualityRuleService.setRuleEnabled(1L, false, "updater");

        // Then
        assertThat(response).isNotNull();
        verify(ruleRepository, times(1)).save(any(QualityRule.class));
    }

    @Test
    @DisplayName("查询质量规则 - 按ID")
    void testGetRule() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(testRule));

        // When
        QualityRuleResponse response = qualityRuleService.getRule(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("查询质量规则 - 按编码")
    void testGetRuleByCode() {
        // Given
        when(ruleRepository.findByRuleCode("RULE-001")).thenReturn(Optional.of(testRule));

        // When
        QualityRuleResponse response = qualityRuleService.getRuleByCode("RULE-001");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRuleCode()).isEqualTo("RULE-001");
    }

    @Test
    @DisplayName("查询质量规则 - 规则不存在")
    void testGetRule_NotFound() {
        // Given
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> qualityRuleService.getRule(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("规则不存在");
    }

    @Test
    @DisplayName("分页查询规则")
    void testSearchRules() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<QualityRule> page = new PageImpl<>(List.of(testRule));
        when(ruleRepository.searchRules(anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<QualityRuleResponse> response = qualityRuleService.searchRules("COMPLETENESS", "ACTIVE", "测试", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取启用的规则列表")
    void testGetEnabledRules() {
        // Given
        when(ruleRepository.findByEnabled(true)).thenReturn(List.of(testRule));

        // When
        List<QualityRuleResponse> rules = qualityRuleService.getEnabledRules();

        // Then
        assertThat(rules).isNotNull();
        assertThat(rules).hasSize(1);
    }

    @Test
    @DisplayName("根据资产获取规则")
    void testGetRulesByAssetId() {
        // Given
        testRule.setAssetId("ASSET-001");
        when(ruleRepository.findEnabledRulesByAssetId("ASSET-001")).thenReturn(List.of(testRule));

        // When
        List<QualityRuleResponse> rules = qualityRuleService.getRulesByAssetId("ASSET-001");

        // Then
        assertThat(rules).isNotNull();
        assertThat(rules).hasSize(1);
    }

    @Test
    @DisplayName("删除质量规则")
    void testDeleteRule() {
        // When
        qualityRuleService.deleteRule(1L);

        // Then
        verify(ruleRepository, times(1)).deleteById(1L);
    }
}
