package com.enterprise.edams.watermark.dto;

import com.enterprise.edams.watermark.entity.WatermarkStatus;
import com.enterprise.edams.watermark.entity.WatermarkType;
import lombok.Data;

@Data
public class RecordSearchRequest {
    
    private Long assetId;
    private Long userId;
    private WatermarkType watermarkType;
    private WatermarkStatus status;
    private int pageNum = 1;
    private int pageSize = 20;
}
