package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.knowledge.dto.EntityDTO;
import com.enterprise.edams.knowledge.dto.EntityDetailDTO;
import com.enterprise.edams.knowledge.entity.Entity;
import com.enterprise.edams.knowledge.entity.Relation;
import com.enterprise.edams.knowledge.repository.EntityMapper;
import com.enterprise.edams.knowledge.repository.RelationMapper;
import com.enterprise.edams.knowledge.service.EntityService;
import com.enterprise.edams.knowledge.service.RelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 实体服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityServiceImpl extends ServiceImpl<EntityMapper, Entity> implements EntityService {

    private final EntityMapper entityMapper;
    private final RelationMapper relationMapper;
    private final RelationService relationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VIEW_COUNT_KEY = "knowledge:entity:views:";

    @Override
    public Page<Entity> selectPage(Long ontologyId, Long classId, int pageNum, int pageSize) {
        Page<Entity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
        if (ontologyId != null) {
            wrapper.eq(Entity::getOntologyId, ontologyId);
        }
        if (classId != null) {
            wrapper.eq(Entity::getClassId, classId);
        }
        wrapper.eq(Entity::getStatus, "ACTIVE");
        wrapper.orderByDesc(Entity::getUpdateTime);
        return page(page, wrapper);
    }

    @Override
    public EntityDTO getById(Long id) {
        Entity entity = baseMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return convertToDTO(entity);
    }

    @Override
    public EntityDTO getByUniqueId(String uniqueId) {
        Entity entity = entityMapper.selectByUniqueId(uniqueId);
        if (entity == null) {
            return null;
        }
        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public EntityDTO create(EntityDTO dto) {
        Entity entity = new Entity();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("ACTIVE");
        entity.setViewCount(0L);
        entity.setFavoriteCount(0);
        baseMapper.insert(entity);
        log.info("Created entity: {}", entity.getId());
        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public EntityDTO update(Long id, EntityDTO dto) {
        Entity entity = baseMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("Entity not found: " + id);
        }
        BeanUtils.copyProperties(dto, entity, "id", "createTime", "creator", "viewCount", "favoriteCount");
        baseMapper.updateById(entity);
        log.info("Updated entity: {}", id);
        return convertToDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Entity entity = baseMapper.selectById(id);
        if (entity != null) {
            entity.setStatus("DELETED");
            baseMapper.updateById(entity);
            log.info("Deleted entity: {}", id);
        }
    }

    @Override
    public List<EntityDTO> search(String keyword, Long ontologyId, int limit) {
        List<Entity> entities = entityMapper.searchEntities(keyword, limit);
        return entities.stream()
                .filter(e -> ontologyId == null || ontologyId.equals(e.getOntologyId()))
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public EntityDetailDTO getDetail(Long id) {
        EntityDetailDTO detail = new EntityDetailDTO();
        
        // 获取实体信息
        Entity entity = baseMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        detail.setEntity(convertToDTO(entity));
        
        // 获取出边关系
        List<Relation> outgoing = relationMapper.selectOutgoingRelations(id);
        detail.setOutgoingRelations(outgoing.stream().map(this::convertRelationToDTO).toList());
        
        // 获取入边关系
        List<Relation> incoming = relationMapper.selectIncomingRelations(id);
        detail.setIncomingRelations(incoming.stream().map(this::convertRelationToDTO).toList());
        
        return detail;
    }

    @Override
    public List<EntityDTO> getHotEntities(int limit) {
        List<Entity> entities = entityMapper.selectHotEntities(limit);
        return entities.stream().map(this::convertToDTO).toList();
    }

    @Override
    public void incrementViewCount(Long id) {
        String key = VIEW_COUNT_KEY + id;
        Long views = redisTemplate.opsForValue().increment(key);
        if (views != null && views % 10 == 0) {
            // 每10次访问持久化一次
            Entity entity = baseMapper.selectById(id);
            if (entity != null) {
                entity.setViewCount(entity.getViewCount() + 10);
                baseMapper.updateById(entity);
            }
        }
    }

    @Override
    @Transactional
    public List<EntityDTO> batchCreate(List<EntityDTO> dtoList) {
        return dtoList.stream().map(this::create).toList();
    }

    @Override
    @Transactional
    public EntityDTO linkAsset(Long entityId, Long assetId, String assetType) {
        Entity entity = baseMapper.selectById(entityId);
        if (entity == null) {
            throw new RuntimeException("Entity not found: " + entityId);
        }
        entity.setAssetId(assetId);
        entity.setAssetType(assetType);
        baseMapper.updateById(entity);
        log.info("Linked asset to entity: entityId={}, assetId={}, assetType={}", entityId, assetId, assetType);
        return convertToDTO(entity);
    }

    private EntityDTO convertToDTO(Entity entity) {
        EntityDTO dto = new EntityDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private com.enterprise.edams.knowledge.dto.RelationDTO convertRelationToDTO(Relation relation) {
        com.enterprise.edams.knowledge.dto.RelationDTO dto = new com.enterprise.edams.knowledge.dto.RelationDTO();
        BeanUtils.copyProperties(relation, dto);
        return dto;
    }
}
