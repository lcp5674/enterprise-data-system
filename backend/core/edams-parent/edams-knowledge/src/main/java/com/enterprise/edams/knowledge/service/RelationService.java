package com.enterprise.edams.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.knowledge.dto.RelationDTO;
import com.enterprise.edams.knowledge.entity.Relation;

import java.util.List;

/**
 * 关系服务接口
 */
public interface RelationService extends IService<Relation> {

    /**
     * 根据实体ID查询关系
     */
    List<RelationDTO> getByEntityId(Long entityId);

    /**
     * 根据ID查询
     */
    RelationDTO getById(Long id);

    /**
     * 创建关系
     */
    RelationDTO create(RelationDTO dto);

    /**
     * 更新关系
     */
    RelationDTO update(Long id, RelationDTO dto);

    /**
     * 删除关系
     */
    void delete(Long id);

    /**
     * 获取实体的所有关系 (出边+入边)
     */
    List<RelationDTO> getEntityRelations(Long entityId);

    /**
     * 获取两个实体间的关系
     */
    List<RelationDTO> getBetweenEntities(Long sourceId, Long targetId);

    /**
     * 获取指定类型的关系统计
     */
    Long countByType(Long ontologyId, String relationType);

    /**
     * 批量创建关系
     */
    List<RelationDTO> batchCreate(List<RelationDTO> dtoList);
}
