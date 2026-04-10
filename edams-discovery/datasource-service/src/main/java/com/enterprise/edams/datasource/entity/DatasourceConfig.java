package com.enterprise.edams.datasource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 数据源配置实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("datasource_config")
public class DatasourceConfig extends BaseEntity {

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源编码
     */
    private String code;

    /**
     * 数据源类型：MYSQL/ORACLE/POSTGRESQL/MONGODB/KAFKA/HIVE/SPARK/MAXCOMPUTE/S3/REST_API
     */
    private String datasourceType;

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
     * 用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String passwordEnc;

    /**
     * JDBC连接URL
     */
    private String connectionUrl;

    /**
     * 扩展属性(JSON)
     */
    private String properties;

    /**
     * JDBC参数(JSON)
     */
    private String jdbcParams;

    /**
     * HTTP请求头(JSON)
     */
    private String httpHeaders;

    /**
     * 认证类型：NONE/BASIC/KERBEROS/AKS/VAULT
     */
    private String authType;

    /**
     * 状态：ACTIVE/INACTIVE
     */
    private String status;

    /**
     * 健康状态：HEALTHY/UNHEALTHY/UNKNOWN
     */
    private String healthStatus;

    /**
     * 最后测试时间
     */
    private LocalDateTime lastTestTime;

    /**
     * 最后测试结果(JSON)
     */
    private String lastTestResult;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 同步间隔（分钟）
     */
    private Integer syncInterval;

    /**
     * 是否启用同步
     */
    private Integer syncEnabled;

    /**
     * 目录名称
     */
    private String catalogName;

    /**
     * 描述
     */
    private String description;

    /**
     * 标签(JSON)
     */
    private String tags;
}
