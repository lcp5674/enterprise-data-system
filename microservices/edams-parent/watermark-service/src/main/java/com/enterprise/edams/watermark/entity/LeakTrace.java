package com.enterprise.edams.watermark.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 泄露溯源记录实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("leak_trace")
public class LeakTrace extends BaseEntity {
    
    /**
     * 溯源编号
     */
    @TableField("trace_no")
    private String traceNo;
    
    /**
     * 泄露文件名称
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 泄露类型
     */
    @TableField("leak_type")
    private LeakType leakType;
    
    /**
     * 发现渠道
     */
    @TableField("discovery_channel")
    private String discoveryChannel;
    
    /**
     * 发现时间
     */
    @TableField("discovery_time")
    private LocalDateTime discoveryTime;
    
    /**
     * 溯源的水印记录ID
     */
    @TableField("watermark_record_id")
    private Long watermarkRecordId;
    
    /**
     * 溯源到的用户ID
     */
    @TableField("suspect_user_id")
    private Long suspectUserId;
    
    /**
     * 溯源到的用户名
     */
    @TableField("suspect_user_name")
    private String suspectUserName;
    
    /**
     * 溯源到的部门
     */
    @TableField("suspect_dept")
    private String suspectDept;
    
    /**
     * 泄露时间估算
     */
    @TableField("estimated_leak_time")
    private LocalDateTime estimatedLeakTime;
    
    /**
     * 置信度
     */
    @TableField("confidence")
    private Double confidence;
    
    /**
     * 状态
     */
    @TableField("status")
    private TraceStatus status;
    
    /**
     * 处置结果
     */
    @TableField("disposal_result")
    private String disposalResult;
    
    /**
     * 附件
     */
    @TableField("attachments")
    private String attachments;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

enum LeakType {
    INTERNAL_LEAK("内部泄露"),
    EXTERNAL_LEAK("外部泄露"),
    INTENTIONAL_LEAK("故意泄露"),
    ACCIDENTAL_LEAK("意外泄露"),
    SUSPECTED_LEAK("疑似泄露");
    
    private final String description;
    LeakType(String description) { this.description = description; }
}

enum TraceStatus {
    REPORTED("已上报"),
    INVESTIGATING("调查中"),
    CONFIRMED("已确认"),
    DISPOSED("已处置"),
    CLOSED("已关闭"),
    FALSE_ALARM("误报");
    
    private final String description;
    TraceStatus(String description) { this.description = description; }
}
