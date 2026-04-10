package com.enterprise.edams.datasource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 创建数据源配置请求DTO
 */
@Data
@SuperBuilder
public class CreateDatasourceRequest {

    /**
     * 数据源名称
     */
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    /**
     * 数据源编码
     */
    @NotBlank(message = "数据源编码不能为空")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "数据源编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    private String code;

    /**
     * 数据源类型
     */
    @NotBlank(message = "数据源类型不能为空")
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
     * 密码（创建时会加密存储）
     */
    private String password;

    /**
     * JDBC连接URL
     */
    private String connectionUrl;

    /**
     * 扩展属性
     */
    private Map<String, Object> properties;

    /**
     * JDBC参数
     */
    private Map<String, String> jdbcParams;

    /**
     * HTTP请求头
     */
    private Map<String, String> httpHeaders;

    /**
     * 认证类型
     */
    private String authType;

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
}
