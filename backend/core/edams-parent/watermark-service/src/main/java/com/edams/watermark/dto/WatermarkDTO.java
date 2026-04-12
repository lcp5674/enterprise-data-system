package com.edams.watermark.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WatermarkDTO {
    private Long id;
    private String assetId;
    private String assetType;
    private String watermarkCode;
    private String watermarkType;
    private String ownerId;
    private String ownerName;
    private String status;
    private LocalDateTime embedTime;
    private LocalDateTime createTime;
}
