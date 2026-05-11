package com.enterprise.edams.analysis.entity;

public enum ExecutionMode {
    MANUAL("手动执行"),
    ASYNC("异步执行"),
    SCHEDULED("定时任务");

    private final String description;

    ExecutionMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
