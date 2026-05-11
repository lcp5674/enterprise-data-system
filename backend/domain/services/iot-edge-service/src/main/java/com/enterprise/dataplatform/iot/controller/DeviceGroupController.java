package com.enterprise.dataplatform.iot.controller;

import com.enterprise.dataplatform.iot.dto.ApiResponse;
import com.enterprise.dataplatform.iot.dto.GroupRequest;
import com.enterprise.dataplatform.iot.entity.DeviceGroup;
import com.enterprise.dataplatform.iot.service.DeviceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Device Group Management", description = "Device group management APIs")
public class DeviceGroupController {

    private final DeviceGroupService deviceGroupService;

    @PostMapping
    @Operation(summary = "Create a new device group")
    public ResponseEntity<ApiResponse<DeviceGroup>> createGroup(
            @Valid @RequestBody GroupRequest request) {
        log.info("Creating device group: {}", request.getGroupName());
        DeviceGroup group = deviceGroupService.createGroup(request);
        return ResponseEntity.ok(ApiResponse.success("Group created successfully", group));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group by ID")
    public ResponseEntity<ApiResponse<DeviceGroup>> getGroup(
            @Parameter(description = "Group ID") @PathVariable String groupId) {
        Optional<DeviceGroup> group = deviceGroupService.getGroupById(groupId);
        return group.map(g -> ResponseEntity.ok(ApiResponse.success(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all groups")
    public ResponseEntity<ApiResponse<List<DeviceGroup>>> getAllGroups() {
        List<DeviceGroup> groups = deviceGroupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/{groupId}/children")
    @Operation(summary = "Get child groups")
    public ResponseEntity<ApiResponse<List<DeviceGroup>>> getChildGroups(
            @Parameter(description = "Group ID") @PathVariable String groupId) {
        List<DeviceGroup> children = deviceGroupService.getChildGroups(groupId);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update a group")
    public ResponseEntity<ApiResponse<DeviceGroup>> updateGroup(
            @Parameter(description = "Group ID") @PathVariable String groupId,
            @Valid @RequestBody GroupRequest request) {
        log.info("Updating device group: {}", groupId);
        DeviceGroup group = deviceGroupService.updateGroup(groupId, request);
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", group));
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete a group")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @Parameter(description = "Group ID") @PathVariable String groupId) {
        log.info("Deleting device group: {}", groupId);
        deviceGroupService.deleteGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search groups by keyword")
    public ResponseEntity<ApiResponse<List<DeviceGroup>>> searchGroups(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        List<DeviceGroup> groups = deviceGroupService.searchGroups(keyword);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }
}
