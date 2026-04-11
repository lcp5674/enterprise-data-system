package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.knowledge.dto.EntityDTO;
import com.enterprise.edams.knowledge.dto.EntityDetailDTO;
import com.enterprise.edams.knowledge.dto.KnowledgeSearchDTO;
import com.enterprise.edams.knowledge.dto.OntologyClassDTO;
import com.enterprise.edams.knowledge.dto.RelationDTO;
import com.enterprise.edams.knowledge.entity.Entity;
import com.enterprise.edams.knowledge.entity.OntologyClass;
import com.enterprise.edams.knowledge.entity.Relation;
import com.enterprise.edams.knowledge.repository.EntityMapper;
import com.enterprise.edams.knowledge.repository.OntologyClassMapper;
import com.enterprise.edams.knowledge.repository.RelationMapper;
import com.enterprise.edams.knowledge.service.EntityService;
import com.enterprise.edams.knowledge.service.KnowledgeGraphService;
import com.enterprise.edams.knowledge.service.OntologyClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    private final EntityMapper entityMapper;
    private final RelationMapper relationMapper;
    private final OntologyClassMapper classMapper;
    private final EntityService entityService;
    private final OntologyClassService classService;

    @Override
    public KnowledgeSearchDTO search(Long ontologyId, String keyword, int limit) {
        KnowledgeSearchDTO result = new KnowledgeSearchDTO();
        result.setOntologyId(ontologyId);
        result.setKeyword(keyword);
        
        // 搜索实体
        List<EntityDTO> entities = entityService.search(keyword, ontologyId, limit);
        result.setEntities(entities);
        result.setTotalEntities((long) entities.size());
        
        // 搜索类
        LambdaQueryWrapper<OntologyClass> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.like(OntologyClass::getClassName, keyword)
                .or().like(OntologyClass::getClassNameZh, keyword);
        if (ontologyId != null) {
            classWrapper.eq(OntologyClass::getOntologyId, ontologyId);
        }
        List<OntologyClass> classes = classMapper.selectList(classWrapper);
        result.setClasses(classes.stream().map(this::convertClassToDTO).limit(limit).toList());
        result.setTotalClasses((long) classes.size());
        
        // 搜索关系
        LambdaQueryWrapper<Relation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.and(w -> w.like(Relation::getRelationName, keyword)
                .or().like(Relation::getDescription, keyword));
        if (ontologyId != null) {
            relationWrapper.eq(Relation::getOntologyId, ontologyId);
        }
        List<Relation> relations = relationMapper.selectList(relationWrapper);
        result.setRelations(relations.stream().map(this::convertRelationToDTO).limit(limit).toList());
        result.setTotalRelations((long) relations.size());
        
        return result;
    }

    @Override
    public List<Long> getRelatedEntities(Long entityId) {
        Set<Long> related = new HashSet<>();
        
        // 获取出边
        List<Relation> outgoing = relationMapper.selectOutgoingRelations(entityId);
        outgoing.forEach(r -> related.add(r.getTargetEntityId()));
        
        // 获取入边
        List<Relation> incoming = relationMapper.selectIncomingRelations(entityId);
        incoming.forEach(r -> related.add(r.getSourceEntityId()));
        
        return new ArrayList<>(related);
    }

    @Override
    public List<Long> getMultiHopEntities(Long entityId, int hops) {
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(entityId);
        visited.add(entityId);
        
        for (int i = 0; i < hops; i++) {
            int size = queue.size();
            for (int j = 0; j < size; j++) {
                Long current = queue.poll();
                List<Long> related = getRelatedEntities(current);
                for (Long id : related) {
                    if (!visited.contains(id)) {
                        visited.add(id);
                        queue.offer(id);
                    }
                }
            }
        }
        
        visited.remove(entityId);
        return new ArrayList<>(visited);
    }

    @Override
    public List<Long> findPath(Long sourceId, Long targetId, int maxHops) {
        // BFS寻找最短路径
        Map<Long, Long> parentMap = new HashMap<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(sourceId);
        parentMap.put(sourceId, null);
        
        while (!queue.isEmpty() && parentMap.size() <= maxHops) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Long current = queue.poll();
                if (current.equals(targetId)) {
                    return buildPath(parentMap, targetId);
                }
                
                List<Long> related = getRelatedEntities(current);
                for (Long id : related) {
                    if (!parentMap.containsKey(id)) {
                        parentMap.put(id, current);
                        queue.offer(id);
                    }
                }
            }
        }
        
        return Collections.emptyList();
    }

    @Override
    public List<Long> findSimilarEntities(Long entityId, int limit) {
        Entity entity = entityMapper.selectById(entityId);
        if (entity == null) {
            return Collections.emptyList();
        }
        
        // 基于相同类、相同标签找相似实体
        LambdaQueryWrapper<Entity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Entity::getClassId, entity.getClassId())
                .ne(Entity::getId, entityId)
                .eq(Entity::getStatus, "ACTIVE");
        
        List<Entity> similar = entityMapper.selectList(wrapper);
        return similar.stream()
                .limit(limit)
                .map(Entity::getId)
                .toList();
    }

    @Override
    public EntityDetailDTO getEntitySubgraph(Long entityId, int depth) {
        EntityDetailDTO detail = entityService.getDetail(entityId);
        if (detail == null) {
            return null;
        }
        
        // BFS获取子图
        Set<Long> visited = new HashSet<>();
        visited.add(entityId);
        
        List<RelationDTO> allRelations = new ArrayList<>();
        allRelations.addAll(detail.getOutgoingRelations());
        allRelations.addAll(detail.getIncomingRelations());
        
        for (int i = 1; i < depth; i++) {
            Set<Long> newVisited = new HashSet<>();
            for (RelationDTO relation : allRelations) {
                if (!visited.contains(relation.getTargetEntityId())) {
                    newVisited.add(relation.getTargetEntityId());
                }
                if (!visited.contains(relation.getSourceEntityId())) {
                    newVisited.add(relation.getSourceEntityId());
                }
            }
            visited.addAll(newVisited);
            
            // 获取新节点的关系
            for (Long id : newVisited) {
                List<Relation> relations = relationMapper.selectByEntityId(id);
                allRelations.addAll(relations.stream().map(this::convertRelationToDTO).toList());
            }
        }
        
        return detail;
    }

    private List<Long> buildPath(Map<Long, Long> parentMap, Long targetId) {
        List<Long> path = new LinkedList<>();
        Long current = targetId;
        while (current != null) {
            path.add(0, current);
            current = parentMap.get(current);
        }
        return path;
    }

    private EntityDTO convertEntityToDTO(Entity entity) {
        EntityDTO dto = new EntityDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private OntologyClassDTO convertClassToDTO(OntologyClass clazz) {
        OntologyClassDTO dto = new OntologyClassDTO();
        BeanUtils.copyProperties(clazz, dto);
        return dto;
    }

    private RelationDTO convertRelationToDTO(Relation relation) {
        RelationDTO dto = new RelationDTO();
        BeanUtils.copyProperties(relation, dto);
        return dto;
    }
}
