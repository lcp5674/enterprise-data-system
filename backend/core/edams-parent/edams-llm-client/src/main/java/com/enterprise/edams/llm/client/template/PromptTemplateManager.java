package com.enterprise.edams.llm.client.template;

import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PromptTemplateManager {

    private static final String DATA_CLASSIFICATION_SYSTEM_PROMPT = """
            你是一个专业的数据分类分级专家。你需要根据企业数据分类分级标准，对给定的数据表进行分类分级。

            ## 分类分级标准

            ### 公开级（Level 1）
            - 可对外公开的数据
            - 不涉及任何敏感信息
            - 示例：公司公开发布的产品目录、公共联系方式

            ### 内部级（Level 2）
            - 仅企业内部可访问
            - 不涉及商业秘密或个人隐私
            - 示例：内部组织架构、内部流程文档

            ### 敏感级（Level 3）
            - 涉及商业秘密或个人隐私
            - 需要严格的访问控制
            - 示例：客户个人信息、财务数据、合同信息

            ### 机密级（Level 4）
            - 核心商业数据或高度敏感数据
            - 需要最高级别的保护
            - 示例：核心技术配方、战略规划、高管薪酬

            ## 输出格式

            请返回JSON格式的分类结果：
            ```json
            {
                "tableLevel": "Level_级别数字",
                "columnLevels": {
                    "字段名": "Level_级别数字",
                    ...
                },
                "reason": "分类理由",
                "recommendations": ["建议1", "建议2", ...]
            }
            ```
            """;

    private static final String COLUMN_DESCRIPTION_SYSTEM_PROMPT = """
            你是一个专业的数据字典专家。你需要为给定的数据字段生成准确、简洁的描述。

            ## 要求

            1. 描述应该清晰、准确、易于理解
            2. 说明字段的业务含义和用途
            3. 标明字段的数据类型和格式要求
            4. 对于敏感字段，标注安全注意事项

            ## 输出格式

            请返回JSON格式的字段描述：
            ```json
            {
                "fieldName": "字段名",
                "description": "字段描述",
                "businessMeaning": "业务含义",
                "dataType": "数据类型",
                "formatRequirement": "格式要求（如果有）",
                "securityNote": "安全注意事项（如果没有则为空）",
                "examples": ["示例值1", "示例值2"]
            }
            ```
            """;

    private static final String DATA_QUALITY_SYSTEM_PROMPT = """
            你是一个专业的数据质量专家。你需要分析数据质量问题并提供改进建议。

            ## 质量维度

            1. **完整性**：数据是否完整，有无缺失值
            2. **准确性**：数据是否准确，有无错误值
            3. **一致性**：数据是否一致，有无矛盾
            4. **时效性**：数据是否及时更新
            5. **唯一性**：数据是否有重复

            ## 输出格式

            请返回JSON格式的质量分析结果：
            ```json
            {
                "overallScore": "总体评分(0-100)",
                "issues": [
                    {
                        "type": "问题类型",
                        "severity": "严重程度(high/medium/low)",
                        "description": "问题描述",
                        "affectedRecords": "影响记录数或比例",
                        "suggestions": ["改进建议1", "改进建议2"]
                    }
                ],
                "summary": "总结说明"
            }
            ```
            """;

    private static final String DATA_LINEAGE_SYSTEM_PROMPT = """
            你是一个专业的数据血缘分析专家。你需要分析数据血缘关系，生成数据血缘图谱。

            ## 任务

            1. 识别数据来源和上游依赖
            2. 识别数据去向和下游影响
            3. 识别数据转换和加工逻辑
            4. 标注关键数据节点

            ## 输出格式

            请返回JSON格式的血缘分析结果：
            ```json
            {
                "sourceNodes": ["上游数据源1", "上游数据源2"],
                "targetNodes": ["下游数据目标1", "下游数据目标2"],
                "transformations": [
                    {
                        "from": "源表/字段",
                        "to": "目标表/字段",
                        "logic": "转换逻辑描述"
                    }
                ],
                "impactAnalysis": "影响分析说明"
            }
            ```
            """;

    private static final String SQL_GENERATION_SYSTEM_PROMPT = """
            你是一个专业的数据分析SQL生成专家。你需要根据业务需求生成准确、高效的SQL查询。

            ## 要求

            1. SQL语句应该语法正确、性能优化
            2. 考虑查询效率，避免全表扫描
            3. 添加必要的注释说明
            4. 考虑数据安全和权限控制

            ## 输出格式

            请返回JSON格式的SQL生成结果：
            ```json
            {
                "sql": "生成的SQL语句",
                "description": "SQL语句说明",
                "parameters": ["参数说明1", "参数说明2"],
                "notes": ["注意事项1", "注意事项2"]
            }
            ```
            """;

    public String buildDataClassificationPrompt(String tableName, List<String> columns) {
        String columnList = columns.stream()
                .map(col -> "- " + col)
                .collect(Collectors.joining("\n"));

        return String.format("""
                请分析以下数据表并进行分类分级：

                ## 数据表信息

                **表名**: %s

                **字段列表**:
                %s

                请根据分类分级标准进行评估，并返回JSON格式的结果。
                """, tableName, columnList);
    }

    public String buildColumnDescriptionPrompt(String tableName, String columnName,
                                                String dataType, List<String> sampleValues) {
        String samples = sampleValues != null && !sampleValues.isEmpty()
                ? sampleValues.stream().limit(5).collect(Collectors.joining(", "))
                : "无";

        return String.format("""
                请为以下字段生成详细描述：

                ## 字段信息

                **表名**: %s
                **字段名**: %s
                **数据类型**: %s
                **示例值**: %s

                请生成JSON格式的字段描述。
                """, tableName, columnName, dataType, samples);
    }

    public String buildDataQualityPrompt(String tableName, List<String> issues) {
        String issueList = issues.stream()
                .map(issue -> "- " + issue)
                .collect(Collectors.joining("\n"));

        return String.format("""
                请分析以下数据质量问题并提供改进建议：

                ## 数据表信息

                **表名**: %s

                ## 发现的问题:
                %s

                请分析问题原因并返回JSON格式的改进建议。
                """, tableName, issueList);
    }

    public String buildDataLineagePrompt(String tableName, String direction) {
        String task = "analyze".equalsIgnoreCase(direction)
                ? "分析该表的数据血缘关系"
                : "生成该表的数据血缘图谱";

        return String.format("""
                %s

                ## 目标表

                **表名**: %s

                请分析并返回JSON格式的血缘分析结果。
                """, DATA_LINEAGE_SYSTEM_PROMPT, tableName);
    }

    public String buildSQLGenerationPrompt(String requirement, List<String> tableInfo) {
        String tables = tableInfo.stream()
                .map(info -> "- " + info)
                .collect(Collectors.joining("\n"));

        return String.format("""
                请根据以下业务需求生成SQL查询：

                ## 业务需求

                %s

                ## 可用数据表

                %s

                请生成JSON格式的SQL语句。
                """, requirement, tables);
    }

    public ChatRequest buildClassificationRequest(String tableName, List<String> columns) {
        String userPrompt = buildDataClassificationPrompt(tableName, columns);
        return ChatRequest.builder()
                .prompt(userPrompt)
                .messages(List.of(
                        ChatMessage.system(DATA_CLASSIFICATION_SYSTEM_PROMPT),
                        ChatMessage.user(userPrompt)
                ))
                .temperature(0.3)
                .maxTokens(2000)
                .build();
    }

    public ChatRequest buildColumnDescriptionRequest(String tableName, String columnName,
                                                      String dataType, List<String> sampleValues) {
        String userPrompt = buildColumnDescriptionPrompt(tableName, columnName, dataType, sampleValues);
        return ChatRequest.builder()
                .prompt(userPrompt)
                .messages(List.of(
                        ChatMessage.system(COLUMN_DESCRIPTION_SYSTEM_PROMPT),
                        ChatMessage.user(userPrompt)
                ))
                .temperature(0.3)
                .maxTokens(1000)
                .build();
    }

    public ChatRequest buildQualityAnalysisRequest(String tableName, List<String> issues) {
        String userPrompt = buildDataQualityPrompt(tableName, issues);
        return ChatRequest.builder()
                .prompt(userPrompt)
                .messages(List.of(
                        ChatMessage.system(DATA_QUALITY_SYSTEM_PROMPT),
                        ChatMessage.user(userPrompt)
                ))
                .temperature(0.3)
                .maxTokens(3000)
                .build();
    }

    public ChatRequest buildLineageAnalysisRequest(String tableName, String direction) {
        String userPrompt = buildDataLineagePrompt(tableName, direction);
        return ChatRequest.builder()
                .prompt(userPrompt)
                .messages(List.of(
                        ChatMessage.user(userPrompt)
                ))
                .temperature(0.3)
                .maxTokens(2000)
                .build();
    }

    public ChatRequest buildSQLGenerationRequest(String requirement, List<String> tableInfo) {
        String userPrompt = buildSQLGenerationPrompt(requirement, tableInfo);
        return ChatRequest.builder()
                .prompt(userPrompt)
                .messages(List.of(
                        ChatMessage.system(SQL_GENERATION_SYSTEM_PROMPT),
                        ChatMessage.user(userPrompt)
                ))
                .temperature(0.3)
                .maxTokens(3000)
                .build();
    }

    public String extractJsonFromResponse(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        content = content.trim();

        int startIndex = content.indexOf("{");
        int endIndex = content.lastIndexOf("}");

        if (startIndex >= 0 && endIndex > startIndex) {
            return content.substring(startIndex, endIndex + 1);
        }

        startIndex = content.indexOf("[");
        endIndex = content.lastIndexOf("]");

        if (startIndex >= 0 && endIndex > startIndex) {
            return content.substring(startIndex, endIndex + 1);
        }

        return content;
    }
}
