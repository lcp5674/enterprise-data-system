package com.enterprise.edams.analysis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PromptTemplateManager {

    public String getDefaultTableAnalysisTemplate() {
        return """
                你是一个专业的数据分析师。请分析以下数据库表的业务含义：

                ## 表信息
                - 表名：{tableName}
                - Schema：{schema}
                - 表注释：{tableComment}

                ## 字段定义
                {columnDefinitions}

                ## 分析要求
                1. 用简洁的中文描述这张表的业务含义（50字以内）
                2. 为每个字段提供业务注释，说明其业务含义
                3. 判断表的类型（FACT=事实表/DIMENSION=维度表/ENUM=枚举表/INTERMEDIATE=中间表/TEMP=临时表）

                ## 输出格式（JSON）
                {
                  "tableDescription": "表的业务含义描述",
                  "tableAlias": "表简称",
                  "tableCategory": "FACT|DIMENSION|ENUM|INTERMEDIATE|TEMP",
                  "fieldDescriptions": {
                    "字段名": "业务含义注释"
                  },
                  "primaryKey": "主键字段（无则填null）",
                  "confidence": 0.95
                }
                """;
    }

    public String getDefaultLineageAnalysisTemplate() {
        return """
                你是一个专业的数据架构师。请分析以下表之间的血缘关系：

                ## 表信息
                - 表名：{tableName}
                - Schema：{schema}

                ## 字段定义
                {columnsInfo}

                ## 分析要求
                1. 基于字段命名和数据含义，识别可能的ETL转换逻辑（如SUM、AVG、COUNT等聚合操作）
                2. 识别可能的表间引用关系
                3. 不要编造不存在的血缘关系

                ## 输出格式（JSON）
                {
                  "lineageRelations": [
                    {
                      "sourceTable": "源表名",
                      "sourceField": "源字段",
                      "targetTable": "目标表名",
                      "targetField": "目标字段",
                      "lineageType": "DIRECT|AGGREGATED|TRANSFORMED",
                      "transformation": "转换描述（如有）",
                      "confidence": 0.90
                    }
                  ],
                  "confidence": 0.88
                }
                """;
    }

    public String getDefaultIndicatorExtractionTemplate() {
        return """
                你是一个专业的数据产品经理。请从以下表中提取潜在的数据指标：

                ## 表信息
                - 表名：{tableName}
                - 表描述：{tableDescription}

                ## 字段定义
                {columnDefinitions}

                ## 分析要求
                1. 识别可作为指标的数值字段（如金额、数量、次数、比率等）
                2. 识别可用于统计的维度字段（如时间、地区、渠道等）
                3. 建议可能的指标定义（指标编码使用英文下划线格式）

                ## 输出格式（JSON）
                {
                  "indicators": [
                    {
                      "indicatorName": "指标中文名称",
                      "indicatorCode": "indicator_code",
                      "indicatorType": "ATOMIC|DERIVED|CALCULATED",
                      "description": "指标描述",
                      "formula": "指标公式",
                      "unit": "指标单位",
                      "dimensions": ["维度列表"],
                      "aggregationType": "SUM|COUNT|AVG|MAX|MIN|DISTINCT",
                      "dataType": "数据类型",
                      "confidence": 0.92
                    }
                  ]
                }
                """;
    }

    public String getDefaultSubjectClassificationTemplate() {
        return """
                你是一个专业的数据治理专家。请为以下表进行主题域分类：

                ## 表信息
                - 表名：{tableName}
                - 表描述：{tableDescription}

                ## 字段列表
                {fieldList}

                ## 常用主题域参考
                - 客户主题（Customer）
                - 交易主题（Transaction）
                - 产品主题（Product）
                - 营销主题（Marketing）
                - 财务主题（Finance）
                - 运营主题（Operation）
                - 库存主题（Inventory）
                - 供应商主题（Supplier）

                ## 分析要求
                1. 根据表名和字段内容，判断其所属主题域
                2. 识别业务域和数据域
                3. 确定指标层级（ATOMIC=原子指标/DERIVED=派生指标/CALCULATED=计算指标）

                ## 输出格式（JSON）
                {
                  "subjectDomain": "所属主题域",
                  "businessDomain": "业务域",
                  "dataDomain": "数据域",
                  "indicatorLayer": "ATOMIC|DERIVED|CALCULATED",
                  "reasoning": "分类依据"
                }
                """;
    }
}
