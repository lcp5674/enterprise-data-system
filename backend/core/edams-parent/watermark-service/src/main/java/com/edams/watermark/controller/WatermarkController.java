package com.edams.watermark.controller;

import com.edams.watermark.dto.WatermarkCreateRequest;
import com.edams.watermark.dto.WatermarkDTO;
import com.edams.watermark.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/watermark")
@RequiredArgsConstructor
public class WatermarkController {

    private final WatermarkService watermarkService;

    @PostMapping("/embed")
    public ResponseEntity<Map<String, Object>> embedWatermark(@RequestBody WatermarkCreateRequest request) {
        log.info("Embedding watermark for assetId: {}", request.getAssetId());
        WatermarkDTO result = watermarkService.embedWatermark(request);
        return ResponseEntity.ok(Map.of("code", 200, "message", "水印嵌入成功", "data", result));
    }

    @GetMapping("/extract/{assetId}")
    public ResponseEntity<Map<String, Object>> extractWatermark(@PathVariable String assetId) {
        log.info("Extracting watermark for assetId: {}", assetId);
        WatermarkDTO result = watermarkService.extractWatermark(assetId);
        return ResponseEntity.ok(Map.of("code", 200, "message", "水印提取成功", "data", result));
    }

    @GetMapping("/verify/{assetId}")
    public ResponseEntity<Map<String, Object>> verifyWatermark(@PathVariable String assetId) {
        boolean valid = watermarkService.verifyWatermark(assetId);
        return ResponseEntity.ok(Map.of("code", 200, "message", "水印验证完成", "data", Map.of("valid", valid)));
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listWatermarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String assetId) {
        Page<WatermarkDTO> result = watermarkService.listWatermarks(assetId, PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of("code", 200, "message", "success", "data", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWatermarkById(@PathVariable Long id) {
        WatermarkDTO result = watermarkService.getById(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "success", "data", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removeWatermark(@PathVariable Long id) {
        watermarkService.removeWatermark(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "水印已移除"));
    }

    @GetMapping("/trace/{watermarkCode}")
    public ResponseEntity<Map<String, Object>> traceWatermark(@PathVariable String watermarkCode) {
        WatermarkDTO result = watermarkService.traceByCode(watermarkCode);
        return ResponseEntity.ok(Map.of("code", 200, "message", "溯源成功", "data", result));
    }
}
