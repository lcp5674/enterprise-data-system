package com.enterprise.edams.datasource.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 数据源连接测试请求DTO
 */
@Data
@SuperBuilder
public class ConnectionTestRequest {

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
     * 密码
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
}
