package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.dto.GroupRequest;
import com.enterprise.dataplatform.iot.entity.DeviceGroup;
import com.enterprise.dataplatform.iot.repository.DeviceGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceGroupService {

    private final DeviceGroupRepository deviceGroupRepository;

    @Transactional
    public DeviceGroup createGroup(GroupRequest request) {
        if (deviceGroupRepository.findByGroupName(request.getGroupName()).isPresent()) {
            throw new IllegalArgumentException("Group already exists: " + request.getGroupName());
        }

        String groupId = "GROUP-" + UUID.randomUUID().toString().substring(0, 8);
        String hierarchyPath = buildHierarchyPath(request.getParentId(), groupId);

        DeviceGroup group = DeviceGroup.builder()
                .groupId(groupId)
                .groupName(request.getGroupName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .hierarchyPath(hierarchyPath)
                .deviceCount(0)
                .tags(request.getTags())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        deviceGroupRepository.save(group);
        log.info("Device group created: {}", groupId);
        return group;
    }

    public Optional<DeviceGroup> getGroupById(String groupId) {
        return deviceGroupRepository.findByGroupId(groupId);
    }

    public List<DeviceGroup> getAllGroups() {
        return deviceGroupRepository.findByDeleted(false);
    }

    public List<DeviceGroup> getChildGroups(String parentId) {
        return deviceGroupRepository.findByParentId(parentId);
    }

    @Transactional
    public DeviceGroup updateGroup(String groupId, GroupRequest request) {
        DeviceGroup group = deviceGroupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (request.getGroupName() != null) {
            group.setGroupName(request.getGroupName());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            group.setTags(request.getTags());
        }

        group.setUpdatedAt(LocalDateTime.now());
        deviceGroupRepository.save(group);

        log.info("Device group updated: {}", groupId);
        return group;
    }

    @Transactional
    public void deleteGroup(String groupId) {
        DeviceGroup group = deviceGroupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        List<DeviceGroup> childGroups = deviceGroupRepository.findByParentId(groupId);
        if (!childGroups.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete group with child groups");
        }

        group.setDeleted(true);
        group.setUpdatedAt(LocalDateTime.now());
        deviceGroupRepository.save(group);

        log.info("Device group deleted: {}", groupId);
    }

    @Transactional
    public void updateDeviceCount(String groupId, int delta) {
        DeviceGroup group = deviceGroupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        group.setDeviceCount(Math.max(0, group.getDeviceCount() + delta));
        group.setUpdatedAt(LocalDateTime.now());
        deviceGroupRepository.save(group);
    }

    public List<DeviceGroup> searchGroups(String keyword) {
        return deviceGroupRepository.findByGroupNameContaining(keyword)
                .stream()
                .filter(g -> !g.getDeleted())
                .collect(Collectors.toList());
    }

    private String buildHierarchyPath(String parentId, String groupId) {
        if (parentId == null || parentId.isEmpty()) {
            return "/" + groupId;
        }

        Optional<DeviceGroup> parent = deviceGroupRepository.findByGroupId(parentId);
        if (parent.isPresent()) {
            return parent.get().getHierarchyPath() + "/" + groupId;
        }
        return "/" + groupId;
    }
}
