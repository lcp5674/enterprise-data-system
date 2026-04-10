package com.enterprise.edams.watermark.dto;

import com.enterprise.edams.watermark.entity.WatermarkType;
import lombok.Data;

@Data
public class AddWatermarkRequest {
    
    private Long assetId;
    private String assetName;
    private String fileType;
    private String filePath;
    private WatermarkType watermarkType;
    private Long templateId;
    private Long userId;
    private String userName;
    private Long deptId;
    private String watermarkContent;
}
