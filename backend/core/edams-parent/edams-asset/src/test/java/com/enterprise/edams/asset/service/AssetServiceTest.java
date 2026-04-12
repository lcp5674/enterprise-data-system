package com.enterprise.edams.asset.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.asset.dto.AssetCreateRequest;
import com.enterprise.edams.asset.dto.AssetDTO;
import com.enterprise.edams.asset.dto.AssetQueryRequest;
import com.enterprise.edams.asset.dto.AssetUpdateRequest;
import com.enterprise.edams.asset.entity.Asset;
import com.enterprise.edams.asset.entity.AssetTag;
import com.enterprise.edams.asset.entity.AssetTagRelation;
import com.enterprise.edams.asset.repository.AssetMapper;
import com.enterprise.edams.asset.repository.AssetTagMapper;
import com.enterprise.edams.asset.repository.AssetTagRelationMapper;
import com.enterprise.edams.asset.service.impl.AssetServiceImpl;
import com.enterprise.edams.common.enums.AssetStatus;
import com.enterprise.edams.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 资产服务单元测试
 * 测试AssetServiceImpl的核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("资产服务测试")
class AssetServiceTest {

    @Mock
    private AssetMapper assetMapper;

    @Mock
    private AssetTagMapper tagMapper;

    @Mock
    private AssetTagRelationMapper tagRelationMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AssetServiceImpl assetService;

    private Asset testAsset;
    private AssetCreateRequest createRequest;
    private AssetUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAsset = Asset.builder()
                .id(1L)
                .assetName("测试资产")
                .assetCode("ASSET001")
                .assetType("TABLE")
                .description("测试描述")
                .ownerId(100L)
                .sensitivity("HIGH")
                .status(AssetStatus.DRAFT)
                .version(1)
                .createdBy("admin")
                .createdTime(LocalDateTime.now())
                .updatedBy("admin")
                .updatedTime(LocalDateTime.now())
                .build();

        createRequest = new AssetCreateRequest();
        createRequest.setAssetName("新资产");
        createRequest.setAssetType("TABLE");
        createRequest.setDescription("新描述");
        createRequest.setOwnerId(100L);
        createRequest.setSensitivity("MEDIUM");
        createRequest.setTagIds(Arrays.asList(1L, 2L));

        updateRequest = new AssetUpdateRequest();
        updateRequest.setAssetName("更新资产");
        updateRequest.setDescription("更新描述");
        updateRequest.setOwnerId(100L);
        updateRequest.setSensitivity("LOW");
        updateRequest.setTagIds(Arrays.asList(1L));
    }

    @Test
    @DisplayName("测试创建资产 - 成功")
    void testCreateAsset_Success() {
        // Given
        when(assetMapper.existsByAssetName("新资产")).thenReturn(0L);
        when(assetMapper.existsByAssetCode(any())).thenReturn(0L);
        when(assetMapper.insert(any(Asset.class))).thenReturn(1);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        AssetDTO result = assetService.createAsset(createRequest, "admin");

        // Then
        assertNotNull(result);
        assertEquals("新资产", result.getAssetName());
        verify(assetMapper, times(1)).insert(any(Asset.class));
    }

    @Test
    @DisplayName("测试创建资产 - 资产名称已存在")
    void testCreateAsset_DuplicateName_ThrowsException() {
        // Given
        when(assetMapper.existsByAssetName("新资产")).thenReturn(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.createAsset(createRequest, "admin");
        });
        assertEquals("资产名称已存在: 新资产", exception.getMessage());
    }

    @Test
    @DisplayName("测试创建资产 - 自定义资产编码已存在")
    void testCreateAsset_DuplicateCode_ThrowsException() {
        // Given
        createRequest.setAssetCode("ASSET001");
        when(assetMapper.existsByAssetName("新资产")).thenReturn(0L);
        when(assetMapper.existsByAssetCode("ASSET001")).thenReturn(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.createAsset(createRequest, "admin");
        });
        assertEquals("资产编码已存在: ASSET001", exception.getMessage());
    }

    @Test
    @DisplayName("测试创建资产 - 带标签")
    void testCreateAsset_WithTags() {
        // Given
        createRequest.setTagIds(Arrays.asList(1L, 2L));
        when(assetMapper.existsByAssetName("新资产")).thenReturn(0L);
        when(assetMapper.existsByAssetCode(any())).thenReturn(0L);
        when(assetMapper.insert(any(Asset.class))).thenAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            asset.setId(1L);
            return 1;
        });
        when(tagRelationMapper.exists(1L, 1L)).thenReturn(0L);
        when(tagRelationMapper.exists(1L, 2L)).thenReturn(0L);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        AssetDTO result = assetService.createAsset(createRequest, "admin");

        // Then
        assertNotNull(result);
        verify(tagRelationMapper, times(2)).insert(any(AssetTagRelation.class));
    }

    @Test
    @DisplayName("测试更新资产 - 成功")
    void testUpdateAsset_Success() {
        // Given
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(assetMapper.selectByAssetName("更新资产")).thenReturn(null);
        when(assetMapper.updateById(any(Asset.class))).thenReturn(1);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(tagMapper.selectTagsByAssetId(1L)).thenReturn(Collections.emptyList());

        // When
        AssetDTO result = assetService.updateAsset(1L, updateRequest, "admin");

        // Then
        assertNotNull(result);
        verify(assetMapper, times(1)).updateById(any(Asset.class));
        verify(assetMapper, times(1)).incrementVersion(1L);
    }

    @Test
    @DisplayName("测试更新资产 - 资产不存在")
    void testUpdateAsset_NotFound_ThrowsException() {
        // Given
        when(assetMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            AssetUpdateRequest request = new AssetUpdateRequest();
            request.setAssetName("测试");
            assetService.updateAsset(999L, request, "admin");
        });
        assertEquals("资产不存在: 999", exception.getMessage());
    }

    @Test
    @DisplayName("测试更新资产 - 已发布状态不可编辑")
    void testUpdateAsset_NotEditable_ThrowsException() {
        // Given
        testAsset.setStatus(AssetStatus.PUBLISHED);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.updateAsset(1L, updateRequest, "admin");
        });
        assertTrue(exception.getMessage().contains("当前状态不允许编辑"));
    }

    @Test
    @DisplayName("测试更新资产 - 重复名称")
    void testUpdateAsset_DuplicateName_ThrowsException() {
        // Given
        Asset existingAsset = Asset.builder().id(2L).assetName("更新资产").build();
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(assetMapper.selectByAssetName("更新资产")).thenReturn(existingAsset);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.updateAsset(1L, updateRequest, "admin");
        });
        assertEquals("资产名称已存在: 更新资产", exception.getMessage());
    }

    @Test
    @DisplayName("测试删除资产 - 成功")
    void testDeleteAsset_Success() {
        // Given
        testAsset.setStatus(AssetStatus.DRAFT);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        doNothing().when(tagRelationMapper).deleteByAssetId(1L);
        when(assetMapper.deleteById(1L)).thenReturn(1);

        // When
        assetService.deleteAsset(1L, "admin");

        // Then
        verify(tagRelationMapper, times(1)).deleteByAssetId(1L);
        verify(assetMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("测试删除资产 - 已发布状态不能删除")
    void testDeleteAsset_Published_ThrowsException() {
        // Given
        testAsset.setStatus(AssetStatus.PUBLISHED);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.deleteAsset(1L, "admin");
        });
        assertEquals("已发布资产不能直接删除,请先归档", exception.getMessage());
    }

    @Test
    @DisplayName("测试获取资产 - 成功")
    void testGetAssetById_Success() {
        // Given
        List<AssetTag> tags = Arrays.asList(
                AssetTag.builder().id(1L).tagName("tag1").build(),
                AssetTag.builder().id(2L).tagName("tag2").build()
        );
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(tagMapper.selectTagsByAssetId(1L)).thenReturn(tags);

        // When
        AssetDTO result = assetService.getAssetById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(2, result.getTagList().size());
    }

    @Test
    @DisplayName("测试获取资产 - 不存在")
    void testGetAssetById_NotFound_ThrowsException() {
        // Given
        when(assetMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.getAssetById(999L);
        });
        assertEquals("资产不存在: 999", exception.getMessage());
    }

    @Test
    @DisplayName("测试按编码获取资产 - 成功")
    void testGetAssetByCode_Success() {
        // Given
        when(assetMapper.selectByAssetCode("ASSET001")).thenReturn(testAsset);
        when(tagMapper.selectTagsByAssetId(1L)).thenReturn(Collections.emptyList());

        // When
        AssetDTO result = assetService.getAssetByCode("ASSET001");

        // Then
        assertNotNull(result);
        assertEquals("ASSET001", result.getAssetCode());
    }

    @Test
    @DisplayName("测试按编码获取资产 - 不存在")
    void testGetAssetByCode_NotFound_ThrowsException() {
        // Given
        when(assetMapper.selectByAssetCode("NOTFOUND")).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.getAssetByCode("NOTFOUND");
        });
        assertEquals("资产不存在: NOTFOUND", exception.getMessage());
    }

    @Test
    @DisplayName("测试分页查询资产 - 成功")
    void testQueryAssets_Success() {
        // Given
        AssetQueryRequest request = new AssetQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setKeyword("测试");

        Page<Asset> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAsset));
        page.setTotal(1);

        when(assetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        IPage<AssetDTO> result = assetService.queryAssets(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("测试分页查询资产 - 带多条件筛选")
    void testQueryAssets_WithFilters() {
        // Given
        AssetQueryRequest request = new AssetQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setAssetTypes(Arrays.asList("TABLE", "VIEW"));
        request.setStatuses(Arrays.asList(AssetStatus.DRAFT, AssetStatus.PUBLISHED));
        request.setOwnerId(100L);

        Page<Asset> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAsset));
        page.setTotal(1);

        when(assetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        IPage<AssetDTO> result = assetService.queryAssets(request);

        // Then
        assertNotNull(result);
        verify(assetMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试搜索资产 - 空关键词")
    void testSearchAssets_EmptyKeyword_ReturnsEmptyList() {
        // When
        List<AssetDTO> result = assetService.searchAssets("");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(assetMapper, never()).selectList(any());
    }

    @Test
    @DisplayName("测试搜索资产 - 成功")
    void testSearchAssets_Success() {
        // Given
        when(assetMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testAsset));
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        List<AssetDTO> result = assetService.searchAssets("测试");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("测试更新资产状态 - 成功")
    void testUpdateAssetStatus_Success() {
        // Given
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(assetMapper.updateById(any(Asset.class))).thenReturn(1);

        // When
        assetService.updateAssetStatus(1L, AssetStatus.REVIEWING, "admin");

        // Then
        verify(assetMapper, times(1)).updateById(any(Asset.class));
    }

    @Test
    @DisplayName("测试发布资产 - 成功")
    void testPublishAsset_Success() {
        // Given
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(assetMapper.updateById(any(Asset.class))).thenReturn(1);

        // When
        assetService.publishAsset(1L, "admin");

        // Then
        verify(assetMapper, times(1)).updateById(any(Asset.class));
    }

    @Test
    @DisplayName("测试发布资产 - 归档状态不可发布")
    void testPublishAsset_Archived_ThrowsException() {
        // Given
        testAsset.setStatus(AssetStatus.ARCHIVED);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.publishAsset(1L, "admin");
        });
        assertTrue(exception.getMessage().contains("当前状态不允许发布"));
    }

    @Test
    @DisplayName("测试归档资产 - 成功")
    void testArchiveAsset_Success() {
        // Given
        testAsset.setStatus(AssetStatus.PUBLISHED);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(assetMapper.updateById(any(Asset.class))).thenReturn(1);

        // When
        assetService.archiveAsset(1L, "admin");

        // Then
        verify(assetMapper, times(1)).updateById(any(Asset.class));
    }

    @Test
    @DisplayName("测试获取资产标签 - 成功")
    void testGetAssetTags_Success() {
        // Given
        List<AssetTag> tags = Arrays.asList(
                AssetTag.builder().id(1L).tagName("tag1").build(),
                AssetTag.builder().id(2L).tagName("tag2").build()
        );
        when(tagMapper.selectTagsByAssetId(1L)).thenReturn(tags);

        // When
        List<String> result = assetService.getAssetTags(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("tag1"));
        assertTrue(result.contains("tag2"));
    }

    @Test
    @DisplayName("测试资产打标签 - 成功")
    void testTagAsset_Success() {
        // Given
        when(tagRelationMapper.deleteByAssetId(1L)).thenReturn(1);
        when(tagRelationMapper.exists(1L, 1L)).thenReturn(0L);
        when(tagMapper.incrementUsageCount(eq(1L), any(LocalDateTime.class))).thenReturn(1);

        // When
        assetService.tagAsset(1L, Arrays.asList(1L), "admin");

        // Then
        verify(tagRelationMapper, times(1)).deleteByAssetId(1L);
        verify(tagRelationMapper, times(1)).insert(any(AssetTagRelation.class));
    }

    @Test
    @DisplayName("测试资产解标签 - 成功")
    void testUntagAsset_Success() {
        // Given
        when(tagRelationMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(tagMapper.decrementUsageCount(1L)).thenReturn(1);

        // When
        assetService.untagAsset(1L, Arrays.asList(1L));

        // Then
        verify(tagRelationMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(tagMapper, times(1)).decrementUsageCount(1L);
    }

    @Test
    @DisplayName("测试资产解标签 - 空列表")
    void testUntagAsset_EmptyList_NoAction() {
        // When
        assetService.untagAsset(1L, Collections.emptyList());

        // Then
        verify(tagRelationMapper, never()).delete(any());
    }

    @Test
    @DisplayName("测试按目录获取资产 - 成功")
    void testGetAssetsByCatalog_Success() {
        // Given
        Page<Asset> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAsset));
        page.setTotal(1);

        when(assetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        IPage<AssetDTO> result = assetService.getAssetsByCatalog(1L, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试按领域获取资产 - 成功")
    void testGetAssetsByDomain_Success() {
        // Given
        Page<Asset> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAsset));
        page.setTotal(1);

        when(assetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        IPage<AssetDTO> result = assetService.getAssetsByDomain(1L, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试按所有者获取资产 - 成功")
    void testGetAssetsByOwner_Success() {
        // Given
        Page<Asset> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAsset));
        page.setTotal(1);

        when(assetMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(tagMapper.selectTagsByAssetId(any())).thenReturn(Collections.emptyList());

        // When
        IPage<AssetDTO> result = assetService.getAssetsByOwner(100L, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("测试统计资产数量 - 按所有者")
    void testCountAssetsByOwner_Success() {
        // Given
        when(assetMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        // When
        long result = assetService.countAssetsByOwner(100L);

        // Then
        assertEquals(10L, result);
    }

    @Test
    @DisplayName("测试统计资产数量 - 按领域")
    void testCountAssetsByDomain_Success() {
        // Given
        when(assetMapper.countByDomainId(1L)).thenReturn(20L);

        // When
        long result = assetService.countAssetsByDomain(1L);

        // Then
        assertEquals(20L, result);
    }

    @Test
    @DisplayName("测试同步资产元数据 - 成功")
    void testSyncAssetMetadata_Success() {
        // Given
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(assetMapper.updateById(any(Asset.class))).thenReturn(1);

        // When
        assetService.syncAssetMetadata(1L, "admin");

        // Then
        verify(assetMapper, times(1)).updateById(any(Asset.class));
    }

    @Test
    @DisplayName("测试同步资产元数据 - 资产不存在")
    void testSyncAssetMetadata_NotFound_ThrowsException() {
        // Given
        when(assetMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            assetService.syncAssetMetadata(999L, "admin");
        });
        assertEquals("资产不存在: 999", exception.getMessage());
    }

    @Test
    @DisplayName("测试操作人ID解析 - 数字字符串")
    void testParseOperatorToLong_NumericString() {
        // This is tested indirectly through addTagsToAsset
        // Given
        when(assetMapper.existsByAssetName(any())).thenReturn(0L);
        when(assetMapper.existsByAssetCode(any())).thenReturn(0L);
        when(assetMapper.insert(any(Asset.class))).thenAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            asset.setId(1L);
            return 1;
        });

        createRequest.setTagIds(Arrays.asList(1L));
        when(tagRelationMapper.exists(1L, 1L)).thenReturn(0L);

        // When - 使用数字字符串作为operator
        assetService.createAsset(createRequest, "12345");

        // Then - 不应抛出异常
        verify(assetMapper, times(1)).insert(any(Asset.class));
    }

    @Test
    @DisplayName("测试操作人ID解析 - 用户名字符串")
    void testParseOperatorToLong_UserName() {
        // This is tested indirectly through addTagsToAsset
        // Given
        when(assetMapper.existsByAssetName(any())).thenReturn(0L);
        when(assetMapper.existsByAssetCode(any())).thenReturn(0L);
        when(assetMapper.insert(any(Asset.class))).thenAnswer(invocation -> {
            Asset asset = invocation.getArgument(0);
            asset.setId(1L);
            return 1;
        });

        createRequest.setTagIds(Arrays.asList(1L));
        when(tagRelationMapper.exists(1L, 1L)).thenReturn(0L);

        // When - 使用用户名字符串作为operator
        assetService.createAsset(createRequest, "admin");

        // Then - 不应抛出NumberFormatException
        verify(assetMapper, times(1)).insert(any(Asset.class));
    }
}
