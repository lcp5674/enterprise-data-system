package com.enterprise.dataplatform.portal.dto.response;

import lombok.Data;

/**
 * Popular Asset Response DTO
 */
@Data
public class PopularAssetResponse {
    private String assetId;
    private String assetName;
    private String assetType;
    private String owner;
    private Long viewCount;
    private Long usageCount;
    private Double qualityScore;
}
