package com.enterprise.edams.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.asset.dto.AssetCatalogCreateRequest;
import com.enterprise.edams.asset.dto.AssetCatalogDTO;
import com.enterprise.edams.asset.entity.AssetCatalog;
import com.enterprise.edams.asset.repository.AssetCatalogMapper;
import com.enterprise.edams.asset.repository.AssetMapper;
import com.enterprise.edams.asset.service.AssetCatalogService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资产目录服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCatalogServiceImpl implements AssetCatalogService {

    private final AssetCatalogMapper catalogMapper;
    private final AssetMapper assetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetCatalogDTO createCatalog(AssetCatalogCreateRequest request, String operator) {
        // 校验同级目录名称唯一性
        if (catalogMapper.existsByNameAndParentId(request.getName(), request.getParentId()) > 0) {
            throw new BusinessException("同级目录下已存在相同名称的目录: " + request.getName());
        }

        // 生成目录编码
        String code = StringUtils.hasText(request.getCode()) ? request.getCode() : generateCatalogCode();
        if (catalogMapper.existsByCode(code) > 0) {
            throw new BusinessException("目录编码已存在: " + code);
        }

        // 计算层级和路径
        int level = 0;
        String path = "/" + code;
        
        if (request.getParentId() != null && request.getParentId() > 0) {
            AssetCatalog parent = catalogMapper.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException("父目录不存在: " + request.getParentId());
            }
            level = parent.getLevel() + 1;
            path = parent.getPath() + "/" + code;
        }

        // 获取排序号
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            Integer maxSort = catalogMapper.selectMaxSortOrder(request.getParentId());
            sortOrder = maxSort == null ? 1 : maxSort + 1;
        }

        AssetCatalog catalog = new AssetCatalog();
        catalog.setName(request.getName());
        catalog.setCode(code);
        catalog.setParentId(request.getParentId());
        catalog.setLevel(level);
        catalog.setPath(path);
        catalog.setSortOrder(sortOrder);
        catalog.setDescription(request.getDescription());
        catalog.setIcon(request.getIcon());
        catalog.setStatus(1);
        catalog.setCreatedBy(operator);
        catalog.setUpdatedBy(operator);

        catalogMapper.insert(catalog);
        
        log.info("目录创建成功: id={}, name={}", catalog.getId(), catalog.getName());
        return convertToDTO(catalog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetCatalogDTO updateCatalog(Long id, AssetCatalogCreateRequest request, String operator) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }

        // 检查是否为根目录(不允许修改)
        if (catalog.getParentId() == 0 && catalog.getLevel() == 0) {
            throw new BusinessException("根目录不允许修改");
        }

        // 校验同级目录名称唯一性(排除自己)
        AssetCatalog existCatalog = catalogMapper.selectByName(request.getName());
        if (existCatalog != null && !existCatalog.getId().equals(id)) {
            throw new BusinessException("同级目录下已存在相同名称的目录: " + request.getName());
        }

        catalog.setName(request.getName());
        catalog.setDescription(request.getDescription());
        catalog.setIcon(request.getIcon());
        catalog.setSortOrder(request.getSortOrder());
        catalog.setUpdatedBy(operator);

        catalogMapper.updateById(catalog);
        
        log.info("目录更新成功: id={}", id);
        return convertToDTO(catalog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCatalog(Long id, String operator) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }

        // 检查是否有子目录
        if (catalogMapper.countChildren(id) > 0) {
            throw new BusinessException("该目录下存在子目录,请先删除子目录");
        }

        // 检查是否有关联资产
        if (assetMapper.countByCatalogId(id) > 0) {
            throw new BusinessException("该目录下存在资产,请先移除资产");
        }

        catalogMapper.deleteById(id);
        log.info("目录删除成功: id={}", id);
    }

    @Override
    public AssetCatalogDTO getCatalogById(Long id) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }
        return convertToDTO(catalog);
    }

    @Override
    public AssetCatalogDTO getCatalogByCode(String code) {
        AssetCatalog catalog = catalogMapper.selectByCode(code);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + code);
        }
        return convertToDTO(catalog);
    }

    @Override
    public List<AssetCatalogDTO> getRootCatalogs() {
        return catalogMapper.selectByParentId(0L).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetCatalogDTO> getChildCatalogs(Long parentId) {
        return catalogMapper.selectByParentId(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AssetCatalogDTO> getCatalogTree() {
        // 查询所有启用的目录
        List<AssetCatalog> allCatalogs = catalogMapper.selectAllEnabled();
        
        // 构建树形结构
        return buildCatalogTree(allCatalogs, 0L);
    }

    @Override
    public List<AssetCatalogDTO> getFullCatalogTree() {
        // 查询所有目录(包括禁用的)
        LambdaQueryWrapper<AssetCatalog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AssetCatalog::getSortOrder);
        List<AssetCatalog> allCatalogs = catalogMapper.selectList(wrapper);
        
        return buildCatalogTree(allCatalogs, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveCatalog(Long id, Long newParentId, String operator) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }

        // 不能移动到自己
        if (id.equals(newParentId)) {
            throw new BusinessException("不能将目录移动到自己下面");
        }

        // 检查是否移动到子目录下
        if (isDescendant(newParentId, id)) {
            throw new BusinessException("不能将目录移动到自己的子目录下");
        }

        // 更新父目录ID和层级
        AssetCatalog newParent = catalogMapper.selectById(newParentId);
        if (newParent == null) {
            throw new BusinessException("目标父目录不存在: " + newParentId);
        }

        int newLevel = newParent.getLevel() + 1;
        String newPath = newParent.getPath() + "/" + catalog.getCode();

        catalog.setParentId(newParentId);
        catalog.setLevel(newLevel);
        catalog.setPath(newPath);
        catalog.setUpdatedBy(operator);
        
        catalogMapper.updateById(catalog);
        
        // 递归更新所有子目录的路径
        updateChildrenPath(catalog.getId(), newPath, newLevel);
        
        log.info("目录移动成功: id={}, newParentId={}", id, newParentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(Long id, Integer sortOrder, String operator) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }
        
        catalog.setSortOrder(sortOrder);
        catalog.setUpdatedBy(operator);
        catalogMapper.updateById(catalog);
        
        log.info("目录排序更新成功: id={}, sortOrder={}", id, sortOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableCatalog(Long id, String operator) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }
        
        catalog.setStatus(1);
        catalog.setUpdatedBy(operator);
        catalogMapper.updateById(catalog);
        
        log.info("目录启用成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableCatalog(Long id, String operator) {
        AssetCatalog catalog = catalogMapper.selectById(id);
        if (catalog == null) {
            throw new BusinessException("目录不存在: " + id);
        }
        
        catalog.setStatus(0);
        catalog.setUpdatedBy(operator);
        catalogMapper.updateById(catalog);
        
        log.info("目录禁用成功: id={}", id);
    }

    @Override
    public int countAssetsInCatalog(Long catalogId) {
        return assetMapper.countByCatalogId(catalogId);
    }

    private List<AssetCatalogDTO> buildCatalogTree(List<AssetCatalog> catalogs, Long parentId) {
        List<AssetCatalogDTO> result = new ArrayList<>();
        
        Map<Long, List<AssetCatalog>> parentMap = catalogs.stream()
                .collect(Collectors.groupingBy(AssetCatalog::getParentId));
        
        List<AssetCatalog> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        
        for (AssetCatalog catalog : children) {
            AssetCatalogDTO dto = convertToDTO(catalog);
            dto.setAssetCount(assetMapper.countByCatalogId(catalog.getId()));
            dto.setIsLeaf(catalogMapper.countChildren(catalog.getId()) == 0);
            
            // 递归构建子目录
            List<AssetCatalogDTO> subChildren = buildCatalogTree(catalogs, catalog.getId());
            if (!subChildren.isEmpty()) {
                dto.setChildren(subChildren);
            }
            
            result.add(dto);
        }
        
        return result;
    }

    private boolean isDescendant(Long parentId, Long childId) {
        if (parentId == null || childId == null) {
            return false;
        }
        
        List<AssetCatalog> children = catalogMapper.selectByParentId(childId);
        for (AssetCatalog child : children) {
            if (child.getId().equals(parentId)) {
                return true;
            }
            if (isDescendant(parentId, child.getId())) {
                return true;
            }
        }
        return false;
    }

    private void updateChildrenPath(Long parentId, String parentPath, int parentLevel) {
        List<AssetCatalog> children = catalogMapper.selectByParentId(parentId);
        
        for (AssetCatalog child : children) {
            String newPath = parentPath + "/" + child.getCode();
            int newLevel = parentLevel + 1;
            
            child.setPath(newPath);
            child.setLevel(newLevel);
            catalogMapper.updateById(child);
            
            // 递归更新子目录
            updateChildrenPath(child.getId(), newPath, newLevel);
        }
    }

    private String generateCatalogCode() {
        return "CAT" + System.currentTimeMillis();
    }

    private AssetCatalogDTO convertToDTO(AssetCatalog catalog) {
        AssetCatalogDTO dto = new AssetCatalogDTO();
        BeanUtils.copyProperties(catalog, dto);
        return dto;
    }
}
