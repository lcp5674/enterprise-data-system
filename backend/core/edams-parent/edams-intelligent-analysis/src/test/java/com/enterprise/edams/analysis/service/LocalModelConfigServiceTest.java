package com.enterprise.edams.analysis.service;

import com.enterprise.edams.analysis.dto.request.CreateModelConfigRequest;
import com.enterprise.edams.analysis.dto.response.ConnectionTestResponse;
import com.enterprise.edams.analysis.dto.response.ModelConfigResponse;
import com.enterprise.edams.analysis.entity.LocalModelConfig;
import com.enterprise.edams.analysis.entity.ModelType;
import com.enterprise.edams.analysis.exception.AnalysisException;
import com.enterprise.edams.analysis.llm.LLMConnector;
import com.enterprise.edams.analysis.llm.LLMConnectorFactory;
import com.enterprise.edams.analysis.llm.LLMResponse;
import com.enterprise.edams.analysis.repository.LocalModelConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalModelConfigServiceTest {

    @Mock
    private LocalModelConfigRepository repository;

    @Mock
    private LLMConnectorFactory connectorFactory;

    @InjectMocks
    private LocalModelConfigService service;

    private LocalModelConfig testConfig;
    private CreateModelConfigRequest createRequest;

    @BeforeEach
    void setUp() {
        testConfig = LocalModelConfig.builder()
                .id(1L)
                .configCode("OLLAMA_QWEN")
                .configName("Ollama Qwen2.5")
                .modelType(ModelType.OLLAMA)
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5-7b")
                .enabled(true)
                .isDefault(true)
                .build();

        createRequest = CreateModelConfigRequest.builder()
                .configCode("OLLAMA_QWEN")
                .configName("Ollama Qwen2.5")
                .modelType(ModelType.OLLAMA)
                .baseUrl("http://localhost:11434")
                .modelName("qwen2.5-7b")
                .enabled(true)
                .isDefault(true)
                .build();
    }

    @Test
    void testCreateConfig_Success() {
        when(repository.existsByConfigCode("OLLAMA_QWEN")).thenReturn(false);
        when(repository.save(any(LocalModelConfig.class))).thenReturn(testConfig);

        ModelConfigResponse response = service.createConfig(createRequest);

        assertNotNull(response);
        assertEquals("OLLAMA_QWEN", response.getConfigCode());
        assertEquals("Ollama Qwen2.5", response.getConfigName());
        verify(repository, times(1)).save(any(LocalModelConfig.class));
    }

    @Test
    void testCreateConfig_DuplicateCode() {
        when(repository.existsByConfigCode("OLLAMA_QWEN")).thenReturn(true);

        assertThrows(AnalysisException.class, () -> {
            service.createConfig(createRequest);
        });

        verify(repository, never()).save(any(LocalModelConfig.class));
    }

    @Test
    void testGetConfig_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testConfig));

        ModelConfigResponse response = service.getConfig(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("OLLAMA_QWEN", response.getConfigCode());
    }

    @Test
    void testGetConfig_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AnalysisException.class, () -> {
            service.getConfig(999L);
        });
    }

    @Test
    void testDeleteConfig_Success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteConfig(1L));

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteConfig_NotFound() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(AnalysisException.class, () -> {
            service.deleteConfig(999L);
        });

        verify(repository, never()).deleteById(anyLong());
    }
}
