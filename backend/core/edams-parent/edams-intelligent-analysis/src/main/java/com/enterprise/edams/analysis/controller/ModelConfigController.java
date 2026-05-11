package com.enterprise.edams.analysis.controller;

import com.enterprise.edams.analysis.dto.request.CreateModelConfigRequest;
import com.enterprise.edams.analysis.dto.request.UpdateModelConfigRequest;
import com.enterprise.edams.analysis.dto.response.ConnectionTestResponse;
import com.enterprise.edams.analysis.dto.response.ModelConfigResponse;
import com.enterprise.edams.analysis.service.LocalModelConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/models")
@RequiredArgsConstructor
public class ModelConfigController {

    private final LocalModelConfigService modelConfigService;

    @PostMapping
    public ResponseEntity<ModelConfigResponse> createConfig(
            @Valid @RequestBody CreateModelConfigRequest request) {
        log.info("Creating model config: {}", request.getConfigCode());
        ModelConfigResponse response = modelConfigService.createConfig(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ModelConfigResponse>> listConfigs() {
        log.debug("Listing all model configs");
        List<ModelConfigResponse> configs = modelConfigService.listConfigs();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModelConfigResponse> getConfig(@PathVariable Long id) {
        log.debug("Getting model config: {}", id);
        ModelConfigResponse config = modelConfigService.getConfig(id);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModelConfigResponse> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateModelConfigRequest request) {
        log.info("Updating model config: {}", id);
        ModelConfigResponse response = modelConfigService.updateConfig(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        log.info("Deleting model config: {}", id);
        modelConfigService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<ConnectionTestResponse> testConnection(
            @PathVariable Long id,
            @RequestParam(required = false) String testPrompt) {
        log.info("Testing connection for config: {}", id);
        ConnectionTestResponse response = modelConfigService.testConnection(id, testPrompt);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/available-models")
    public ResponseEntity<List<String>> getAvailableModels(@PathVariable Long id) {
        log.info("Getting available models for config: {}", id);
        List<String> models = modelConfigService.getAvailableModels(id);
        return ResponseEntity.ok(models);
    }

    @GetMapping("/default")
    public ResponseEntity<ModelConfigResponse> getDefaultConfig() {
        log.debug("Getting default model config");
        ModelConfigResponse config = modelConfigService.getDefaultConfig();
        return ResponseEntity.ok(config);
    }
}
