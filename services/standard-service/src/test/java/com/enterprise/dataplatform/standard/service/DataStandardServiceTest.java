package com.enterprise.dataplatform.standard.service;

import com.enterprise.dataplatform.standard.domain.entity.DataStandard;
import com.enterprise.dataplatform.standard.domain.entity.StandardVersion;
import com.enterprise.dataplatform.standard.dto.request.DataStandardRequest;
import com.enterprise.dataplatform.standard.repository.DataStandardRepository;
import com.enterprise.dataplatform.standard.repository.StandardVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataStandardServiceTest {

    @Mock
    private DataStandardRepository standardRepository;

    @Mock
    private StandardVersionRepository versionRepository;

    @InjectMocks
    private DataStandardService standardService;

    private DataStandard testStandard;

    @BeforeEach
    void setUp() {
        testStandard = DataStandard.builder()
                .id(1L)
                .standardCode("STD_001")
                .standardName("客户ID标准")
                .standardType("IDENTIFIER")
                .category("CORE")
                .description("定义客户ID的格式和验证规则")
                .dataType("VARCHAR")
                .maxLength(32)
                .pattern("^C[0-9]{10}$")
                .priority(1)
                .status("DRAFT")
                .version(1)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createStandard_shouldCreateSuccessfully() {
        DataStandardRequest request = DataStandardRequest.builder()
                .standardCode("STD_002")
                .standardName("产品编码标准")
                .standardType("CODE")
                .category("BUSINESS")
                .build();

        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);

        DataStandard result = standardService.createStandard(request);

        assertNotNull(result);
        verify(standardRepository, times(1)).save(any(DataStandard.class));
    }

    @Test
    void getStandardById_shouldReturnStandardWhenExists() {
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));

        Optional<DataStandard> result = standardService.getStandardById(1L);

        assertTrue(result.isPresent());
        assertEquals(testStandard.getStandardCode(), result.get().getStandardCode());
    }

    @Test
    void getStandardById_shouldReturnEmptyWhenNotExists() {
        when(standardRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DataStandard> result = standardService.getStandardById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateStandard_shouldUpdateSuccessfully() {
        DataStandardRequest request = DataStandardRequest.builder()
                .standardCode("STD_001")
                .standardName("更新后的标准")
                .standardType("IDENTIFIER")
                .category("CORE")
                .build();

        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);

        DataStandard result = standardService.updateStandard(1L, request);

        assertNotNull(result);
        verify(standardRepository, times(1)).save(any(DataStandard.class));
    }

    @Test
    void publishStandard_shouldPublishSuccessfully() {
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);

        DataStandard result = standardService.publishStandard(1L);

        assertNotNull(result);
        verify(standardRepository, times(1)).save(any(DataStandard.class));
    }

    @Test
    void deprecateStandard_shouldDeprecateSuccessfully() {
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(standardRepository.save(any(DataStandard.class))).thenReturn(testStandard);

        DataStandard result = standardService.deprecateStandard(1L);

        assertNotNull(result);
        verify(standardRepository, times(1)).save(any(DataStandard.class));
    }

    @Test
    void validateDataFormat_shouldReturnTrueForValidData() {
        testStandard.setPattern("^C[0-9]{10}$");
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));

        boolean result = standardService.validateDataFormat(1L, "C1234567890");

        assertTrue(result);
    }

    @Test
    void validateDataFormat_shouldReturnFalseForInvalidData() {
        testStandard.setPattern("^C[0-9]{10}$");
        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));

        boolean result = standardService.validateDataFormat(1L, "INVALID");

        assertFalse(result);
    }

    @Test
    void searchStandards_shouldReturnMatchingStandards() {
        when(standardRepository.searchByKeyword("客户")).thenReturn(List.of(testStandard));

        List<DataStandard> results = standardService.searchStandards("客户");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getStandardsByCategory_shouldReturnMatchingStandards() {
        when(standardRepository.findByCategory("CORE")).thenReturn(List.of(testStandard));

        List<DataStandard> results = standardService.getStandardsByCategory("CORE");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void createVersion_shouldCreateNewVersion() {
        testStandard.setVersion(1);
        StandardVersion newVersion = StandardVersion.builder()
                .id(2L)
                .standardId(1L)
                .versionNumber(2)
                .versionStatus("ACTIVE")
                .build();

        when(standardRepository.findById(1L)).thenReturn(Optional.of(testStandard));
        when(versionRepository.save(any(StandardVersion.class))).thenReturn(newVersion);

        StandardVersion result = standardService.createVersion(1L);

        assertNotNull(result);
        assertEquals(2, result.getVersionNumber());
    }
}
