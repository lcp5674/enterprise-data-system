package com.enterprise.edams.catalog.service;

import com.enterprise.edams.catalog.dto.CatalogDTO;
import com.enterprise.edams.catalog.dto.CatalogCreateRequest;
import java.util.List;
import java.util.Map;

public interface CatalogService {
    List<CatalogDTO> getCatalogTree();
    CatalogDTO getById(Long id);
    CatalogDTO create(CatalogCreateRequest request);
    CatalogDTO update(Long id, CatalogCreateRequest request);
    void delete(Long id);
    List<CatalogDTO> getChildren(Long parentId);
    List<CatalogDTO> search(String keyword);
    Map<String, Object> getStats();
}
