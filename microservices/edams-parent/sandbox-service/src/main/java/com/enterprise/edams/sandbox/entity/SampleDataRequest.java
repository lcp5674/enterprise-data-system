package com.enterprise.edams.sandbox.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 样本数据请求实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sample_data_request")
public class SampleDataRequest extends BaseEntity {
    
    /**
     * 请求编号
     */
    @TableField("request_no")
    private String requestNo;
    
    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * 资产名称
     */
    @TableField("asset_name")
    private String assetName;
    
    /**
    /**
     * 请求用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 请求用户名
     */
    @TableField("user_name")
    private String userName;
    
    /**
     * 样本类型
     */
    @TableField("sample_type")
    private SampleType sampleType;
    
    /**
     * 样本数量
     */
    @TableField("sample_count")
    private Integer sampleCount;
    
    /**
     * 数据大小限制(MB)
     */
    @TableField("size_limit")
    private BigDecimal sizeLimit;
    
    /**
     * 脱敏规则ID
     */
    @TableField("desensitization_rule_id")
    private Long desensitizationRuleId;
    
    /**
     * 用途说明
     */
    @TableField("purpose")
    private String purpose;
    
    /**
     * 状态
     */
    @TableField("status")
    private SampleRequestStatus status;
    
    /**
     * 处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;
    
    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;
    
    /**
     * 下载地址
     */
    @TableField("download_url")
    private String downloadUrl;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

enum SampleType {
    RANDOM("随机抽样"),
    STRATIFIED("分层抽样"),
    TIME_SERIES("时序抽样"),
    CONDITIONAL("条件抽样");
    
    private final String description;
    SampleType(String description) { this.description = description; }
}

enum SampleRequestStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    COMPLETED("已完成"),
    REJECTED("已拒绝"),
    EXPIRED("已过期");
    
    private final String description;
    SampleRequestStatus(String description) { this.description = description; }
}
