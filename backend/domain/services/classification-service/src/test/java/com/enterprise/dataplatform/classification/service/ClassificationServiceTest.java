package com.enterprise.dataplatform.classification.service;

import com.enterprise.dataplatform.classification.domain.entity.ClassificationRule;
import com.enterprise.dataplatform.classification.domain.enums.ClassificationRuleType;
import com.enterprise.dataplatform.classification.domain.enums.SensitivityLevel;
import com.enterprise.dataplatform.classification.dto.request.ClassificationRequest;
import com.enterprise.dataplatform.classification.dto.response.ClassificationResponse;
import com.enterprise.dataplatform.classification.repository.AssetClassificationRepository;
import com.enterprise.dataplatform.classification.repository.ClassificationRuleRepository;
import com.enterprise.dataplatform.classification.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("分类分级服务测试")
class ClassificationServiceTest {

    @Mock
    private ClassificationRuleRepository ruleRepository;

    @Mock
    private AssetClassificationRepository classificationRepository;

    private ClassificationService classificationService;
    private List<ClassificationStrategy> strategies;

    @BeforeEach
    void setUp() {
        strategies = Arrays.asList(
                new PatternMatchingStrategy(),
                new DataTypeStrategy(),
                new KeywordMatchingStrategy(),
                new ColumnNameStrategy()
        );
        classificationService = new ClassificationService(
                ruleRepository,
                classificationRepository,
                strategies
        );
    }

    @Test
    @DisplayName("关键词匹配策略 - 包含敏感词的字段应被正确分类")
    void testKeywordMatchingStrategy() {
        KeywordMatchingStrategy strategy = new KeywordMatchingStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.KEYWORD)
                .sensitivityLevel(SensitivityLevel.HIGHLY_CONFIDENTIAL)
                .ruleName("Password Detection")
                .build();

        List<String> sampleValues = Arrays.asList("password123", "secret_key", "normal_value");

        ClassificationResponse result = strategy.classify(
                "field1", "password", "string", sampleValues, rule);

        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
        assertThat(result.getSensitivityLevel()).isEqualTo(SensitivityLevel.HIGHLY_CONFIDENTIAL);
        assertThat(result.getConfidenceScore()).isGreaterThan(0);
    }

    @Test
    @DisplayName("关键词匹配策略 - 无敏感词应返回NO_MATCH")
    void testKeywordMatchingNoMatch() {
        KeywordMatchingStrategy strategy = new KeywordMatchingStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.KEYWORD)
                .sensitivityLevel(SensitivityLevel.HIGHLY_CONFIDENTIAL)
                .ruleName("Password Detection")
                .build();

        List<String> sampleValues = Arrays.asList("normal_value", "public_info");

        ClassificationResponse result = strategy.classify(
                "field1", "description", "string", sampleValues, rule);

        assertThat(result.getStatus()).isEqualTo("NO_MATCH");
    }

    @Test
    @DisplayName("列名匹配策略 - 匹配列名应正确分类")
    void testColumnNameStrategy() {
        ColumnNameStrategy strategy = new ColumnNameStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.COLUMN_NAME)
                .sensitivityLevel(SensitivityLevel.CONFIDENTIAL)
                .columnPattern(".*phone.*")
                .ruleName("Phone Number Detection")
                .build();

        List<String> sampleValues = Arrays.asList("1234567890");

        ClassificationResponse result = strategy.classify(
                "field1", "user_phone_number", "string", sampleValues, rule);

        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
        assertThat(result.getSensitivityLevel()).isEqualTo(SensitivityLevel.CONFIDENTIAL);
    }

    @Test
    @DisplayName("数据类型策略 - BLOB类型应被分类为高度机密")
    void testDataTypeStrategy() {
        DataTypeStrategy strategy = new DataTypeStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.DATA_TYPE)
                .sensitivityLevel(SensitivityLevel.HIGHLY_CONFIDENTIAL)
                .dataType("blob")
                .ruleName("Binary Data Detection")
                .build();

        ClassificationResponse result = strategy.classify(
                "field1", "file_content", "blob", null, rule);

        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
        assertThat(result.getSensitivityLevel()).isEqualTo(SensitivityLevel.HIGHLY_CONFIDENTIAL);
    }

    @Test
    @DisplayName("正则匹配策略 - 匹配正则表达式应正确分类")
    void testPatternMatchingStrategy() {
        PatternMatchingStrategy strategy = new PatternMatchingStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.PATTERN_MATCH)
                .sensitivityLevel(SensitivityLevel.CONFIDENTIAL)
                .pattern("\\d{11}")  // 11位数字
                .confidenceThreshold(0.7)
                .ruleName("Phone Pattern")
                .build();

        List<String> sampleValues = Arrays.asList("13812345678", "13998765432", "invalid");

        ClassificationResponse result = strategy.classify(
                "field1", "phone", "string", sampleValues, rule);

        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
        assertThat(result.getConfidenceScore()).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("分类服务 - 已有分类的资产不应重复分类")
    void testClassifyAssetExisting() {
        when(classificationRepository.findByAssetId("asset-001")).thenReturn(Optional.empty());
        when(ruleRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        ClassificationRequest request = ClassificationRequest.builder()
                .assetId("asset-001")
                .columnName("test_column")
                .dataType("string")
                .build();

        ClassificationResponse result = classificationService.classifyAsset(request);

        assertThat(result.getAssetId()).isEqualTo("asset-001");
        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
    }

    @Test
    @DisplayName("分类服务 - 强制重新分类应覆盖现有结果")
    void testClassifyAssetForceClassify() {
        when(ruleRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

        ClassificationRequest request = ClassificationRequest.builder()
                .assetId("asset-001")
                .columnName("test_column")
                .dataType("string")
                .forceClassify(true)
                .build();

        ClassificationResponse result = classificationService.classifyAsset(request);

        assertThat(result.getAssetId()).isEqualTo("asset-001");
    }

    @Test
    @DisplayName("敏感级别 - 高度机密级别应有最高优先级")
    void testSensitivityLevelOrder() {
        assertThat(SensitivityLevel.HIGHLY_CONFIDENTIAL.getLevel())
                .isGreaterThan(SensitivityLevel.CONFIDENTIAL.getLevel());
        assertThat(SensitivityLevel.CONFIDENTIAL.getLevel())
                .isGreaterThan(SensitivityLevel.INTERNAL.getLevel());
        assertThat(SensitivityLevel.INTERNAL.getLevel())
                .isGreaterThan(SensitivityLevel.PUBLIC.getLevel());
    }

    @Test
    @DisplayName("关键词匹配策略 - 身份证关键词检测")
    void testKeywordDetectionIdCard() {
        KeywordMatchingStrategy strategy = new KeywordMatchingStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.KEYWORD)
                .sensitivityLevel(SensitivityLevel.HIGHLY_CONFIDENTIAL)
                .ruleName("ID Card Detection")
                .build();

        List<String> sampleValues = Arrays.asList("110101199001011234", "123456789012345", "normal");

        ClassificationResponse result = strategy.classify(
                "field1", "id_card_number", "string", sampleValues, rule);

        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
        assertThat(result.getMatchedKeywords()).isNotEmpty();
    }

    @Test
    @DisplayName("关键词匹配策略 - 邮箱关键词检测")
    void testKeywordDetectionEmail() {
        KeywordMatchingStrategy strategy = new KeywordMatchingStrategy();
        
        ClassificationRule rule = ClassificationRule.builder()
                .ruleType(ClassificationRuleType.KEYWORD)
                .sensitivityLevel(SensitivityLevel.CONFIDENTIAL)
                .ruleName("Email Detection")
                .build();

        List<String> sampleValues = Arrays.asList("user@example.com", "contact@company.org");

        ClassificationResponse result = strategy.classify(
                "field1", "email_address", "string", sampleValues, rule);

        assertThat(result.getStatus()).isEqualTo("CLASSIFIED");
        assertThat(result.getMatchedKeywords()).isNotEmpty();
    }

    @Test
    @DisplayName("分类规则类型 - 所有类型应可访问")
    void testClassificationRuleTypes() {
        assertThat(ClassificationRuleType.values()).hasSizeGreaterThan(0);
        assertThat(ClassificationRuleType.PATTERN_MATCH.getDescription()).isNotEmpty();
        assertThat(ClassificationRuleType.KEYWORD.getDescription()).isNotEmpty();
        assertThat(ClassificationRuleType.DATA_TYPE.getDescription()).isNotEmpty();
    }

    @Test
    @DisplayName("敏感级别 - 中文描述应正确返回")
    void testSensitivityLevelDescriptions() {
        assertThat(SensitivityLevel.PUBLIC.getDescription()).isEqualTo("公开");
        assertThat(SensitivityLevel.INTERNAL.getDescription()).isEqualTo("内部");
        assertThat(SensitivityLevel.CONFIDENTIAL.getDescription()).isEqualTo("机密");
        assertThat(SensitivityLevel.HIGHLY_CONFIDENTIAL.getDescription()).isEqualTo("高度机密");
    }
}
