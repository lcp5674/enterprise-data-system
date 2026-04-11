package com.enterprise.edams.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatIntent;
import com.enterprise.edams.chatbot.repository.ChatIntentMapper;
import com.enterprise.edams.chatbot.service.IntentRecognizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 意图识别服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognizerImpl implements IntentRecognizer {

    private final ChatIntentMapper intentMapper;

    // 意图关键词映射
    private static final Map<String, List<String>> INTENT_KEYWORDS = new HashMap<>();
    
    static {
        INTENT_KEYWORDS.put("QUERY_METADATA", Arrays.asList("元数据", "表结构", "字段", "属性", "metadata", "schema", "column"));
        INTENT_KEYWORDS.put("QUERY_QUALITY", Arrays.asList("质量", "完整性", "准确性", "质量评分", "quality", "completeness"));
        INTENT_KEYWORDS.put("QUERY_LINEAGE", Arrays.asList("血缘", "上下游", "数据流", "lineage", "dependency"));
        INTENT_KEYWORDS.put("GENERATE_REPORT", Arrays.asList("报表", "报告", "导出", "生成", "report", "export"));
        INTENT_KEYWORDS.put("CHECK_STATUS", Arrays.asList("状态", "健康", "监控", "status", "health", "monitor"));
        INTENT_KEYWORDS.put("FAQ", Arrays.asList("如何", "怎么", "帮助", "使用", "what", "how", "help"));
    }

    @Override
    public RecognitionResult recognizeIntent(String userMessage, ChatContext context) {
        userMessage = userMessage.toLowerCase();
        
        String bestIntent = null;
        BigDecimal bestConfidence = BigDecimal.ZERO;
        Map<String, String> extractedSlots = new HashMap<>();
        
        // 获取启用的意图列表
        List<ChatIntent> enabledIntents = intentMapper.findAllEnabled();
        
        // 关键词匹配
        for (Map.Entry<String, List<String>> entry : INTENT_KEYWORDS.entrySet()) {
            String intentType = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int matchCount = 0;
            for (String keyword : keywords) {
                if (userMessage.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            
            if (matchCount > 0) {
                BigDecimal confidence = BigDecimal.valueOf(matchCount * 0.2).min(BigDecimal.ONE);
                if (confidence.compareTo(bestConfidence) > 0) {
                    bestConfidence = confidence;
                    bestIntent = intentType;
                }
            }
        }
        
        // 默认意图
        if (bestIntent == null) {
            bestIntent = "FAQ";
            bestConfidence = BigDecimal.valueOf(0.5);
        }
        
        // 提取槽位
        extractedSlots = extractSlots(userMessage, bestIntent);
        
        log.debug("意图识别结果: intent={}, confidence={}", bestIntent, bestConfidence);
        
        RecognitionResult result = new RecognitionResult(bestIntent, bestConfidence, extractedSlots);
        return result;
    }

    @Override
    public ChatIntent getIntentDetails(String intentType) {
        List<ChatIntent> intents = intentMapper.findByIntentType(intentType);
        return intents.isEmpty() ? null : intents.get(0);
    }

    @Override
    public Map<String, String> extractSlots(String userMessage, String intentType) {
        Map<String, String> slots = new HashMap<>();
        
        // 提取资产名称
        Pattern assetPattern = Pattern.compile("(资产|表|数据集)(?:名称|叫)?(.+?)(?:的|是|吗|$)");
        Matcher assetMatcher = assetPattern.matcher(userMessage);
        if (assetMatcher.find()) {
            slots.put("assetName", assetMatcher.group(2).trim());
        }
        
        // 提取时间范围
        Pattern timePattern = Pattern.compile("(最近|昨天|今天|本周|本月)?(.+?)(天|小时|分钟|周|月)前");
        Matcher timeMatcher = timePattern.matcher(userMessage);
        if (timeMatcher.find()) {
            slots.put("timeRange", timeMatcher.group(0));
            slots.put("timeUnit", timeMatcher.group(3));
        }
        
        // 提取质量维度
        if (userMessage.contains("完整性")) slots.put("qualityDimension", "completeness");
        if (userMessage.contains("准确性")) slots.put("qualityDimension", "accuracy");
        if (userMessage.contains("一致性")) slots.put("qualityDimension", "consistency");
        if (userMessage.contains("时效性")) slots.put("qualityDimension", "timeliness");
        
        // 提取报表类型
        if (userMessage.contains("质量报告")) slots.put("reportType", "quality");
        if (userMessage.contains("资产报告")) slots.put("reportType", "asset");
        if (userMessage.contains("血缘报告")) slots.put("reportType", "lineage");
        
        return slots;
    }

    @Override
    public boolean validateSlots(String intentType, Map<String, String> slots) {
        if (slots == null || slots.isEmpty()) {
            return false;
        }
        
        // 简单验证：至少有一个槽位
        return !slots.isEmpty();
    }

    @Override
    public Map<String, String> getMissingRequiredSlots(String intentType, Map<String, String> currentSlots) {
        Map<String, String> missing = new HashMap<>();
        
        switch (intentType) {
            case "QUERY_METADATA":
                if (!currentSlots.containsKey("assetName")) {
                    missing.put("assetName", "请提供资产名称或表名");
                }
                break;
            case "GENERATE_REPORT":
                if (!currentSlots.containsKey("reportType")) {
                    missing.put("reportType", "请提供报表类型(质量/资产/血缘)");
                }
                break;
            case "QUERY_QUALITY":
                if (!currentSlots.containsKey("assetName")) {
                    missing.put("assetName", "请提供资产名称");
                }
                break;
        }
        
        return missing;
    }

    @Override
    public void updateIntentInContext(ChatContext context, String intentType, BigDecimal confidence) {
        context.setCurrentIntent(intentType);
        context.setIntentConfidence(confidence);
    }

    @Override
    public String getFollowUpIntent(String currentIntent, ChatContext context) {
        ChatIntent intent = getIntentDetails(currentIntent);
        if (intent != null && intent.getFollowUpIntents() != null) {
            String[] followUps = intent.getFollowUpIntents().split(",");
            if (followUps.length > 0) {
                return followUps[0].trim();
            }
        }
        return "FAQ";
    }

    @Override
    public boolean isConfirmation(String userMessage) {
        String msg = userMessage.toLowerCase();
        return msg.contains("是") || msg.contains("对") || msg.contains("确认") || 
               msg.equals("y") || msg.equals("yes") || msg.equals("ok");
    }

    @Override
    public boolean isCancellation(String userMessage) {
        String msg = userMessage.toLowerCase();
        return msg.contains("取消") || msg.contains("不要") || msg.contains("算了") ||
               msg.equals("n") || msg.equals("no");
    }
}
