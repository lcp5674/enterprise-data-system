package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.knowledge.dto.RelationDTO;
import com.enterprise.edams.knowledge.entity.Entity;
import com.enterprise.edams.knowledge.entity.Relation;
import com.enterprise.edams.knowledge.repository.EntityMapper;
import com.enterprise.edams.knowledge.repository.RelationMapper;
import com.enterprise.edams.knowledge.service.RelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelationServiceImpl extends ServiceImpl<RelationMapper, Relation> implements RelationService {

    private final RelationMapper relationMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<RelationDTO> getByEntityId(Long entityId) {
        List<Relation> relations = relationMapper.selectByEntityId(entityId);
        return relations.stream().map(this::convertToDTO).toList();
    }

    @Override
    public RelationDTO getById(Long id) {
        Relation relation = baseMapper.selectById(id);
        if (relation == null) {
            return null;
        }
        return convertToDTO(relation);
    }

    @Override
    @Transactional
    public RelationDTO create(RelationDTO dto) {
        Relation relation = new Relation();
        BeanUtils.copyProperties(dto, relation);
        relation.setStatus("ACTIVE");
        
        // 获取源实体名称
        if (dto.getSourceEntityId() != null) {
            Entity source = entityMapper.selectById(dto.getSourceEntityId());
            if (source != null) {
                relation.setSourceEntityName(source.getName());
            }
        }
        
        // 获取目标实体名称
        if (dto.getTargetEntityId() != null) {
            Entity target = entityMapper.selectById(dto.getTargetEntityId());
            if (target != null) {
                relation.setTargetEntityName(target.getName());
            }
        }
        
        baseMapper.insert(relation);
        log.info("Created relation: {}", relation.getId());
        return convertToDTO(relation);
    }

    @Override
    @Transactional
    public RelationDTO update(Long id, RelationDTO dto) {
        Relation relation = baseMapper.selectById(id);
        if (relation == null) {
            throw new RuntimeException("Relation not found: " + id);
        }
        BeanUtils.copyProperties(dto, relation, "id", "createTime", "creator");
        baseMapper.updateById(relation);
        log.info("Updated relation: {}", id);
        return convertToDTO(relation);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Relation relation = baseMapper.selectById(id);
        if (relation != null) {
            relation.setStatus("DELETED");
            baseMapper.updateById(relation);
            log.info("Deleted relation: {}", id);
        }
    }

    @Override
    public List<RelationDTO> getEntityRelations(Long entityId) {
        List<Relation> relations = new ArrayList<>();
        relations.addAll(relationMapper.selectOutgoingRelations(entityId));
        relations.addAll(relationMapper.selectIncomingRelations(entityId));
        return relations.stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<RelationDTO> getBetweenEntities(Long sourceId, Long targetId) {
        List<Relation> relations = relationMapper.selectBetweenEntities(sourceId, targetId);
        return relations.stream().map(this::convertToDTO).toList();
    }

    @Override
    public Long countByType(Long ontologyId, String relationType) {
        return relationMapper.countByOntologyId(ontologyId);
    }

    @Override
    @Transactional
    public List<RelationDTO> batchCreate(List<RelationDTO> dtoList) {
        return dtoList.stream().map(this::create).toList();
    }

    private RelationDTO convertToDTO(Relation relation) {
        RelationDTO dto = new RelationDTO();
        BeanUtils.copyProperties(relation, dto);
        return dto;
    }
}
