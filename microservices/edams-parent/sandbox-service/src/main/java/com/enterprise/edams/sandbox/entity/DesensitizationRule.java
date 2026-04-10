package com.enterprise.edams.sandbox.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 脱敏规则实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("desensitization_rule")
public class DesensitizationRule extends BaseEntity {
    
    /**
     * 规则编码
     */
    @TableField("rule_code")
    private String ruleCode;
    
    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;
    
    /**
     * 数据类型
     */
    @TableField("data_type")
    private DataType dataType;
    
    /**
     * 脱敏方式
     */
    @TableField("method")
    private DesensitizationMethod method;
    
    /**
     * 脱敏参数(JSON)
     */
    @TableField("params")
    private String params;
    
    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;
    
    /**
     * 状态
     */
    @TableField("status")
    private RuleStatus status;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
}

enum DataType {
    NAME("姓名"),
    PHONE("手机号"),
    ID_CARD("身份证"),
    EMAIL("邮箱"),
    BANK_CARD("银行卡"),
    ADDRESS("地址"),
    COMPANY("公司名"),
    CUSTOM("自定义");
    
    private final String description;
    DataType(String description) { this.description = description; }
}

enum DesensitizationMethod {
    MASK("掩码"),
    HASH("哈希"),
    REPLACE("替换"),
    TRUNCATE("截断"),
    SHUFFLE("打乱"),
    DIFFERENTIAL("差分隐私"),
    FORMAT_PRESERVING("格式保持加密");
    
    private final String description;
    DesensitizationMethod(String description) { this.description = description; }
}

enum RuleStatus {
    ACTIVE("生效中"),
    INACTIVE("已停用");
    
    private final String description;
    RuleStatus(String description) { this.description = description; }
}
