package com.enterprise.edams.common.exception;

/**
 * 错误码接口
 *
 * @author Architecture Team
 * @version 1.0.0
 */
public interface ErrorCode {

    // ============ 1xxx - 系统级错误 ============
    int SYSTEM_ERROR = 1000;
    int SERVICE_UNAVAILABLE = 1001;
    int GATEWAY_TIMEOUT = 1002;
    int PARAMETER_INVALID = 1003;
    int SIGNATURE_INVALID = 1004;

    // ============ 2xxx - 认证授权错误 ============
    int UNAUTHORIZED = 2000;
    int TOKEN_INVALID = 2001;
    int TOKEN_EXPIRED = 2002;
    int ACCESS_DENIED = 2003;
    int PERMISSION_DENIED = 2004;

    // ============ 3xxx - 业务数据错误 ============
    int RESOURCE_NOT_FOUND = 3000;
    int RESOURCE_ALREADY_EXISTS = 3001;
    int DATA_CONFLICT = 3002;
    int DATA_INVALID = 3003;
    int DATA_TOO_LARGE = 3004;

    // ============ 4xxx - 资产管理错误 ============
    int ASSET_NOT_FOUND = 4000;
    int ASSET_NAME_DUPLICATE = 4001;
    int ASSET_LOCKED = 4002;
    int ASSET_ARCHIVED = 4003;

    // ============ 5xxx - 治理相关错误 ============
    int LINEAGE_CIRCULAR = 5000;
    int QUALITY_CHECK_FAILED = 5001;
    int STANDARD_NOT_MATCH = 5002;

    // ============ 6xxx - 第三方服务错误 ============
    int DATASOURCE_CONNECTION_FAILED = 6000;
    int MQ_SEND_FAILED = 6001;
    int CACHE_OPERATION_FAILED = 6002;
}
