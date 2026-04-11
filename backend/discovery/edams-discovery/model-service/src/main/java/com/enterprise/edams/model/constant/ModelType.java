package com.enterprise.edams.model.constant;

/**
 * 模型类型枚举
 */
public enum ModelType {

    CONCEPTUAL("CONCEPTUAL", "概念模型", "用于业务描述的高层数据模型"),
    LOGICAL("LOGICAL", "逻辑模型", "与业务需求对应的逻辑结构模型"),
    PHYSICAL("PHYSICAL", "物理模型", "针对特定数据库的物理实现模型");

    private final String code;
    private final String description;
    private final String remark;

    ModelType(String code, String description, String remark) {
        this.code = code;
        this.description = description;
        this.remark = remark;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getRemark() {
        return remark;
    }
}
