package com.enterprise.dataplatform.iot.service;

import com.enterprise.dataplatform.iot.dto.GroupRequest;
import com.enterprise.dataplatform.iot.entity.DeviceGroup;
import com.enterprise.dataplatform.iot.repository.DeviceGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceGroupService Unit Tests")
class DeviceGroupServiceTest {

    @Mock
    private DeviceGroupRepository deviceGroupRepository;

    @InjectMocks
    private DeviceGroupService deviceGroupService;

    private DeviceGroup testGroup;
    private GroupRequest groupRequest;

    @BeforeEach
    void setUp() {
        testGroup = DeviceGroup.builder()
                .id("1")
                .groupId("GROUP-001")
                .groupName("Production Sensors")
                .description("Production environment sensors")
                .deviceCount(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        groupRequest = GroupRequest.builder()
                .groupName("Production Sensors")
                .description("Production environment sensors")
                .build();
    }

    @Test
    @DisplayName("Should create group successfully")
    void testCreateGroup_Success() {
        when(deviceGroupRepository.findByGroupName("Production Sensors")).thenReturn(Optional.empty());
        when(deviceGroupRepository.save(any(DeviceGroup.class))).thenAnswer(i -> {
            DeviceGroup group = i.getArgument(0);
            group.setId("1");
            return group;
        });

        DeviceGroup result = deviceGroupService.createGroup(groupRequest);

        assertNotNull(result);
        assertEquals("Production Sensors", result.getGroupName());
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup.class));
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate group")
    void testCreateGroup_Duplicate() {
        when(deviceGroupRepository.findByGroupName("Production Sensors")).thenReturn(Optional.of(testGroup));

        assertThrows(IllegalArgumentException.class, () ->
                deviceGroupService.createGroup(groupRequest));
    }

    @Test
    @DisplayName("Should get group by ID successfully")
    void testGetGroupById_Success() {
        when(deviceGroupRepository.findByGroupId("GROUP-001")).thenReturn(Optional.of(testGroup));

        Optional<DeviceGroup> result = deviceGroupService.getGroupById("GROUP-001");

        assertTrue(result.isPresent());
        assertEquals("GROUP-001", result.get().getGroupId());
    }

    @Test
    @DisplayName("Should return empty when group not found")
    void testGetGroupById_NotFound() {
        when(deviceGroupRepository.findByGroupId("UNKNOWN")).thenReturn(Optional.empty());

        Optional<DeviceGroup> result = deviceGroupService.getGroupById("UNKNOWN");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should get all groups successfully")
    void testGetAllGroups_Success() {
        when(deviceGroupRepository.findByDeleted(false)).thenReturn(Collections.singletonList(testGroup));

        List<DeviceGroup> groups = deviceGroupService.getAllGroups();

        assertEquals(1, groups.size());
        assertEquals("GROUP-001", groups.get(0).getGroupId());
    }

    @Test
    @DisplayName("Should get child groups successfully")
    void testGetChildGroups_Success() {
        when(deviceGroupRepository.findByParentId("GROUP-001")).thenReturn(Collections.emptyList());

        List<DeviceGroup> children = deviceGroupService.getChildGroups("GROUP-001");

        assertNotNull(children);
    }

    @Test
    @DisplayName("Should update group successfully")
    void testUpdateGroup_Success() {
        GroupRequest updateRequest = GroupRequest.builder()
                .groupName("Updated Group Name")
                .description("Updated description")
                .build();

        when(deviceGroupRepository.findByGroupId("GROUP-001")).thenReturn(Optional.of(testGroup));
        when(deviceGroupRepository.save(any(DeviceGroup.class))).thenReturn(testGroup);

        DeviceGroup result = deviceGroupService.updateGroup("GROUP-001", updateRequest);

        assertNotNull(result);
        verify(deviceGroupRepository, times(1)).save(any(DeviceGroup.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent group")
    void testUpdateGroup_NotFound() {
        when(deviceGroupRepository.findByGroupId("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                deviceGroupService.updateGroup("UNKNOWN", groupRequest));
    }

    @Test
    @DisplayName("Should delete group successfully")
    void testDeleteGroup_Success() {
        when(deviceGroupRepository.findByGroupId("GROUP-001")).thenReturn(Optional.of(testGroup));
        when(deviceGroupRepository.findByParentId("GROUP-001")).thenReturn(Collections.emptyList());
        when(deviceGroupRepository.save(any(DeviceGroup.class))).thenReturn(testGroup);

        assertDoesNotThrow(() -> deviceGroupService.deleteGroup("GROUP-001"));

        verify(deviceGroupRepository, times(1)).save(argThat(group -> group.getDeleted()));
    }

    @Test
    @DisplayName("Should throw exception when deleting group with children")
    void testDeleteGroup_WithChildren() {
        DeviceGroup childGroup = DeviceGroup.builder()
                .groupId("CHILD-001")
                .parentId("GROUP-001")
                .build();

        when(deviceGroupRepository.findByGroupId("GROUP-001")).thenReturn(Optional.of(testGroup));
        when(deviceGroupRepository.findByParentId("GROUP-001")).thenReturn(Collections.singletonList(childGroup));

        assertThrows(IllegalArgumentException.class, () ->
                deviceGroupService.deleteGroup("GROUP-001"));
    }

    @Test
    @DisplayName("Should update device count successfully")
    void testUpdateDeviceCount_Success() {
        when(deviceGroupRepository.findByGroupId("GROUP-001")).thenReturn(Optional.of(testGroup));
        when(deviceGroupRepository.save(any(DeviceGroup.class))).thenReturn(testGroup);

        assertDoesNotThrow(() -> deviceGroupService.updateDeviceCount("GROUP-001", 5));

        verify(deviceGroupRepository, times(1)).save(argThat(group ->
                group.getDeviceCount() == 15
        ));
    }

    @Test
    @DisplayName("Should not allow negative device count")
    void testUpdateDeviceCount_Negative() {
        when(deviceGroupRepository.findByGroupId("GROUP-001")).thenReturn(Optional.of(testGroup));
        when(deviceGroupRepository.save(any(DeviceGroup.class))).thenReturn(testGroup);

        assertDoesNotThrow(() -> deviceGroupService.updateDeviceCount("GROUP-001", -20));

        verify(deviceGroupRepository, times(1)).save(argThat(group ->
                group.getDeviceCount() == 0
        ));
    }

    @Test
    @DisplayName("Should search groups by keyword successfully")
    void testSearchGroups_Success() {
        when(deviceGroupRepository.findByGroupNameContaining("Production"))
                .thenReturn(Collections.singletonList(testGroup));

        List<DeviceGroup> results = deviceGroupService.searchGroups("Production");

        assertEquals(1, results.size());
        assertEquals("GROUP-001", results.get(0).getGroupId());
    }
}
