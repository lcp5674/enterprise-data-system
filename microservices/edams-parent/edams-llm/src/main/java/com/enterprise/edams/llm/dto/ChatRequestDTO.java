package com.enterprise.edams.llm.dto;

import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 聊天请求DTO
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Data
@Builder
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 历史消息
     */
    private List<ChatMessage> history;

    /**
     * 模型选择策略: AUTO, COST_OPTIMIZED, LATENCY_OPTIMIZED, QUALITY_FIRST
     */
    @Builder.Default
    private String strategy = "AUTO";

    /**
     * 指定模型
     */
    private String model;

    /**
     * 指定提供商
     */
    private String provider;

    /**
     * 温度参数 (0-2)
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 顶部概率截断
     */
    private Double topP;

    /**
     * 频率惩罚
     */
    private Double frequencyPenalty;

    /**
     * 存在惩罚
     */
    private Double presencePenalty;

    /**
     * 停止词
     */
    private List<String> stop;

    /**
     * 系统提示
     */
    private String systemPrompt;

    /**
     * 工具定义
     */
    private List<ToolDefinition> tools;

    /**
     * 工具选择
     */
    private String toolChoice;

    /**
     * 是否流式输出
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 扩展参数
     */
    private Map<String, Object> extraParams;

    /**
     * 聊天消息
     */
    @Data
    @Builder
    public static class ChatMessage implements Serializable {
        private String role;      // user, assistant, system
        private String content;
        private String name;
    }

    /**
     * 工具定义
     */
    @Data
    @Builder
    public static class ToolDefinition implements Serializable {
        private String type;     // function
        private Function function;

        @Data
        @Builder
        public static class Function implements Serializable {
            private String name;
            private String description;
            private Map<String, Object> parameters;
        }
    }
}
