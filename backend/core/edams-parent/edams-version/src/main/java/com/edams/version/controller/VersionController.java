package com.edams.version.controller;

import com.edams.version.entity.Version;
import com.edams.version.entity.VersionDiff;
import com.edams.version.service.VersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/version")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createVersion(@RequestBody Version version) {
        Version saved = versionService.createVersion(version);
        return ResponseEntity.ok(Map.of("code", 200, "message", "版本创建成功", "data", saved));
    }

    @GetMapping("/list/{assetId}")
    public ResponseEntity<Map<String, Object>> listVersions(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Version> versions = versionService.listVersionsByAsset(assetId, page * size, size);
        return ResponseEntity.ok(Map.of("code", 200, "data", versions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVersion(@PathVariable Long id) {
        Version version = versionService.getById(id);
        return ResponseEntity.ok(Map.of("code", 200, "data", version));
    }

    @GetMapping("/latest/{assetId}")
    public ResponseEntity<Map<String, Object>> getLatestVersion(@PathVariable String assetId) {
        Version version = versionService.getLatestVersion(assetId);
        return ResponseEntity.ok(Map.of("code", 200, "data", version));
    }

    @PostMapping("/rollback/{id}")
    public ResponseEntity<Map<String, Object>> rollback(@PathVariable Long id) {
        versionService.rollback(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "版本回滚成功"));
    }

    @GetMapping("/diff")
    public ResponseEntity<Map<String, Object>> diffVersions(
            @RequestParam Long fromId,
            @RequestParam Long toId) {
        VersionDiff diff = versionService.compareVersions(fromId, toId);
        return ResponseEntity.ok(Map.of("code", 200, "data", diff));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVersion(@PathVariable Long id) {
        versionService.deleteVersion(id);
        return ResponseEntity.ok(Map.of("code", 200, "message", "版本已删除"));
    }
}
