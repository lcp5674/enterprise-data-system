package com.enterprise.dataplatform.ethics.domain.enums;

public enum ReviewStatus {
    DRAFT("DRAFT", "草稿"),
    PENDING("PENDING", "待审核"),
    IN_REVIEW("IN_REVIEW", "审核中"),
    APPROVED("APPROVED", "已批准"),
    REJECTED("REJECTED", "已拒绝"),
    REVISION_REQUESTED("REVISION_REQUESTED", "需要修改"),
    EXECUTED("EXECUTED", "已执行"),
    EXPIRED("EXPIRED", "已过期"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    ReviewStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ReviewStatus fromCode(String code) {
        for (ReviewStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewStatus code: " + code);
    }
}
