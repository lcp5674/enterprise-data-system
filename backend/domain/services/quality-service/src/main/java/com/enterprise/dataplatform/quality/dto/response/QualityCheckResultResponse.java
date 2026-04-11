package com.enterprise.dataplatform.quality.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 质量检查结果响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckResultResponse {

    private Long id;
    private String batchNo;
    private Long taskId;
    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String assetId;
    private String assetName;
    private String checkStatus;
    private String checkResult;
    private Double qualityScore;
    private Long totalRecords;
    private Long checkedRecords;
    private Long passedRecords;
    private Long failedRecords;
    private Double violationRate;
    private Double threshold;
    private Boolean exceedsThreshold;
    private LocalDateTime checkStartTime;
    private LocalDateTime checkEndTime;
    private Long executionTimeMs;
    private String executor;
    private LocalDateTime createTime;
    private LocalDateTime checkTime;
}
