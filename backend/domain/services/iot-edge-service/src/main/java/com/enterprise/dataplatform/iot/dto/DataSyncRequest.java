package com.enterprise.dataplatform.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSyncRequest {

    private String deviceId;
    private String syncType;
    private String priority;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer batchSize;
}
