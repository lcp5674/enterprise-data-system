package com.enterprise.edams.common.result;

import lombok.Getter;

/**
 * 结果码枚举
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(0, "success"),

    // 认证授权错误 10001-10099
    UNAUTHORIZED(10001, "未认证或Token无效"),
    TOKEN_INVALID(10002, "Token无效"),
    TOKEN_EXPIRED(10003, "Token已过期"),
    NO_PERMISSION(10004, "无权限访问"),
    USER_DISABLED(10005, "用户已被禁用"),
    USER_LOCKED(10006, "用户已被锁定"),
    INVALID_CREDENTIALS(10007, "用户名或密码错误"),
    INVALID_VERIFY_CODE(10008, "验证码错误"),
    TOO_MANY_ATTEMPTS(10009, "登录尝试次数过多，请稍后重试"),

    // 访问控制错误 10101-10199
    IP_RESTRICTED(10101, "IP受限"),
    TIME_RESTRICTED(10102, "时间受限"),

    // 资源操作错误 20001-20099
    RESOURCE_NOT_FOUND(20001, "资源不存在"),
    RESOURCE_ALREADY_EXISTS(20002, "资源已存在"),
    RESOURCE_OCCUPIED(20003, "资源被占用"),

    // 资源状态错误 20101-20199
    STATUS_NOT_ALLOWED(20101, "状态不允许"),
    RESOURCE_LOCKED(20102, "资源已锁定"),

    // 参数校验错误 30001-30099
    PARAM_INVALID(30001, "参数格式错误"),
    PARAM_MISSING(30002, "必填参数缺失"),
    PARAM_OUT_OF_RANGE(30003, "参数值超出范围"),

    // 业务逻辑错误 40001-40099
    BUSINESS_RULE_VIOLATION(40001, "业务规则冲突"),
    PRECONDITION_FAILED(40002, "前置条件不满足"),
    OPERATION_NOT_ALLOWED(40003, "当前状态不允许操作"),

    // 流程审批错误 40101-40199
    APPROVAL_NOT_PASSED(40101, "审批未通过"),
    APPROVAL_TIMEOUT(40102, "审批超时"),

    // 系统内部错误 50001-50099
    SERVICE_UNAVAILABLE(50001, "服务暂不可用"),
    SYSTEM_BUSY(50002, "系统繁忙，请稍后重试"),
    DATA_ERROR(50003, "数据异常"),

    // 第三方服务错误 50101-50199
    EXTERNAL_SERVICE_TIMEOUT(50101, "外部服务超时"),
    EXTERNAL_SERVICE_UNAVAILABLE(50102, "外部服务不可用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
