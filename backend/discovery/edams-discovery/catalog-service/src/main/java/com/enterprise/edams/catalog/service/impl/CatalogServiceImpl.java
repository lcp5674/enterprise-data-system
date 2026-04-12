package com.enterprise.edams.catalog.service.impl;

import com.enterprise.edams.catalog.dto.CatalogCreateRequest;
import com.enterprise.edams.catalog.dto.CatalogDTO;
import com.enterprise.edams.catalog.entity.Catalog;
import com.enterprise.edams.catalog.mapper.CatalogMapper;
import com.enterprise.edams.catalog.service.CatalogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private CatalogMapper catalogMapper;

    @Override
    public List<CatalogDTO> getCatalogTree() {
        List<Catalog> all = catalogMapper.findAll();
        List<CatalogDTO> dtos = all.stream().map(this::toDTO).collect(Collectors.toList());
        return buildTree(dtos, null);
    }

    @Override
    public CatalogDTO getById(Long id) {
        Catalog catalog = catalogMapper.findById(id);
        if (catalog == null) {
            throw new RuntimeException("目录不存在: " + id);
        }
        return toDTO(catalog);
    }

    @Override
    @Transactional
    public CatalogDTO create(CatalogCreateRequest request) {
        Catalog catalog = new Catalog();
        BeanUtils.copyProperties(request, catalog);
        catalog.setStatus("ACTIVE");
        catalog.setCreateTime(LocalDateTime.now());
        catalog.setUpdateTime(LocalDateTime.now());
        catalogMapper.insert(catalog);
        log.info("Created catalog: id={}, name={}", catalog.getId(), catalog.getName());
        return toDTO(catalog);
    }

    @Override
    @Transactional
    public CatalogDTO update(Long id, CatalogCreateRequest request) {
        Catalog existing = catalogMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("目录不存在: " + id);
        }
        BeanUtils.copyProperties(request, existing);
        existing.setUpdateTime(LocalDateTime.now());
        catalogMapper.update(existing);
        return toDTO(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        long childCount = catalogMapper.countByParentId(id);
        if (childCount > 0) {
            throw new RuntimeException("存在子目录，无法删除");
        }
        catalogMapper.deleteById(id);
        log.info("Deleted catalog: id={}", id);
    }

    @Override
    public List<CatalogDTO> getChildren(Long parentId) {
        return catalogMapper.findByParentId(parentId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CatalogDTO> search(String keyword) {
        return catalogMapper.searchByKeyword(keyword)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", catalogMapper.countAll());
        stats.put("rootCount", catalogMapper.countByParentId(null));
        return stats;
    }

    private CatalogDTO toDTO(Catalog catalog) {
        CatalogDTO dto = new CatalogDTO();
        BeanUtils.copyProperties(catalog, dto);
        return dto;
    }

    private List<CatalogDTO> buildTree(List<CatalogDTO> all, Long parentId) {
        return all.stream()
                .filter(c -> Objects.equals(c.getParentId(), parentId))
                .peek(c -> c.setChildren(buildTree(all, c.getId())))
                .collect(Collectors.toList());
    }
}
