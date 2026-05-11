package com.enterprise.dataplatform.iot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sync_record")
@Document(collection = "sync_records")
public class SyncRecord {

    @Id
    private String id;

    @TableField("device_id")
    private String deviceId;

    @TableField("sync_type")
    private SyncType syncType;

    @TableField("sync_direction")
    private SyncDirection syncDirection;

    @TableField("record_count")
    private Long recordCount;

    @TableField("data_size_bytes")
    private Long dataSizeBytes;

    @TableField("compressed_size_bytes")
    private Long compressedSizeBytes;

    @TableField("compression_ratio")
    private Double compressionRatio;

    @TableField("status")
    private Status status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField("priority")
    private String priority;

    @TableField("created_at")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum SyncType {
        FULL,
        INCREMENTAL,
        DELTA
    }

    public enum SyncDirection {
        EDGE_TO_CENTER,
        CENTER_TO_EDGE
    }

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
