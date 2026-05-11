package com.enterprise.edams.analysis.entity;

public enum TaskStatus {
    PENDING("待执行"),
    RUNNING("执行中"),
    PAUSED("已暂停"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == RUNNING || this == PENDING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
