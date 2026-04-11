package com.enterprise.edams.chatbot.entity;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天上下文实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
public class ChatContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 当前意图
     */
    private String currentIntent;

    /**
     * 当前意图置信度
     */
    private BigDecimal intentConfidence;

    /**
     * 槽位数据
     */
    private Map<String, String> slots;

    /**
     * 对话历史
     */
    private List<ChatMessage> recentMessages;

    /**
     * 上下文变量
     */
    private Map<String, Object> variables;

    /**
     * 用户偏好
     */
    private Map<String, Object> preferences;

    /**
     * 关联的数据资产ID
     */
    private List<Long> relatedAssetIds;

    /**
     * 关联的搜索关键词
     */
    private List<String> searchKeywords;

    /**
     * 上下文创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 上下文更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 获取槽位值
     */
    public String getSlotValue(String slotName) {
        return slots != null ? slots.get(slotName) : null;
    }

    /**
     * 设置槽位值
     */
    public void setSlotValue(String slotName, String value) {
        if (slots == null) {
            slots = new java.util.HashMap<>();
        }
        slots.put(slotName, value);
    }

    /**
     * 获取上下文变量
     */
    public Object getVariable(String key) {
        return variables != null ? variables.get(key) : null;
    }

    /**
     * 设置上下文变量
     */
    public void setVariable(String key, Object value) {
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }
        variables.put(key, value);
    }
}
