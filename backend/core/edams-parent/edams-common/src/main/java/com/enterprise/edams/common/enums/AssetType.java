package com.enterprise.edams.common.enums;

/**
 * 资产类型枚举
 *
 * @author Architecture Team
 * @version 1.0.0
 */
public enum AssetType {
    /**
     * 数据库
     */
    DATABASE("DATABASE", "数据库"),

    /**
     * 数据表
     */
    TABLE("TABLE", "数据表"),

    /**
     * 视图
     */
    VIEW("VIEW", "视图"),

    /**
     * 字段
     */
    COLUMN("COLUMN", "字段"),

    /**
     * 文件
     */
    FILE("FILE", "文件"),

    /**
     * API接口
     */
    API("API", "API接口"),

    /**
     * 数据流
     */
    DATAFLOW("DATAFLOW", "数据流"),

    /**
     * ETL任务
     */
    ETL("ETL", "ETL任务"),

    /**
     * 报表
     */
    REPORT("REPORT", "报表"),

    /**
     * 指标
     */
    METRIC("METRIC", "指标"),

    /**
     * 标签
     */
    TAG("TAG", "标签");

    private final String code;
    private final String description;

    AssetType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AssetType fromCode(String code) {
        for (AssetType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
