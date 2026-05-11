package com.enterprise.dataplatform.ethics.service;

import com.enterprise.dataplatform.ethics.domain.dto.request.EthicsFrameworkRequest;
import com.enterprise.dataplatform.ethics.domain.dto.response.EthicsFrameworkResponse;
import com.enterprise.dataplatform.ethics.domain.entity.EthicsFramework;
import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import com.enterprise.dataplatform.ethics.repository.EthicsFrameworkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EthicsFrameworkServiceTest {

    @Mock
    private EthicsFrameworkRepository frameworkRepository;

    @InjectMocks
    private EthicsFrameworkService frameworkService;

    private EthicsFrameworkRequest request;
    private EthicsFramework framework;

    @BeforeEach
    void setUp() {
        request = EthicsFrameworkRequest.builder()
                .frameworkCode("EF-001")
                .frameworkName("数据伦理框架")
                .description("测试框架")
                .principles(Arrays.asList("透明性", "公平性", "隐私保护"))
                .riskThreshold("MEDIUM")
                .category("ESG")
                .source("内部")
                .tags("数据治理,ESG")
                .build();

        framework = EthicsFramework.builder()
                .id(1L)
                .frameworkCode("EF-001")
                .frameworkName("数据伦理框架")
                .description("测试框架")
                .principles(Arrays.asList("透明性", "公平性", "隐私保护"))
                .riskThreshold(EthicsLevel.MEDIUM)
                .status("DRAFT")
                .enabled(true)
                .version(1)
                .creator("admin")
                .build();
    }

    @Test
    void testCreateFramework_Success() {
        when(frameworkRepository.existsByFrameworkCode("EF-001")).thenReturn(false);
        when(frameworkRepository.save(any(EthicsFramework.class))).thenReturn(framework);

        EthicsFrameworkResponse response = frameworkService.createFramework(request, "admin");

        assertNotNull(response);
        assertEquals("EF-001", response.getFrameworkCode());
        assertEquals("数据伦理框架", response.getFrameworkName());
        assertEquals("DRAFT", response.getStatus());
        assertTrue(response.getEnabled());

        verify(frameworkRepository).existsByFrameworkCode("EF-001");
        verify(frameworkRepository).save(any(EthicsFramework.class));
    }

    @Test
    void testCreateFramework_DuplicateCode() {
        when(frameworkRepository.existsByFrameworkCode("EF-001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                frameworkService.createFramework(request, "admin"));

        verify(frameworkRepository).existsByFrameworkCode("EF-001");
        verify(frameworkRepository, never()).save(any());
    }

    @Test
    void testUpdateFramework_Success() {
        when(frameworkRepository.findById(1L)).thenReturn(Optional.of(framework));
        when(frameworkRepository.save(any(EthicsFramework.class))).thenReturn(framework);

        request.setFrameworkName("更新后的框架名称");
        EthicsFrameworkResponse response = frameworkService.updateFramework(1L, request, "admin");

        assertNotNull(response);
        verify(frameworkRepository).findById(1L);
        verify(frameworkRepository).save(any(EthicsFramework.class));
    }

    @Test
    void testUpdateFramework_NotFound() {
        when(frameworkRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                frameworkService.updateFramework(999L, request, "admin"));

        verify(frameworkRepository).findById(999L);
    }

    @Test
    void testPublishFramework_Success() {
        when(frameworkRepository.findById(1L)).thenReturn(Optional.of(framework));
        when(frameworkRepository.save(any(EthicsFramework.class))).thenAnswer(invocation -> {
            EthicsFramework saved = invocation.getArgument(0);
            saved.setStatus("ACTIVE");
            return saved;
        });

        EthicsFrameworkResponse response = frameworkService.publishFramework(1L, "admin");

        assertNotNull(response);
        assertEquals("ACTIVE", response.getStatus());
        verify(frameworkRepository).save(any(EthicsFramework.class));
    }

    @Test
    void testGetFramework_Success() {
        when(frameworkRepository.findById(1L)).thenReturn(Optional.of(framework));

        EthicsFrameworkResponse response = frameworkService.getFramework(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("EF-001", response.getFrameworkCode());
    }

    @Test
    void testGetFramework_NotFound() {
        when(frameworkRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                frameworkService.getFramework(999L));
    }

    @Test
    void testGetActiveFrameworks() {
        List<EthicsFramework> frameworks = Arrays.asList(framework);
        when(frameworkRepository.findByEnabled(true)).thenReturn(frameworks);

        List<EthicsFrameworkResponse> responses = frameworkService.getActiveFrameworks();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getEnabled());
    }

    @Test
    void testDeleteFramework() {
        doNothing().when(frameworkRepository).deleteById(1L);

        frameworkService.deleteFramework(1L);

        verify(frameworkRepository).deleteById(1L);
    }
}
