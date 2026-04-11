package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.knowledge.dto.OntologyDTO;
import com.enterprise.edams.knowledge.entity.Ontology;
import com.enterprise.edams.knowledge.entity.OntologyClass;
import com.enterprise.edams.knowledge.entity.Entity;
import com.enterprise.edams.knowledge.entity.Relation;
import com.enterprise.edams.knowledge.repository.OntologyMapper;
import com.enterprise.edams.knowledge.repository.OntologyClassMapper;
import com.enterprise.edams.knowledge.repository.EntityMapper;
import com.enterprise.edams.knowledge.repository.RelationMapper;
import com.enterprise.edams.knowledge.service.OntologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 本体论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OntologyServiceImpl extends ServiceImpl<OntologyMapper, Ontology> implements OntologyService {

    private final OntologyMapper ontologyMapper;
    private final OntologyClassMapper classMapper;
    private final EntityMapper entityMapper;
    private final RelationMapper relationMapper;

    @Override
    public Page<Ontology> selectPage(OntologyDTO dto, int pageNum, int pageSize) {
        Page<Ontology> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Ontology> wrapper = new LambdaQueryWrapper<>();
        if (dto.getName() != null) {
            wrapper.like(Ontology::getName, dto.getName());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Ontology::getStatus, dto.getStatus());
        }
        wrapper.orderByDesc(Ontology::getUpdateTime);
        return page(page, wrapper);
    }

    @Override
    public OntologyDTO getById(Long id) {
        Ontology ontology = baseMapper.selectById(id);
        if (ontology == null) {
            return null;
        }
        OntologyDTO dto = new OntologyDTO();
        BeanUtils.copyProperties(ontology, dto);
        return dto;
    }

    @Override
    @Transactional
    public OntologyDTO create(OntologyDTO dto) {
        Ontology ontology = new Ontology();
        BeanUtils.copyProperties(dto, ontology);
        ontology.setStatus("DRAFT");
        ontologyMapper.insert(ontology);
        log.info("Created ontology: {}", ontology.getId());
        OntologyDTO result = new OntologyDTO();
        BeanUtils.copyProperties(ontology, result);
        return result;
    }

    @Override
    @Transactional
    public OntologyDTO update(Long id, OntologyDTO dto) {
        Ontology ontology = baseMapper.selectById(id);
        if (ontology == null) {
            throw new RuntimeException("Ontology not found: " + id);
        }
        BeanUtils.copyProperties(dto, ontology, "id", "createTime", "creator");
        ontologyMapper.updateById(ontology);
        log.info("Updated ontology: {}", id);
        OntologyDTO result = new OntologyDTO();
        BeanUtils.copyProperties(ontology, result);
        return result;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
        log.info("Deleted ontology: {}", id);
    }

    @Override
    @Transactional
    public OntologyDTO publish(Long id) {
        Ontology ontology = baseMapper.selectById(id);
        if (ontology == null) {
            throw new RuntimeException("Ontology not found: " + id);
        }
        ontology.setStatus("PUBLISHED");
        ontologyMapper.updateById(ontology);
        log.info("Published ontology: {}", id);
        OntologyDTO result = new OntologyDTO();
        BeanUtils.copyProperties(ontology, result);
        return result;
    }

    @Override
    public List<OntologyDTO> getPublishedOntologies() {
        List<Ontology> ontologies = ontologyMapper.selectPublishedOntologies();
        return ontologies.stream().map(ontology -> {
            OntologyDTO dto = new OntologyDTO();
            BeanUtils.copyProperties(ontology, dto);
            return dto;
        }).toList();
    }

    @Override
    public Ontology getStatistics(Long id) {
        Ontology ontology = baseMapper.selectById(id);
        if (ontology == null) {
            return null;
        }
        // 统计类数量
        LambdaQueryWrapper<OntologyClass> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(OntologyClass::getOntologyId, id);
        ontology.setTotalClassCount(Math.toIntExact(classMapper.selectCount(classWrapper)));

        // 统计根类数量
        LambdaQueryWrapper<OntologyClass> rootWrapper = new LambdaQueryWrapper<>();
        rootWrapper.eq(OntologyClass::getOntologyId, id).isNull(OntologyClass::getParentClassId);
        ontology.setRootClassCount(Math.toIntExact(classMapper.selectCount(rootWrapper)));

        // 统计实体数量
        LambdaQueryWrapper<Entity> entityWrapper = new LambdaQueryWrapper<>();
        entityWrapper.eq(Entity::getOntologyId, id).eq(Entity::getStatus, "ACTIVE");
        ontology.setTotalEntityCount(entityMapper.selectCount(entityWrapper));

        // 统计关系数量
        LambdaQueryWrapper<Relation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(Relation::getOntologyId, id).eq(Relation::getStatus, "ACTIVE");
        ontology.setTotalRelationCount(relationMapper.selectCount(relationWrapper));

        return ontology;
    }
}
