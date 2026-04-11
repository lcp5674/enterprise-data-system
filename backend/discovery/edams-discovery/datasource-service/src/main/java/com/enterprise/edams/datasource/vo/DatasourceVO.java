package com.enterprise.edams.datasource.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 数据源配置VO（列表展示）
 */
@Data
@SuperBuilder
public class DatasourceVO {

    /**
     * 数据源ID
     */
    private Long id;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源编码
     */
    private String code;

    /**
     * 数据源类型
     */
    private String datasourceType;

    /**
     * 数据源类型描述
     */
    private String datasourceTypeDesc;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 健康状态描述
     */
    private String healthStatusDesc;

    /**
     * 最后测试时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTestTime;

    /**
     * 最后同步时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncTime;

    /**
     * 同步间隔（分钟）
     */
    private Integer syncInterval;

    /**
     * 是否启用同步
     */
    private Boolean syncEnabled;

    /**
     * 目录名称
     */
    private String catalogName;

    /**
     * 描述
     */
    private String description;

    /**
     * 标签
     */
    private String tags;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
