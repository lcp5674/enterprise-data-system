package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.context.AnalysisContext;
import com.enterprise.edams.analysis.entity.AnalysisResult;
import com.enterprise.edams.analysis.entity.LocalModelConfig;
import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.llm.LLMConnector;
import com.enterprise.edams.analysis.llm.LLMConnectorFactory;
import com.enterprise.edams.analysis.llm.LLMRequest;
import com.enterprise.edams.analysis.llm.LLMResponse;
import com.enterprise.edams.analysis.metadata.ColumnMetadata;
import com.enterprise.edams.analysis.metadata.TableMetadata;
import com.enterprise.edams.analysis.repository.LocalModelConfigRepository;
import com.enterprise.edams.analysis.result.IndicatorDefinition;
import com.enterprise.edams.analysis.result.LineageRelation;
import com.enterprise.edams.analysis.result.SubjectClassification;
import com.enterprise.edams.analysis.result.TableAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentAnalyzerService {

    private final LocalModelConfigRepository configRepository;
    private final LLMConnectorFactory connectorFactory;
    private final ObjectMapper objectMapper;
    private final PromptTemplateManager templateManager;

    public AnalysisResult analyzeTable(AnalysisContext context, String tableName, TableMetadata metadata) {
        log.info("Analyzing table: {}.{}", metadata.getSchemaName(), tableName);

        LocalModelConfig config = configRepository.findById(context.getModelConfigId())
                .orElseThrow(() -> new AnalysisException("CONFIG_NOT_FOUND", "模型配置不存在"));

        LLMConnector connector = connectorFactory.getConnector(config);

        AnalysisResult.AnalysisResultBuilder resultBuilder = AnalysisResult.builder()
                .schemaName(metadata.getSchemaName())
                .tableName(tableName)
                .success(true);

        try {
            TableAnalysisResult tableAnalysis = analyzeTableStructure(context, connector, config, metadata);
            resultBuilder.tableDescription(tableAnalysis.getTableDescription())
                    .tableAlias(tableAnalysis.getTableAlias())
                    .tableCategory(tableAnalysis.getTableCategory())
                    .fieldAnalysisResult(toJson(tableAnalysis.getFieldDescriptions()))
                    .confidence(tableAnalysis.getConfidence());

            if (Boolean.TRUE.equals(context.getEnableLineageAnalysis())) {
                List<LineageRelation> lineages = analyzeLineage(context, connector, config, metadata);
                resultBuilder.lineageRelations(toJson(lineages));
            }

            if (Boolean.TRUE.equals(context.getEnableIndicatorExtraction())) {
                List<IndicatorDefinition> indicators = extractIndicators(context, connector, config, metadata);
                resultBuilder.indicatorDefinitions(toJson(indicators));
            }

            if (Boolean.TRUE.equals(context.getEnableSubjectClassification())) {
                SubjectClassification classification = classifySubject(context, connector, config, metadata, tableAnalysis);
                resultBuilder.subjectDomain(classification.getSubjectDomain())
                        .businessDomain(classification.getBusinessDomain())
                        .dataDomain(classification.getDataDomain())
                        .indicatorLayer(classification.getIndicatorLayer() != null 
                                ? classification.getIndicatorLayer().name() : null);
            }

            log.info("Table analysis completed: {}.{}", metadata.getSchemaName(), tableName);

        } catch (Exception e) {
            log.error("Table analysis failed: {}.{}", metadata.getSchemaName(), tableName, e);
            resultBuilder.success(false)
                    .errorMessage(e.getMessage());
        }

        return resultBuilder.build();
    }

    public TableAnalysisResult analyzeTableStructure(AnalysisContext context, LLMConnector connector,
                                                    LocalModelConfig config, TableMetadata metadata) {
        String prompt = buildTableAnalysisPrompt(config, metadata);

        LLMRequest request = LLMRequest.builder()
                .model(config.getModelName())
                .prompt(prompt)
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .build();

        LLMResponse response = connector.generate(request);

        if (!response.getSuccess()) {
            throw new AnalysisException("LLM_FAILED", "LLM调用失败: " + response.getErrorMessage());
        }

        return parseTableAnalysisResult(response.getContent());
    }

    public List<LineageRelation> analyzeLineage(AnalysisContext context, LLMConnector connector,
                                                  LocalModelConfig config, TableMetadata metadata) {
        String prompt = buildLineageAnalysisPrompt(config, metadata);

        LLMRequest request = LLMRequest.builder()
                .model(config.getModelName())
                .prompt(prompt)
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .build();

        LLMResponse response = connector.generate(request);

        if (!response.getSuccess()) {
            log.warn("Lineage analysis failed: {}", response.getErrorMessage());
            return List.of();
        }

        return parseLineageResult(response.getContent());
    }

    public List<IndicatorDefinition> extractIndicators(AnalysisContext context, LLMConnector connector,
                                                        LocalModelConfig config, TableMetadata metadata) {
        String prompt = buildIndicatorExtractionPrompt(config, metadata);

        LLMRequest request = LLMRequest.builder()
                .model(config.getModelName())
                .prompt(prompt)
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .build();

        LLMResponse response = connector.generate(request);

        if (!response.getSuccess()) {
            log.warn("Indicator extraction failed: {}", response.getErrorMessage());
            return List.of();
        }

        return parseIndicatorResult(response.getContent());
    }

    public SubjectClassification classifySubject(AnalysisContext context, LLMConnector connector,
                                                LocalModelConfig config, TableMetadata metadata,
                                                TableAnalysisResult tableAnalysis) {
        String prompt = buildSubjectClassificationPrompt(config, metadata, tableAnalysis);

        LLMRequest request = LLMRequest.builder()
                .model(config.getModelName())
                .prompt(prompt)
                .maxTokens(config.getMaxTokens())
                .temperature(config.getTemperature())
                .build();

        LLMResponse response = connector.generate(request);

        if (!response.getSuccess()) {
            log.warn("Subject classification failed: {}", response.getErrorMessage());
            return SubjectClassification.builder().build();
        }

        return parseSubjectClassification(response.getContent());
    }

    private String buildTableAnalysisPrompt(LocalModelConfig config, TableMetadata metadata) {
        String template = config.getTableAnalysisTemplate();
        if (template == null || template.isEmpty()) {
            template = templateManager.getDefaultTableAnalysisTemplate();
        }

        StringBuilder columnDefs = new StringBuilder();
        for (ColumnMetadata column : metadata.getColumns()) {
            columnDefs.append(String.format("- %s (%s): %s%n",
                    column.getColumnName(),
                    column.getDataType(),
                    column.getColumnComment() != null ? column.getColumnComment() : "无注释"));
        }

        return template
                .replace("{tableName}", metadata.getTableName())
                .replace("{schema}", metadata.getSchemaName() != null ? metadata.getSchemaName() : "default")
                .replace("{columnDefinitions}", columnDefs.toString())
                .replace("{tableComment}", metadata.getTableComment() != null ? metadata.getTableComment() : "无");
    }

    private String buildLineageAnalysisPrompt(LocalModelConfig config, TableMetadata metadata) {
        String template = config.getLineageAnalysisTemplate();
        if (template == null || template.isEmpty()) {
            template = templateManager.getDefaultLineageAnalysisTemplate();
        }

        StringBuilder columnDefs = new StringBuilder();
        for (ColumnMetadata column : metadata.getColumns()) {
            columnDefs.append(String.format("- %s (%s)%n", column.getColumnName(), column.getDataType()));
        }

        return template
                .replace("{tableName}", metadata.getTableName())
                .replace("{schema}", metadata.getSchemaName() != null ? metadata.getSchemaName() : "default")
                .replace("{columnsInfo}", columnDefs.toString());
    }

    private String buildIndicatorExtractionPrompt(LocalModelConfig config, TableMetadata metadata) {
        String template = config.getIndicatorExtractionTemplate();
        if (template == null || template.isEmpty()) {
            template = templateManager.getDefaultIndicatorExtractionTemplate();
        }

        StringBuilder columnDefs = new StringBuilder();
        for (ColumnMetadata column : metadata.getColumns()) {
            columnDefs.append(String.format("- %s: %s (%s)%n",
                    column.getColumnName(),
                    column.getColumnComment() != null ? column.getColumnComment() : "无注释",
                    column.getDataType()));
        }

        return template
                .replace("{tableName}", metadata.getTableName())
                .replace("{tableDescription}", metadata.getTableComment() != null ? metadata.getTableComment() : "无")
                .replace("{columnDefinitions}", columnDefs.toString());
    }

    private String buildSubjectClassificationPrompt(LocalModelConfig config, TableMetadata metadata,
                                                    TableAnalysisResult tableAnalysis) {
        String template = config.getSubjectClassificationTemplate();
        if (template == null || template.isEmpty()) {
            template = templateManager.getDefaultSubjectClassificationTemplate();
        }

        StringBuilder fieldList = new StringBuilder();
        for (ColumnMetadata column : metadata.getColumns()) {
            fieldList.append(String.format("- %s: %s%n",
                    column.getColumnName(),
                    column.getColumnComment() != null ? column.getColumnComment() : "无注释"));
        }

        return template
                .replace("{tableName}", metadata.getTableName())
                .replace("{tableDescription}", tableAnalysis.getTableDescription())
                .replace("{fieldList}", fieldList.toString());
    }

    private TableAnalysisResult parseTableAnalysisResult(String content) {
        try {
            String jsonContent = extractJson(content);
            JsonNode node = objectMapper.readTree(jsonContent);

            Map<String, String> fieldDescs = new HashMap<>();
            JsonNode fieldNode = node.get("fieldDescriptions");
            if (fieldNode != null && fieldNode.isObject()) {
                fieldNode.fields().forEachRemaining(entry ->
                        fieldDescs.put(entry.getKey(), entry.getValue().asText()));
            }

            return TableAnalysisResult.builder()
                    .tableDescription(getTextValue(node, "tableDescription"))
                    .tableAlias(getTextValue(node, "tableAlias"))
                    .tableCategory(getTextValue(node, "tableCategory"))
                    .fieldDescriptions(fieldDescs)
                    .primaryKey(getTextValue(node, "primaryKey"))
                    .confidence(getDecimalValue(node, "confidence", BigDecimal.valueOf(0.85)))
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse table analysis result: {}", e.getMessage());
            return TableAnalysisResult.builder()
                    .tableDescription("分析结果解析失败")
                    .confidence(BigDecimal.valueOf(0.5))
                    .build();
        }
    }

    private List<LineageRelation> parseLineageResult(String content) {
        try {
            String jsonContent = extractJson(content);
            JsonNode node = objectMapper.readTree(jsonContent);
            JsonNode relationsNode = node.get("lineageRelations");

            if (relationsNode == null || !relationsNode.isArray()) {
                return List.of();
            }

            return objectMapper.convertValue(relationsNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LineageRelation.class));

        } catch (Exception e) {
            log.warn("Failed to parse lineage result: {}", e.getMessage());
            return List.of();
        }
    }

    private List<IndicatorDefinition> parseIndicatorResult(String content) {
        try {
            String jsonContent = extractJson(content);
            JsonNode node = objectMapper.readTree(jsonContent);
            JsonNode indicatorsNode = node.get("indicators");

            if (indicatorsNode == null || !indicatorsNode.isArray()) {
                return List.of();
            }

            return objectMapper.convertValue(indicatorsNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, IndicatorDefinition.class));

        } catch (Exception e) {
            log.warn("Failed to parse indicator result: {}", e.getMessage());
            return List.of();
        }
    }

    private SubjectClassification parseSubjectClassification(String content) {
        try {
            String jsonContent = extractJson(content);
            JsonNode node = objectMapper.readTree(jsonContent);

            return SubjectClassification.builder()
                    .subjectDomain(getTextValue(node, "subjectDomain"))
                    .businessDomain(getTextValue(node, "businessDomain"))
                    .dataDomain(getTextValue(node, "dataDomain"))
                    .reasoning(getTextValue(node, "reasoning"))
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse subject classification: {}", e.getMessage());
            return SubjectClassification.builder().build();
        }
    }

    private String extractJson(String content) {
        content = content.trim();

        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}");

        if (startIndex >= 0 && endIndex > startIndex) {
            return content.substring(startIndex, endIndex + 1);
        }

        startIndex = content.indexOf("[");
        endIndex = content.lastIndexOf("]");

        if (startIndex >= 0 && endIndex > startIndex) {
            return "{\"data\": " + content.substring(startIndex, endIndex + 1) + "}";
        }

        return content;
    }

    private String getTextValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    private BigDecimal getDecimalValue(JsonNode node, String field, BigDecimal defaultValue) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull()) {
            try {
                return new BigDecimal(fieldNode.asText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON", e);
            return "{}";
        }
    }
}
