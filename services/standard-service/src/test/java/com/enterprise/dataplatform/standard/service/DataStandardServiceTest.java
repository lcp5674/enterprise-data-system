package com.enterprise.dataplatform.standard.service;

import com.enterprise.dataplatform.standard.domain.entity.DataStandard;
import com.enterprise.dataplatform.standard.domain.entity.StandardMapping;
import com.enterprise.dataplatform.standard.domain.entity.StandardVersion;
import com.enterprise.dataplatform.standard.dto.request.DataStandardRequest;
import com.enterprise.dataplatform.standard.dto.response.DataStandardResponse;
import com.enterprise.dataplatform.standard.repository.DataStandardRepository;
import com.enterprise.dataplatform.standard.repository.StandardVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DataStandardService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据标准服务测试")
class DataStandardServiceTest {

    @Mock
    private DataStandardRepository standardRepository;

    @Mock
    private StandardVersionRepository versionRepository;

    @InjectMocks
    private DataStandardService dataStandardService;

    // 使用反射设置ObjectMapper，因为RequiredArgsConstructor不自动注入
    private ObjectMapper objectMapper;

    private DataStandard testStandard;
    private DataStandardRequest testRequest;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        // 使用反射注入objectMapper
        var field = DataStandardService.class.getDeclaredField("objectMapper");
        field.setAccessible(true);
        field.set(dataStandardService, objectMapper);

        testStandard = DataStandard.builder()
                .id(1L)
                .standardCode("STD-001")
                .standardName("手机号标准")
                .description("手机号数据标准")
                .category("基础数据")
                .standardType("FORMAT")
                .dataType("VARCHAR")
                .valueRange("^1[3-9]\\d{9}$")
                .maxLength(11)
                .required(true)
                .status("DRAFT")
                .priority(1)
                .version(1)
                .creator("admin")
                .createTime(LocalDateTime.now())
                .build();

        testRequest = DataStandardRequest.builder()
                .standardCode("STD-001")
                .standardName("手机号标准")
                .description("手机号数据标准")
                .category("基础数据")
                .standardType("FORMAT")
                .dataType("VARCHAR")
                .valueRange("^1[3-9]\\d{9}$")
                .maxLength(11)
                .required(true)
                .priority(1)
                .build();
    }

    @Test
    @DisplayName("创建数据标准 - 成功")
    void testCreateStandard_Success() {
        // Given
        when(standardRepository.existsByStandardCode("STD-001")).thenReturn(false);
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);
        when(versionRepository.save(any(StandardVersion.class))).thenReturn(new StandardVersion());

        // When
        DataStandardResponse response = dataStandardService.createStandard(testRequest, "admin");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStandardCode()).isEqualTo("STD-001");
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        verify(standardRepository, times(1)).save(any(DataStandard.class));
        verify(versionRepository, times(1)).save(any(StandardVersion.class));
    }

    @Test
    @DisplayName("创建数据标准 - 编码已存在")
    void testCreateStandard_DuplicateCode() {
        // Given
        when(standardRepository.existsByStandardCode("STD-001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> dataStandardService.createStandard(testRequest, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标准编码已存在");
    }

    @Test
    @DisplayName("更新数据标准 - 成功")
    void testUpdateStandard_Success() {
        // Given
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);
        when(versionRepository.save(any(StandardVersion.class))).thenReturn(new StandardVersion());

        DataStandardRequest updateRequest = DataStandardRequest.builder()
                .standardName("更新后的名称")
                .description("更新后的描述")
                .category("基础数据")
                .standardType("FORMAT")
                .dataType("VARCHAR")
                .required(true)
                .build();

        // When
        DataStandardResponse response = dataStandardService.updateStandard(1L, updateRequest, "updater");

        // Then
        assertThat(response).isNotNull();
        verify(standardRepository, times(1)).save(any(DataStandard.class));
        verify(versionRepository, times(1)).save(any(StandardVersion.class));
    }

    @Test
    @DisplayName("更新数据标准 - 标准不存在")
    void testUpdateStandard_NotFound() {
        // Given
        when(standardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dataStandardService.updateStandard(999L, testRequest, "updater"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("数据标准不存在");
    }

    @Test
    @DisplayName("发布数据标准 - 成功")
    void testPublishStandard_Success() {
        // Given
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);
        when(versionRepository.save(any(StandardVersion.class))).thenReturn(new StandardVersion());

        // When
        DataStandardResponse response = dataStandardService.publishStandard(1L, "publisher");

        // Then
        assertThat(response).isNotNull();
        verify(standardRepository, times(1)).save(any(DataStandard.class));
        verify(versionRepository, times(1)).save(any(StandardVersion.class));
    }

    @Test
    @DisplayName("废弃数据标准 - 成功")
    void testDeprecateStandard_Success() {
        // Given
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);
        when(versionRepository.save(any(StandardVersion.class))).thenReturn(new StandardVersion());

        // When
        DataStandardResponse response = dataStandardService.deprecateStandard(1L, "deprecator", "不再使用");

        // Then
        assertThat(response).isNotNull();
        verify(standardRepository, times(1)).save(any(DataStandard.class));
        verify(versionRepository, times(1)).save(any(StandardVersion.class));
    }

    @Test
    @DisplayName("查询数据标准 - 按ID")
    void testGetStandard() {
        // Given
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));

        // When
        DataStandardResponse response = dataStandardService.getStandard(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStandardCode()).isEqualTo("STD-001");
    }

    @Test
    @DisplayName("查询数据标准 - 按编码")
    void testGetStandardByCode() {
        // Given
        when(standardRepository.findByStandardCode("STD-001")).thenReturn(Optional.of(testStandard));

        // When
        DataStandardResponse response = dataStandardService.getStandardByCode("STD-001");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStandardCode()).isEqualTo("STD-001");
    }

    @Test
    @DisplayName("查询数据标准 - 标准不存在")
    void testGetStandard_NotFound() {
        // Given
        when(standardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dataStandardService.getStandard(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("数据标准不存在");
    }

    @Test
    @DisplayName("分页查询数据标准")
    void testSearchStandards() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<DataStandard> page = new PageImpl<>(List.of(testStandard));
        when(standardRepository.searchStandards(anyString(), anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<DataStandardResponse> response = dataStandardService.searchStandards(
                "基础数据", "ACTIVE", "FORMAT", "手机号", pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取所有激活的标准")
    void testGetAllActiveStandards() {
        // Given
        testStandard.setStatus("ACTIVE");
        when(standardRepository.findAllActive()).thenReturn(List.of(testStandard));

        // When
        List<DataStandardResponse> standards = dataStandardService.getAllActiveStandards();

        // Then
        assertThat(standards).isNotNull();
        assertThat(standards).hasSize(1);
    }

    @Test
    @DisplayName("删除数据标准")
    void testDeleteStandard() {
        // When
        dataStandardService.deleteStandard(1L);

        // Then
        verify(standardRepository, times(1)).deleteById(1L);
    }
}
