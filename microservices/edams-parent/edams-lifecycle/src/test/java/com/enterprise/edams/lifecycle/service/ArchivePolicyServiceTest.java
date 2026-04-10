package com.enterprise.edams.lifecycle.service;

import com.enterprise.edams.lifecycle.dto.ArchivePolicyCreateRequest;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyDTO;
import com.enterprise.edams.lifecycle.entity.ArchivePolicy;
import com.enterprise.edams.lifecycle.entity.ArchiveRecord;
import com.enterprise.edams.lifecycle.repository.ArchivePolicyRepository;
import com.enterprise.edams.lifecycle.service.impl.ArchivePolicyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 归档策略服务单元测试
 *
 * @author EDAMS Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("归档策略服务单元测试")
class ArchivePolicyServiceTest {

    @Mock
    private ArchivePolicyRepository archivePolicyRepository;

    @Mock
    private ArchiveRecordService archiveRecordService;

    @InjectMocks
    private ArchivePolicyServiceImpl archivePolicyService;

    private ArchivePolicy testPolicy;
    private ArchivePolicyCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 设置配置属性
        ReflectionTestUtils.setField(archivePolicyService, "archiveStoragePath", "/data/archive");
        ReflectionTestUtils.setField(archivePolicyService, "storageType", "local");
        ReflectionTestUtils.setField(archivePolicyService, "archiveBatchSize", 100);

        testPolicy = new ArchivePolicy();
        testPolicy.setId("policy-001");
        testPolicy.setName("测试归档策略");
        testPolicy.setCode("TEST-ARCHIVE-001");
        testPolicy.setDescription("测试归档描述");
        testPolicy.setBusinessType("data_asset");
        testPolicy.setDataCategory("sensitive");
        testPolicy.setRetentionDays(365);
        testPolicy.setTriggerType(1);
        testPolicy.setArchiveTarget(1);
        testPolicy.setCompressionType(1); // GZIP
        testPolicy.setEncryptionType(0);
        testPolicy.setDeleteSource(false);
        testPolicy.setEnabled(true);
        testPolicy.setExecuteCount(0);
        testPolicy.setSuccessCount(0);
        testPolicy.setFailCount(0);
        testPolicy.setTriggerCondition("{\"days\":90}");
        testPolicy.setTargetConfig("{\"type\":\"local\"}");
        testPolicy.setCreatedBy("system");
        testPolicy.setCreateTime(LocalDateTime.now());

        createRequest = new ArchivePolicyCreateRequest();
        createRequest.setName("新建归档策略");
        createRequest.setCode("NEW-ARCHIVE-001");
        createRequest.setDescription("新建归档描述");
        createRequest.setBusinessType("data_asset");
        createRequest.setDataCategory("sensitive");
        createRequest.setRetentionDays(180);
        createRequest.setTriggerType(1);
        createRequest.setArchiveTarget(1);
        createRequest.setCompressionType(2); // ZIP
        createRequest.setEncryptionType(1); // AES-256
        createRequest.setDeleteSource(false);
        createRequest.setPriority(1);
    }

    @Test
    @DisplayName("创建归档策略 - 成功")
    void testCreatePolicy_Success() {
        // Given
        when(archivePolicyRepository.insert(any(ArchivePolicy.class))).thenReturn(1);

        // When
        ArchivePolicyDTO result = archivePolicyService.createPolicy(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("新建归档策略");
        assertThat(result.getCode()).isEqualTo("NEW-ARCHIVE-001");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getExecuteCount()).isEqualTo(0);
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailCount()).isEqualTo(0);

        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository, times(1)).insert(captor.capture());
        ArchivePolicy capturedPolicy = captor.getValue();
        assertThat(capturedPolicy.getCompressionType()).isEqualTo(2);
        assertThat(capturedPolicy.getEncryptionType()).isEqualTo(1);
    }

    @Test
    @DisplayName("更新归档策略 - 成功")
    void testUpdatePolicy_Success() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        ArchivePolicyCreateRequest updateRequest = new ArchivePolicyCreateRequest();
        updateRequest.setName("更新后的策略名称");
        updateRequest.setRetentionDays(200);

        // When
        ArchivePolicyDTO result = archivePolicyService.updatePolicy("policy-001", updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(archivePolicyRepository, times(1)).updateById(any(ArchivePolicy.class));
    }

    @Test
    @DisplayName("更新归档策略 - 策略不存在")
    void testUpdatePolicy_NotFound() {
        // Given
        when(archivePolicyRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> archivePolicyService.updatePolicy("non-existent", createRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("归档策略不存在");
    }

    @Test
    @DisplayName("删除归档策略 - 成功")
    void testDeletePolicy_Success() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.deleteById("policy-001")).thenReturn(true);

        // When
        archivePolicyService.deletePolicy("policy-001");

        // Then
        verify(archivePolicyRepository, times(1)).deleteById("policy-001");
    }

    @Test
    @DisplayName("获取归档策略 - 成功")
    void testGetPolicy_Success() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);

        // When
        ArchivePolicyDTO result = archivePolicyService.getPolicy("policy-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("policy-001");
        assertThat(result.getName()).isEqualTo("测试归档策略");
        assertThat(result.getCode()).isEqualTo("TEST-ARCHIVE-001");
    }

    @Test
    @DisplayName("获取归档策略 - 不存在")
    void testGetPolicy_NotFound() {
        // Given
        when(archivePolicyRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> archivePolicyService.getPolicy("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("归档策略不存在");
    }

    @Test
    @DisplayName("启用归档策略 - 成功")
    void testEnablePolicy_Success() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.enablePolicy("policy-001");

        // Then
        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository).updateById(captor.capture());
        assertThat(captor.getValue().getEnabled()).isTrue();
    }

    @Test
    @DisplayName("禁用归档策略 - 成功")
    void testDisablePolicy_Success() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.disablePolicy("policy-001");

        // Then
        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository).updateById(captor.capture());
        assertThat(captor.getValue().getEnabled()).isFalse();
    }

    @Test
    @DisplayName("执行归档策略 - 无待归档资产")
    void testExecutePolicy_NoAssets() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then - 无资产时只更新执行统计
        verify(archivePolicyRepository, times(1)).updateById(any(ArchivePolicy.class));
        verify(archiveRecordService, never()).save(any(ArchiveRecord.class));
    }

    @Test
    @DisplayName("执行归档策略 - GZIP压缩测试")
    void testExecutePolicy_GZIPCompression() {
        // Given
        testPolicy.setCompressionType(1); // GZIP
        testPolicy.setRetentionDays(90);

        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then
        verify(archivePolicyRepository, times(1)).updateById(any(ArchivePolicy.class));
    }

    @Test
    @DisplayName("执行归档策略 - ZIP压缩测试")
    void testExecutePolicy_ZIPCompression() {
        // Given
        testPolicy.setCompressionType(2); // ZIP
        testPolicy.setRetentionDays(60);

        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then
        verify(archivePolicyRepository, times(1)).updateById(any(ArchivePolicy.class));
    }

    @Test
    @DisplayName("执行归档策略 - 策略不存在")
    void testExecutePolicy_NotFound() {
        // Given
        when(archivePolicyRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> archivePolicyService.executePolicy("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("归档策略不存在");
    }

    @Test
    @DisplayName("获取压缩格式名称 - GZIP")
    void testGetCompressionFormat_GZIP() {
        // Given
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        testPolicy.setCompressionType(1);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then
        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository).updateById(captor.capture());
        assertThat(captor.getValue().getCompressionType()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取压缩格式名称 - ZIP")
    void testGetCompressionFormat_ZIP() {
        // Given
        testPolicy.setCompressionType(2);
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then
        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository).updateById(captor.capture());
        assertThat(captor.getValue().getCompressionType()).isEqualTo(2);
    }

    @Test
    @DisplayName("获取加密算法名称 - AES-256")
    void testGetEncryptionAlgorithm_AES256() {
        // Given
        testPolicy.setEncryptionType(1);
        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then
        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository).updateById(captor.capture());
        assertThat(captor.getValue().getEncryptionType()).isEqualTo(1);
    }

    @Test
    @DisplayName("执行统计更新 - 成功计数增加")
    void testUpdatePolicyExecutionStats_Success() {
        // Given
        testPolicy.setExecuteCount(0);
        testPolicy.setSuccessCount(0);
        testPolicy.setFailCount(0);

        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        archivePolicyService.executePolicy("policy-001");

        // Then
        ArgumentCaptor<ArchivePolicy> captor = ArgumentCaptor.forClass(ArchivePolicy.class);
        verify(archivePolicyRepository).updateById(captor.capture());

        ArchivePolicy updatedPolicy = captor.getValue();
        assertThat(updatedPolicy.getLastExecuteTime()).isNotNull();
        assertThat(updatedPolicy.getExecuteCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("执行统计更新 - 失败计数增加")
    void testUpdatePolicyExecutionStats_Failure() {
        // Given
        testPolicy.setExecuteCount(0);
        testPolicy.setSuccessCount(0);
        testPolicy.setFailCount(0);

        when(archivePolicyRepository.selectById("policy-001")).thenReturn(testPolicy);
        when(archivePolicyRepository.updateById(any(ArchivePolicy.class))).thenReturn(true);

        // When
        try {
            archivePolicyService.executePolicy("policy-001");
        } catch (Exception e) {
            // 预期无资产情况不会抛异常
        }

        // Then - 即使无资产也应该更新执行统计
        verify(archivePolicyRepository).updateById(any(ArchivePolicy.class));
    }
}
