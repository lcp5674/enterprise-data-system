package com.enterprise.edams.datasource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接测试响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResponse {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 连接信息
     */
    private ConnectionInfo connectionInfo;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 连接信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionInfo {
        /**
         * 服务器版本
         */
        private String serverVersion;
        
        /**
         * 数据库名称
         */
        private String databaseName;
        
        /**
         * 连接超时时间
         */
        private Integer connectionTimeout;
    }
}
