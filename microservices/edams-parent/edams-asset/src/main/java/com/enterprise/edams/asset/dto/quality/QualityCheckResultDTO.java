package com.enterprise.edams.asset.dto.quality;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 质量检查结果DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class QualityCheckResultDTO {

    /**
     * 检查ID
     */
    private String checkId;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 检查类型
     */
    private String checkType;

    /**
     * 检查状态: RUNNING, COMPLETED, FAILED
     */
    private String status;

    /**
     * 总体得分
     */
    private Double overallScore;

    /**
     * 通过的规则数
     */
    private Integer passedRules;

    /**
     * 失败的规则数
     */
    private Integer failedRules;

    /**
     * 执行的规则列表
     */
    private List<RuleResult> ruleResults;

    /**
     * 检查开始时间
     */
    private LocalDateTime startTime;

    /**
     * 检查结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时(秒)
     */
    private Long durationSeconds;

    /**
     * 告警是否已触发
     */
    private Boolean alertTriggered;

    /**
     * 执行人
     */
    private String executedBy;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 附加信息
     */
    private Map<String, Object> metadata;

    /**
     * 规则执行结果
     */
    @Data
    public static class RuleResult {
        private String ruleId;
        private String ruleName;
        private String ruleType;
        private String status;
        private Double score;
        private String errorMessage;
        private Object result;
        private LocalDateTime executionTime;
    }
}
