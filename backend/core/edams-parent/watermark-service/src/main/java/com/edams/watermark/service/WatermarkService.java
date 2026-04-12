package com.edams.watermark.service;

import com.edams.watermark.dto.WatermarkCreateRequest;
import com.edams.watermark.dto.WatermarkDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WatermarkService {
    WatermarkDTO embedWatermark(WatermarkCreateRequest request);
    WatermarkDTO extractWatermark(String assetId);
    boolean verifyWatermark(String assetId);
    Page<WatermarkDTO> listWatermarks(String assetId, Pageable pageable);
    WatermarkDTO getById(Long id);
    void removeWatermark(Long id);
    WatermarkDTO traceByCode(String watermarkCode);
}
