package com.edams.watermark.dto;

import lombok.Data;

@Data
public class WatermarkCreateRequest {
    private String assetId;
    private String assetType;
    private String ownerId;
    private String ownerName;
    private String watermarkType; // VISIBLE / INVISIBLE / DIGITAL
    private String embedPosition;
    private String extraInfo;
}
