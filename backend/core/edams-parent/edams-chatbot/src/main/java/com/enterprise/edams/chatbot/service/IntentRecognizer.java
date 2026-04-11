package com.enterprise.edams.chatbot.service;

import com.enterprise.edams.chatbot.entity.ChatIntent;
import com.enterprise.edams.chatbot.entity.ChatContext;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 意图识别服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface IntentRecognizer {

    /**
     * 识别用户意图
     */
    RecognitionResult recognizeIntent(String userMessage, ChatContext context);

    /**
     * 获取意图详情
     */
    ChatIntent getIntentDetails(String intentType);

    /**
     * 提取槽位
     */
    Map<String, String> extractSlots(String userMessage, String intentType);

    /**
     * 验证槽位是否完整
     */
    boolean validateSlots(String intentType, Map<String, String> slots);

    /**
     * 获取缺失的必填槽位
     */
    Map<String, String> getMissingRequiredSlots(String intentType, Map<String, String> currentSlots);

    /**
     * 更新上下文中的意图
     */
    void updateIntentInContext(ChatContext context, String intentType, BigDecimal confidence);

    /**
     * 获取后续意图
     */
    String getFollowUpIntent(String currentIntent, ChatContext context);

    /**
     * 判断是否为确认请求
     */
    boolean isConfirmation(String userMessage);

    /**
     * 判断是否为取消请求
     */
    boolean isCancellation(String userMessage);

    /**
     * 意图识别结果
     */
    class RecognitionResult {
        private String intentType;
        private BigDecimal confidence;
        private Map<String, String> extractedSlots;
        private String suggestion;

        public RecognitionResult() {}

        public RecognitionResult(String intentType, BigDecimal confidence, Map<String, String> extractedSlots) {
            this.intentType = intentType;
            this.confidence = confidence;
            this.extractedSlots = extractedSlots;
        }

        public String getIntentType() { return intentType; }
        public void setIntentType(String intentType) { this.intentType = intentType; }
        public BigDecimal getConfidence() { return confidence; }
        public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
        public Map<String, String> getExtractedSlots() { return extractedSlots; }
        public void setExtractedSlots(Map<String, String> extractedSlots) { this.extractedSlots = extractedSlots; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
