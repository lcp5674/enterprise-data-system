package com.edams.watermark.service.impl;

import com.edams.watermark.dto.WatermarkCreateRequest;
import com.edams.watermark.dto.WatermarkDTO;
import com.edams.watermark.entity.Watermark;
import com.edams.watermark.repository.WatermarkMapper;
import com.edams.watermark.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatermarkServiceImpl implements WatermarkService {

    private final WatermarkMapper watermarkMapper;

    @Override
    public WatermarkDTO embedWatermark(WatermarkCreateRequest request) {
        Watermark watermark = new Watermark();
        watermark.setAssetId(request.getAssetId());
        watermark.setAssetType(request.getAssetType());
        watermark.setWatermarkCode(generateWatermarkCode(request));
        watermark.setWatermarkType(request.getWatermarkType());
        watermark.setOwnerId(request.getOwnerId());
        watermark.setOwnerName(request.getOwnerName());
        watermark.setStatus("ACTIVE");
        watermark.setEmbedTime(LocalDateTime.now());
        watermark.setCreateTime(LocalDateTime.now());
        watermarkMapper.insert(watermark);
        log.info("Watermark embedded: code={}, assetId={}", watermark.getWatermarkCode(), watermark.getAssetId());
        return toDTO(watermark);
    }

    @Override
    public WatermarkDTO extractWatermark(String assetId) {
        Watermark watermark = watermarkMapper.findByAssetId(assetId);
        if (watermark == null) {
            throw new RuntimeException("未找到资产水印: " + assetId);
        }
        return toDTO(watermark);
    }

    @Override
    public boolean verifyWatermark(String assetId) {
        Watermark watermark = watermarkMapper.findByAssetId(assetId);
        return watermark != null && "ACTIVE".equals(watermark.getStatus());
    }

    @Override
    public Page<WatermarkDTO> listWatermarks(String assetId, Pageable pageable) {
        List<Watermark> list = watermarkMapper.findAll(assetId,
                (int) pageable.getOffset(), pageable.getPageSize());
        long total = watermarkMapper.count(assetId);
        List<WatermarkDTO> dtos = list.stream().map(this::toDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public WatermarkDTO getById(Long id) {
        Watermark watermark = watermarkMapper.findById(id);
        if (watermark == null) throw new RuntimeException("水印不存在: " + id);
        return toDTO(watermark);
    }

    @Override
    public void removeWatermark(Long id) {
        watermarkMapper.deleteById(id);
        log.info("Watermark removed: id={}", id);
    }

    @Override
    public WatermarkDTO traceByCode(String watermarkCode) {
        Watermark watermark = watermarkMapper.findByCode(watermarkCode);
        if (watermark == null) throw new RuntimeException("水印码不存在: " + watermarkCode);
        return toDTO(watermark);
    }

    private String generateWatermarkCode(WatermarkCreateRequest request) {
        return "WM-" + request.getOwnerId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private WatermarkDTO toDTO(Watermark w) {
        WatermarkDTO dto = new WatermarkDTO();
        dto.setId(w.getId());
        dto.setAssetId(w.getAssetId());
        dto.setAssetType(w.getAssetType());
        dto.setWatermarkCode(w.getWatermarkCode());
        dto.setWatermarkType(w.getWatermarkType());
        dto.setOwnerId(w.getOwnerId());
        dto.setOwnerName(w.getOwnerName());
        dto.setStatus(w.getStatus());
        dto.setEmbedTime(w.getEmbedTime());
        dto.setCreateTime(w.getCreateTime());
        return dto;
    }
}
