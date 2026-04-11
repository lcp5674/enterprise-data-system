package com.enterprise.dataplatform.metadata.service;

import com.enterprise.dataplatform.metadata.dto.request.MetadataFieldRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataRegisterRequest;
import com.enterprise.dataplatform.metadata.dto.request.MetadataSearchRequest;
import com.enterprise.dataplatform.metadata.dto.response.MetadataResponse;
import com.enterprise.dataplatform.metadata.entity.MetadataObject;
import com.enterprise.dataplatform.metadata.repository.MetadataFieldRepository;
import com.enterprise.dataplatform.metadata.repository.MetadataObjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MetadataService
 */
@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @Mock
    private MetadataObjectRepository metadataObjectRepository;

    @Mock
    private MetadataFieldRepository metadataFieldRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    @InjectMocks
    private MetadataService metadataService;

    private MetadataRegisterRequest testRequest;
    private MetadataObject testMetadataObject;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Initialize service with real ObjectMapper
        metadataService = new MetadataService(
                metadataObjectRepository,
                metadataFieldRepository,
                kafkaTemplate,
                objectMapper
        );

        // Prepare test request
        testRequest = MetadataRegisterRequest.builder()
                .objectId("test-object-001")
                .objectType("TABLE")
                .domainCode("finance")
                .name("test_table")
                .displayName("测试表")
                .description("This is a test table")
                .owner("admin")
                .ownerEmail("admin@example.com")
                .sensitivity("INTERNAL")
                .dataSource("postgresql")
                .build();

        // Prepare test entity
        testMetadataObject = MetadataObject.builder()
                .id(1L)
                .objectId("test-object-001")
                .objectType("TABLE")
                .domainCode("finance")
                .name("test_table")
                .displayName("测试表")
                .description("This is a test table")
                .owner("admin")
                .ownerEmail("admin@example.com")
                .sensitivity("INTERNAL")
                .status("ACTIVE")
                .dataSource("postgresql")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("测试注册新元数据")
    void testRegisterMetadata_NewObject() {
        // Given
        when(metadataObjectRepository.findByObjectId("test-object-001"))
                .thenReturn(Optional.empty());
        when(metadataObjectRepository.save(any(MetadataObject.class)))
                .thenReturn(testMetadataObject);
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        // When
        MetadataResponse response = metadataService.registerMetadata(testRequest, null);

        // Then
        assertNotNull(response);
        assertEquals("test-object-001", response.getObjectId());
        assertEquals("TABLE", response.getObjectType());
        assertEquals("finance", response.getDomainCode());
        assertEquals("ACTIVE", response.getStatus());
        
        verify(metadataObjectRepository).findByObjectId("test-object-001");
        verify(metadataObjectRepository).save(any(MetadataObject.class));
    }

    @Test
    @DisplayName("测试注册已有元数据时更新")
    void testRegisterMetadata_ExistingObject() {
        // Given
        when(metadataObjectRepository.findByObjectId("test-object-001"))
                .thenReturn(Optional.of(testMetadataObject));
        when(metadataObjectRepository.save(any(MetadataObject.class)))
                .thenReturn(testMetadataObject);
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        MetadataRegisterRequest updateRequest = MetadataRegisterRequest.builder()
                .objectId("test-object-001")
                .objectType("TABLE")
                .domainCode("finance")
                .name("updated_table")
                .displayName("更新后的表")
                .build();

        // When
        MetadataResponse response = metadataService.registerMetadata(updateRequest, null);

        // Then
        assertNotNull(response);
        verify(metadataObjectRepository).save(any(MetadataObject.class));
    }

    @Test
    @DisplayName("测试获取元数据")
    void testGetMetadata() {
        // Given
        when(metadataObjectRepository.findByObjectId("test-object-001"))
                .thenReturn(Optional.of(testMetadataObject));
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        // When
        MetadataResponse response = metadataService.getMetadata("test-object-001");

        // Then
        assertNotNull(response);
        assertEquals("test-object-001", response.getObjectId());
        assertEquals("test_table", response.getName());
        
        verify(metadataObjectRepository).findByObjectId("test-object-001");
    }

    @Test
    @DisplayName("测试获取不存在的元数据抛出异常")
    void testGetMetadata_NotFound() {
        // Given
        when(metadataObjectRepository.findByObjectId("non-existent"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> metadataService.getMetadata("non-existent"));
    }

    @Test
    @DisplayName("测试删除元数据")
    void testDeleteMetadata() {
        // Given
        when(metadataObjectRepository.findByObjectId("test-object-001"))
                .thenReturn(Optional.of(testMetadataObject));
        when(metadataObjectRepository.save(any(MetadataObject.class)))
                .thenReturn(testMetadataObject);

        // When
        metadataService.deleteMetadata("test-object-001");

        // Then
        verify(metadataObjectRepository).findByObjectId("test-object-001");
        verify(metadataObjectRepository).save(argThat(obj -> 
                "DEPRECATED".equals(obj.getStatus())));
    }

    @Test
    @DisplayName("测试搜索元数据分页")
    void testSearchMetadata_Pagination() {
        // Given
        MetadataSearchRequest searchRequest = MetadataSearchRequest.builder()
                .objectType("TABLE")
                .domainCode("finance")
                .page(0)
                .size(10)
                .build();

        List<MetadataObject> objects = Arrays.asList(testMetadataObject);
        Page<MetadataObject> page = new PageImpl<>(objects);

        when(metadataObjectRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        // When
        Page<MetadataResponse> result = metadataService.searchMetadata(searchRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("test-object-001", result.getContent().get(0).getObjectId());
    }

    @Test
    @DisplayName("测试按域查询元数据")
    void testGetMetadataByDomain() {
        // Given
        when(metadataObjectRepository.findByDomainCode("finance"))
                .thenReturn(Arrays.asList(testMetadataObject));
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        // When
        List<MetadataResponse> responses = metadataService.getMetadataByDomain("finance");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("finance", responses.get(0).getDomainCode());
    }

    @Test
    @DisplayName("测试获取元数据统计")
    void testGetMetadataStats() {
        // Given
        when(metadataObjectRepository.count()).thenReturn(100L);
        when(metadataObjectRepository.countByObjectType("TABLE")).thenReturn(50L);
        when(metadataObjectRepository.countByObjectType("API")).thenReturn(30L);
        when(metadataObjectRepository.countByObjectType("VIEW")).thenReturn(20L);
        when(metadataObjectRepository.countBySensitivity("INTERNAL")).thenReturn(60L);
        when(metadataObjectRepository.countBySensitivity("PUBLIC")).thenReturn(40L);
        when(metadataObjectRepository.findAll()).thenReturn(Arrays.asList(testMetadataObject));
        when(metadataObjectRepository.countByDomainCode("finance")).thenReturn(100L);

        // When
        Map<String, Object> stats = metadataService.getMetadataStats();

        // Then
        assertNotNull(stats);
        assertEquals(100L, stats.get("totalCount"));
        assertNotNull(stats.get("byObjectType"));
        assertNotNull(stats.get("bySensitivity"));
        assertNotNull(stats.get("byDomain"));
    }

    @Test
    @DisplayName("测试更新元数据")
    void testUpdateMetadata() {
        // Given
        when(metadataObjectRepository.findByObjectId("test-object-001"))
                .thenReturn(Optional.of(testMetadataObject));
        when(metadataObjectRepository.save(any(MetadataObject.class)))
                .thenReturn(testMetadataObject);
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        MetadataRegisterRequest updateRequest = MetadataRegisterRequest.builder()
                .objectId("test-object-001")
                .objectType("TABLE")
                .domainCode("hr")
                .name("updated_table")
                .build();

        // When
        MetadataResponse response = metadataService.updateMetadata("test-object-001", updateRequest, null);

        // Then
        assertNotNull(response);
        verify(metadataObjectRepository).save(any(MetadataObject.class));
    }

    @Test
    @DisplayName("测试同步资产元数据")
    void testSyncFromAsset() {
        // Given
        when(metadataObjectRepository.findByObjectId("asset-001"))
                .thenReturn(Optional.empty());
        when(metadataObjectRepository.save(any(MetadataObject.class)))
                .thenAnswer(invocation -> {
                    MetadataObject obj = invocation.getArgument(0);
                    obj.setId(1L);
                    return obj;
                });
        when(metadataFieldRepository.findByObjectId("asset-001"))
                .thenReturn(List.of());

        Map<String, Object> assetInfo = Map.of(
                "assetType", "TABLE",
                "domainCode", "sales",
                "name", "sales_data",
                "displayName", "销售数据",
                "description", "销售数据表",
                "owner", "sales_admin",
                "sensitivityLevel", "CONFIDENTIAL"
        );

        // When
        MetadataResponse response = metadataService.syncFromAsset("asset-001", assetInfo);

        // Then
        assertNotNull(response);
        assertEquals("asset-001", response.getObjectId());
        assertEquals("TABLE", response.getObjectType());
    }

    @Test
    @DisplayName("测试注册带字段的元数据")
    void testRegisterMetadataWithFields() {
        // Given
        when(metadataObjectRepository.findByObjectId("test-object-001"))
                .thenReturn(Optional.empty());
        when(metadataObjectRepository.save(any(MetadataObject.class)))
                .thenReturn(testMetadataObject);
        when(metadataFieldRepository.findByObjectId("test-object-001"))
                .thenReturn(List.of());

        List<MetadataFieldRequest> fields = Arrays.asList(
                MetadataFieldRequest.builder()
                        .fieldName("id")
                        .fieldType("BIGINT")
                        .nullable(false)
                        .primaryKey(true)
                        .ordinalPosition(0)
                        .build(),
                MetadataFieldRequest.builder()
                        .fieldName("name")
                        .fieldType("VARCHAR(255)")
                        .nullable(false)
                        .ordinalPosition(1)
                        .build()
        );

        // When
        MetadataResponse response = metadataService.registerMetadata(testRequest, fields);

        // Then
        assertNotNull(response);
        verify(metadataFieldRepository).saveAll(anyList());
    }
}
