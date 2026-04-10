package com.enterprise.dataplatform.standard.service;

import com.enterprise.dataplatform.standard.domain.entity.DataStandard;
import com.enterprise.dataplatform.standard.domain.entity.StandardMapping;
import com.enterprise.dataplatform.standard.dto.request.StandardMappingRequest;
import com.enterprise.dataplatform.standard.dto.response.StandardMappingResponse;
import com.enterprise.dataplatform.standard.repository.DataStandardRepository;
import com.enterprise.dataplatform.standard.repository.StandardMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * StandardMappingService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("标准映射服务测试")
class StandardMappingServiceTest {

    @Mock
    private StandardMappingRepository mappingRepository;

    @Mock
    private DataStandardRepository standardRepository;

    @InjectMocks
    private StandardMappingService mappingService;

    private DataStandard testStandard;
    private StandardMapping testMapping;
    private StandardMappingRequest testRequest;

    @BeforeEach
    void setUp() {
        testStandard = DataStandard.builder()
                .id(1L)
                .standardCode("STD-001")
                .standardName("手机号标准")
                .status("ACTIVE")
                .build();

        testMapping = StandardMapping.builder()
                .id(1L)
                .dataStandard(testStandard)
                .assetId("COL-001")
                .assetName("phone_number")
                .assetType("COLUMN")
                .fieldName("phone_number")
                .fieldChineseName("手机号")
                .fieldDataType("VARCHAR")
                .mappingStatus("MAPPED")
                .mappingType("STANDARD_FIELD")
                .coverageRate(100.0)
                .qualityScore(95.0)
                .creator("admin")
                .createTime(LocalDateTime.now())
                .build();

        testRequest = StandardMappingRequest.builder()
                .standardId(1L)
                .assetId("COL-001")
                .assetName("phone_number")
                .assetType("COLUMN")
                .fieldName("phone_number")
                .fieldChineseName("手机号")
                .fieldDataType("VARCHAR")
                .mappingType("STANDARD_FIELD")
                .build();
    }

    @Test
    @DisplayName("创建字段映射 - 成功")
    void testCreateMapping_Success() {
        // Given
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(mappingRepository.save(any(StandardMapping.class))).thenReturn(testMapping);

        // When
        StandardMappingResponse response = mappingService.createMapping(testRequest, "admin");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAssetId()).isEqualTo("COL-001");
        verify(mappingRepository, times(1)).save(any(StandardMapping.class));
    }

    @Test
    @DisplayName("创建字段映射 - 标准不存在")
    void testCreateMapping_StandardNotFound() {
        // Given
        when(standardRepository.findById(999L)).thenReturn(Optional.empty());

        testRequest.setStandardId(999L);

        // When & Then
        assertThatThrownBy(() -> mappingService.createMapping(testRequest, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标准不存在");
    }

    @Test
    @DisplayName("更新字段映射 - 成功")
    void testUpdateMapping_Success() {
        // Given
        when(mappingRepository.findById(1L)).thenReturn(Optional.of(testMapping));
        when(mappingRepository.save(any(StandardMapping.class))).thenReturn(testMapping);

        StandardMappingRequest updateRequest = StandardMappingRequest.builder()
                .fieldChineseName("电话号码")
                .mappingType("STANDARD_FIELD")
                .build();

        // When
        StandardMappingResponse response = mappingService.updateMapping(1L, updateRequest, "updater");

        // Then
        assertThat(response).isNotNull();
        verify(mappingRepository, times(1)).save(any(StandardMapping.class));
    }

    @Test
    @DisplayName("更新字段映射 - 映射不存在")
    void testUpdateMapping_NotFound() {
        // Given
        when(mappingRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> mappingService.updateMapping(999L, testRequest, "updater"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("映射不存在");
    }

    @Test
    @DisplayName("删除字段映射")
    void testDeleteMapping() {
        // When
        mappingService.deleteMapping(1L);

        // Then
        verify(mappingRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("获取映射详情")
    void testGetMapping() {
        // Given
        when(mappingRepository.findById(1L)).thenReturn(Optional.of(testMapping));

        // When
        StandardMappingResponse response = mappingService.getMapping(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("根据资产获取映射列表")
    void testGetMappingsByAsset() {
        // Given
        when(mappingRepository.findByAssetId("COL-001")).thenReturn(List.of(testMapping));

        // When
        List<StandardMappingResponse> mappings = mappingService.getMappingsByAsset("COL-001");

        // Then
        assertThat(mappings).isNotNull();
        assertThat(mappings).hasSize(1);
    }

    @Test
    @DisplayName("根据标准获取映射列表")
    void testGetMappingsByStandard() {
        // Given
        when(mappingRepository.findByStandardId(1L)).thenReturn(List.of(testMapping));

        // When
        List<StandardMappingResponse> mappings = mappingService.getMappingsByStandard(1L);

        // Then
        assertThat(mappings).isNotNull();
        assertThat(mappings).hasSize(1);
    }

    @Test
    @DisplayName("获取未映射字段")
    void testGetUnmappedFields() {
        // Given
        when(mappingRepository.findByMappingStatus("UNMAPPED")).thenReturn(List.of());

        // When
        List<StandardMappingResponse> unmapped = mappingService.getUnmappedFields();

        // Then
        assertThat(unmapped).isNotNull();
    }

    @Test
    @DisplayName("批量创建映射")
    void testBatchCreateMappings() {
        // Given
        List<StandardMappingRequest> requests = List.of(testRequest);
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(mappingRepository.save(any(StandardMapping.class))).thenReturn(testMapping);

        // When
        List<StandardMappingResponse> results = mappingService.batchCreateMappings(requests, "admin");

        // Then
        assertThat(results).isNotNull();
        verify(mappingRepository, times(1)).save(any(StandardMapping.class));
    }

    @Test
    @DisplayName("检查映射状态")
    void testCheckMappingStatus() {
        // Given
        when(mappingRepository.countByMappingStatus("MAPPED")).thenReturn(80L);
        when(mappingRepository.countByMappingStatus("UNMAPPED")).thenReturn(20L);
        when(mappingRepository.count()).thenReturn(100L);

        // When
        StandardMappingService.MappingStatusVO status = mappingService.checkMappingStatus();

        // Then
        assertThat(status).isNotNull();
        assertThat(status.getTotalCount()).isEqualTo(100L);
        assertThat(status.getMappedCount()).isEqualTo(80L);
        assertThat(status.getUnmappedCount()).isEqualTo(20L);
        assertThat(status.getCoverageRate()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("更新映射覆盖率")
    void testUpdateCoverageRate() {
        // Given
        when(mappingRepository.findById(1L)).thenReturn(Optional.of(testMapping));
        when(mappingRepository.save(any(StandardMapping.class))).thenReturn(testMapping);

        // When
        mappingService.updateCoverageRate(1L, 85.5);

        // Then
        verify(mappingRepository, times(1)).save(any(StandardMapping.class));
    }

    @Test
    @DisplayName("更新映射质量得分")
    void testUpdateQualityScore() {
        // Given
        when(mappingRepository.findById(1L)).thenReturn(Optional.of(testMapping));
        when(mappingRepository.save(any(StandardMapping.class))).thenReturn(testMapping);

        // When
        mappingService.updateQualityScore(1L, 98.5);

        // Then
        verify(mappingRepository, times(1)).save(any(StandardMapping.class));
    }
}
