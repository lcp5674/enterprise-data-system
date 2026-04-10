package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入结果DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class DataImportResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 图谱ID
     */
    private String graphId;

    /**
     * 导入状态: PENDING-待处理, RUNNING-运行中, SUCCESS-成功, FAILED-失败, PARTIAL-部分成功
     */
    private String status;

    /**
     * 总记录数
     */
    private Long totalRecords;

    /**
     * 成功导入数
     */
    private Long successCount;

    /**
     * 失败记录数
     */
    private Long failureCount;

    /**
     * 跳过的记录数
     */
    private Long skippedCount;

    /**
     * 新增节点数
     */
    private Long newNodes;

    /**
     * 更新节点数
     */
    private Long updatedNodes;

    /**
     * 新增边数
     */
    private Long newEdges;

    /**
     * 更新边数
     */
    private Long updatedEdges;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时(ms)
     */
    private Long duration;

    /**
     * 错误列表
     */
    private List<ImportError> errors;

    /**
     * 导入详情
     */
    private String details;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 导入错误信息
     */
    @Data
    @Builder
    public static class ImportError implements Serializable {
        private Integer rowNumber;
        private String errorType;
        private String errorMessage;
        private String recordData;
    }
}
