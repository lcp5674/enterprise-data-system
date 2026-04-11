package com.enterprise.edams.chatbot.service;

import com.enterprise.edams.chatbot.entity.ChatContext;
import com.enterprise.edams.chatbot.entity.ChatMessage;

import java.util.List;
import java.util.Map;

/**
 * 响应生成服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ResponseGenerator {

    /**
     * 生成响应消息
     */
    String generateResponse(String intentType, Map<String, String> slots, ChatContext context);

    /**
     * 生成多轮对话响应
     */
    String generateMultiTurnResponse(String intentType, Map<String, String> slots, ChatContext context, List<String> conversationHistory);

    /**
     * 生成确认请求
     */
    String generateConfirmationRequest(String intentType, Map<String, String> slots);

    /**
     * 生成错误消息
     */
    String generateErrorMessage(String errorType, String details);

    /**
     * 生成帮助消息
     */
    String generateHelpMessage();

    /**
     * 生成FAQ响应
     */
    String generateFaqResponse(String question);

    /**
     * 生成数据查询响应
     */
    String generateDataQueryResponse(String queryType, Map<String, String> params, ChatContext context);

    /**
     * 生成报表响应
     */
    String generateReportResponse(Map<String, Object> reportData, String format);

    /**
     * 生成状态查询响应
     */
    String generateStatusResponse(String targetType, String targetId);

    /**
     * 格式化数据为可读文本
     */
    String formatDataAsText(Object data);

    /**
     * 生成卡片消息
     */
    ChatMessage generateCardMessage(String title, String description, Map<String, String> actions);

    /**
     * 生成后续建议
     */
    List<String> generateFollowUpSuggestions(String intentType, ChatContext context);
}
