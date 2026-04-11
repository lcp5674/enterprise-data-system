package com.enterprise.edams.chatbot.service.impl;

import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatMessage;
import com.enterprise.edams.chatbot.service.ResponseGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 响应生成服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseGeneratorImpl implements ResponseGenerator {

    @Override
    public String generateResponse(String intentType, Map<String, String> slots, ChatContext context) {
        if (slots == null) slots = new HashMap<>();
        
        return switch (intentType) {
            case "QUERY_METADATA" -> generateMetadataResponse(slots);
            case "QUERY_QUALITY" -> generateQualityResponse(slots);
            case "QUERY_LINEAGE" -> generateLineageResponse(slots);
            case "GENERATE_REPORT" -> generateReportResponseText(slots);
            case "CHECK_STATUS" -> generateStatusResponseText(slots);
            case "FAQ" -> generateFaqResponseText(slots);
            default -> generateDefaultResponse();
        };
    }

    @Override
    public String generateMultiTurnResponse(String intentType, Map<String, String> slots, ChatContext context, List<String> conversationHistory) {
        String response = generateResponse(intentType, slots, context);
        
        // 检查是否需要追问槽位
        Map<String, String> missingSlots = getMissingSlots(intentType, slots);
        if (!missingSlots.isEmpty()) {
            StringBuilder clarification = new StringBuilder(response);
            clarification.append("\n\n请问：");
            for (Map.Entry<String, String> entry : missingSlots.entrySet()) {
                clarification.append(entry.getValue()).append(" ");
            }
            return clarification.toString();
        }
        
        return response;
    }

    @Override
    public String generateConfirmationRequest(String intentType, Map<String, String> slots) {
        StringBuilder sb = new StringBuilder("您想要执行以下操作：\n");
        sb.append("- 操作类型：").append(intentType).append("\n");
        if (slots != null) {
            for (Map.Entry<String, String> entry : slots.entrySet()) {
                sb.append("- ").append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
        }
        sb.append("\n请确认是否正确？（是/否）");
        return sb.toString();
    }

    @Override
    public String generateErrorMessage(String errorType, String details) {
        return switch (errorType) {
            case "intent_not_found" -> "抱歉，我无法理解您的意图。请尝试换一种表述方式。";
            case "slot_missing" -> "请提供" + details + "信息。";
            case "service_error" -> "服务暂时不可用，请稍后再试。错误详情：" + details;
            case "data_not_found" -> "未找到相关数据：" + details;
            default -> "处理您的请求时出现问题，请重试或联系管理员。";
        };
    }

    @Override
    public String generateHelpMessage() {
        return """
            我是EDAMS智能助手，可以帮您：

            1. 查询元数据 - "查询某张表的结构"
            2. 查询数据质量 - "查看某资产的质量评分"
            3. 查询数据血缘 - "查看某表的上游数据"
            4. 生成报表 - "生成质量报告"
            5. 检查状态 - "查看系统状态"

            您可以直接输入您的需求，我会尽力帮助您！
            """;
    }

    @Override
    public String generateFaqResponse(String question) {
        question = question.toLowerCase();
        
        if (question.contains("如何") || question.contains("怎么")) {
            return "您可以通过以下方式操作：\n1. 在资产列表中找到目标资产\n2. 点击查看详情\n3. 进行相应的管理操作";
        }
        
        if (question.contains("质量")) {
            return "数据质量包括完整性、准确性、一致性、时效性等维度。您可以在质量模块查看和管理数据质量规则。";
        }
        
        if (question.contains("血缘")) {
            return "数据血缘展示了数据从源头到消费端的流转路径，帮助您追踪数据来源和影响分析。";
        }
        
        return "抱歉，我暂时无法回答这个问题。您可以尝试换一种问法，或联系管理员获取帮助。";
    }

    @Override
    public String generateDataQueryResponse(String queryType, Map<String, String> params, ChatContext context) {
        return generateResponse(queryType, params, context);
    }

    @Override
    public String generateReportResponse(Map<String, Object> reportData, String format) {
        StringBuilder sb = new StringBuilder("报表生成完成！\n\n");
        sb.append("报表类型：").append(reportData.getOrDefault("type", "未知")).append("\n");
        sb.append("生成时间：").append(reportData.getOrDefault("timestamp", "未知")).append("\n");
        sb.append("数据记录：").append(reportData.getOrDefault("recordCount", 0)).append("条\n\n");
        
        if (reportData.containsKey("summary")) {
            sb.append("摘要信息：\n").append(reportData.get("summary")).append("\n");
        }
        
        sb.append("\n您可以使用导出功能下载详细报表。");
        return sb.toString();
    }

    @Override
    public String generateStatusResponse(String targetType, String targetId) {
        return String.format("正在查询%s[%s]的状态信息...", targetType != null ? targetType : "系统", targetId != null ? targetId : "全部");
    }

    @Override
    public String formatDataAsText(Object data) {
        if (data == null) return "无数据";
        
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            if (list.isEmpty()) return "暂无数据";
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(list.size(), 10); i++) {
                sb.append(i + 1).append(". ").append(list.get(i)).append("\n");
            }
            if (list.size() > 10) {
                sb.append("... 共").append(list.size()).append("条记录");
            }
            return sb.toString();
        }
        
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            StringBuilder sb = new StringBuilder();
            map.forEach((k, v) -> sb.append(k).append("：").append(v).append("\n"));
            return sb.toString();
        }
        
        return data.toString();
    }

    @Override
    public ChatMessage generateCardMessage(String title, String description, Map<String, String> actions) {
        return ChatMessage.builder()
                .role("assistant")
                .content(title + "\n\n" + description)
                .messageType("card")
                .metadata(actions != null ? new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(actions).toString() : null)
                .messageTime(new java.time.LocalDateTime())
                .build();
    }

    @Override
    public List<String> generateFollowUpSuggestions(String intentType, ChatContext context) {
        List<String> suggestions = new ArrayList<>();
        
        switch (intentType) {
            case "QUERY_METADATA":
                suggestions.add("查看该资产的详细字段信息");
                suggestions.add("查看该资产的质量评分");
                suggestions.add("查看该资产的上下游血缘");
                break;
            case "QUERY_QUALITY":
                suggestions.add("查看质量问题详情");
                suggestions.add("生成质量报告");
                suggestions.add("配置质量规则");
                break;
            case "QUERY_LINEAGE":
                suggestions.add("查看影响分析");
                suggestions.add("追踪数据源头");
                suggestions.add("可视化血缘图谱");
                break;
            case "GENERATE_REPORT":
                suggestions.add("导出为Excel");
                suggestions.add("导出为PDF");
                suggestions.add("定时生成报表");
                break;
            default:
                suggestions.add("还有什么可以帮您？");
                suggestions.add("查看帮助信息");
        }
        
        return suggestions;
    }

    // ==================== 私有辅助方法 ====================

    private String generateMetadataResponse(Map<String, String> slots) {
        String assetName = slots.getOrDefault("assetName", "未知资产");
        return String.format("""
            查询到资产[%s]的元数据信息：

            资产名称：%s
            资产类型：数据表
            所属数据库：edams_prod
            字段数量：25
            数据量：1.2GB
            最后更新：2026-04-11 10:00

            如需查看详细字段信息，请告诉我。
            """, assetName, assetName);
    }

    private String generateQualityResponse(Map<String, String> slots) {
        String assetName = slots.getOrDefault("assetName", "未知资产");
        String dimension = slots.getOrDefault("qualityDimension", "综合评分");
        return String.format("""
            资产[%s]的质量评分：

            综合评分：85分（良好）
            - 完整性：92%% ✓
            - 准确性：88%% ✓
            - 一致性：80%% ⚠
            - 时效性：85%% ✓

            主要问题：存在少量数据不一致情况
            """, assetName);
    }

    private String generateLineageResponse(Map<String, String> slots) {
        String assetName = slots.getOrDefault("assetName", "未知资产");
        return String.format("""
            资产[%s]的数据血缘：

            📥 上游数据源（3个）：
            - source_db.user_orders
            - source_db.user_profile
            - etl.order_aggregation

            📤 下游数据（5个）：
            - dw.dwd_user_orders
            - dw.dws_daily_summary
            - report.order_daily
            """, assetName);
    }

    private String generateReportResponseText(Map<String, String> slots) {
        String reportType = slots.getOrDefault("reportType", "综合");
        return String.format("正在为您生成[%s]报表，请稍候...", reportType);
    }

    private String generateStatusResponseText(Map<String, String> slots) {
        return """
            📊 系统状态总览：

            服务状态：正常 ✓
            - API服务：运行中
            - 数据库服务：正常
            - 缓存服务：正常
            - 消息队列：正常

            今日数据：
            - 新增资产：12个
            - 活跃用户：56人
            - 处理任务：328个
            """;
    }

    private String generateFaqResponseText(Map<String, String> slots) {
        return "这是一个常见问题。如需更多帮助，请输入「帮助」查看支持的功能。";
    }

    private String generateDefaultResponse() {
        return "我理解了您的问题，正在处理中...";
    }

    private Map<String, String> getMissingSlots(String intentType, Map<String, String> currentSlots) {
        Map<String, String> missing = new HashMap<>();
        
        if (currentSlots == null) {
            switch (intentType) {
                case "QUERY_METADATA" -> missing.put("assetName", "资产名称");
                case "QUERY_QUALITY" -> missing.put("assetName", "资产名称");
                case "GENERATE_REPORT" -> missing.put("reportType", "报表类型");
            }
        }
        
        return missing;
    }
}
