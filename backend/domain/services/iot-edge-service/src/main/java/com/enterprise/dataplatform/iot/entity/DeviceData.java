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
@TableName("device_data")
@Document(collection = "device_data")
public class DeviceData {

    @Id
    private String id;

    @TableField("device_id")
    private String deviceId;

    @TableField("data_type")
    private String dataType;

    @TableField("data_key")
    private String dataKey;

    @TableField("data_value")
    private String dataValue;

    @TableField("unit")
    private String unit;

    @TableField("timestamp")
    private LocalDateTime timestamp;

    @TableField("quality")
    private DataQuality quality;

    @TableField("tags")
    private java.util.Map<String, String> tags;

    @TableField("metadata")
    private java.util.Map<String, Object> metadata;

    @TableField("sync_status")
    private SyncStatus syncStatus;

    @TableField("sync_time")
    private LocalDateTime syncTime;

    @TableField("compressed")
    private boolean compressed;

    @TableField("compression_ratio")
    private Double compressionRatio;

    @TableField("created_at")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum DataQuality {
        GOOD,
        UNCERTAIN,
        BAD,
        NO_DATA
    }

    public enum SyncStatus {
        PENDING,
        SYNCING,
        SYNCED,
        FAILED
    }
}
