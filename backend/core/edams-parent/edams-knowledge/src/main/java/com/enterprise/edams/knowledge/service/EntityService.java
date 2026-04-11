package com.enterprise.edams.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.knowledge.dto.EntityDTO;
import com.enterprise.edams.knowledge.dto.EntityDetailDTO;
import com.enterprise.edams.knowledge.entity.Entity;

import java.util.List;

/**
 * 实体服务接口
 */
public interface EntityService extends IService<Entity> {

    /**
     * 分页查询实体
     */
    Page<Entity> selectPage(Long ontologyId, Long classId, int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    EntityDTO getById(Long id);

    /**
     * 根据唯一标识查询
     */
    EntityDTO getByUniqueId(String uniqueId);

    /**
     * 创建实体
     */
    EntityDTO create(EntityDTO dto);

    /**
     * 更新实体
     */
    EntityDTO update(Long id, EntityDTO dto);

    /**
     * 删除实体
     */
    void delete(Long id);

    /**
     * 搜索实体
     */
    List<EntityDTO> search(String keyword, Long ontologyId, int limit);

    /**
     * 获取实体详情
     */
    EntityDetailDTO getDetail(Long id);

    /**
     * 获取热门实体
     */
    List<EntityDTO> getHotEntities(int limit);

    /**
     * 增加访问次数
     */
    void incrementViewCount(Long id);

    /**
     * 批量创建实体
     */
    List<EntityDTO> batchCreate(List<EntityDTO> dtoList);

    /**
     * 关联资产
     */
    EntityDTO linkAsset(Long entityId, Long assetId, String assetType);
}
