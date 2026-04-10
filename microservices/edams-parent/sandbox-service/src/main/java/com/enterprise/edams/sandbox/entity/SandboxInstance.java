package com.enterprise.edams.sandbox.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * 沙箱实例实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sandbox_instance")
public class SandboxInstance extends BaseEntity {
    
    /**
     * 实例编码
     */
    @TableField("instance_code")
    private String instanceCode;
    
    /**
     * 实例名称
     */
    @TableField("instance_name")
    private String instanceName;
    
    /**
     * 沙箱类型
     */
    @TableField("sandbox_type")
    private SandboxType sandboxType;
    
    /**
     * 状态
     */
    @TableField("status")
    private InstanceStatus status;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 用户名称
     */
    @TableField("user_name")
    private String userName;
    
    /**
     * 关联资产ID
     */
    @TableField("asset_ids")
    private String assetIds;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    
    /**
     * 实际删除时间
     */
    @TableField("deleted_time")
    private LocalDateTime deletedTime;
    
    /**
     * 资源配置
     */
    @TableField("resource_config")
    private String resourceConfig;
}

enum SandboxType {
    DATA_ANALYSIS("数据分析"),
    SCRIPT_TEST("脚本测试"),
    MODEL_TRAINING("模型训练"),
    REPORT_GENERATION("报表生成"),
    ETL_TEST("ETL测试");
    
    private final String description;
    SandboxType(String description) { this.description = description; }
}

enum InstanceStatus {
    CREATING("创建中"),
    RUNNING("运行中"),
    STOPPED("已停止"),
    EXPIRED("已过期"),
    DELETED("已删除");
    
    private final String description;
    InstanceStatus(String description) { this.description = description; }
}
