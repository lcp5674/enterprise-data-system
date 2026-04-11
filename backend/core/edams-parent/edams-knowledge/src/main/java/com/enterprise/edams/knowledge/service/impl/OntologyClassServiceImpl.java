package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.knowledge.dto.OntologyClassDTO;
import com.enterprise.edams.knowledge.entity.OntologyClass;
import com.enterprise.edams.knowledge.repository.OntologyClassMapper;
import com.enterprise.edams.knowledge.service.OntologyClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 本体类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OntologyClassServiceImpl extends ServiceImpl<OntologyClassMapper, OntologyClass> implements OntologyClassService {

    @Override
    public List<OntologyClassDTO> selectByOntologyId(Long ontologyId) {
        List<OntologyClass> classes = baseMapper.selectByOntologyId(ontologyId);
        return classes.stream().map(this::convertToDTO).toList();
    }

    @Override
    public OntologyClassDTO getById(Long id) {
        OntologyClass clazz = baseMapper.selectById(id);
        if (clazz == null) {
            return null;
        }
        return convertToDTO(clazz);
    }

    @Override
    @Transactional
    public OntologyClassDTO create(OntologyClassDTO dto) {
        OntologyClass clazz = new OntologyClass();
        BeanUtils.copyProperties(dto, clazz);
        
        // 设置层级
        if (dto.getParentClassId() != null) {
            OntologyClass parent = baseMapper.selectById(dto.getParentClassId());
            if (parent != null) {
                clazz.setLevel(parent.getLevel() + 1);
                clazz.setIsLeaf(true);
                // 更新父节点为非叶子
                parent.setIsLeaf(false);
                baseMapper.updateById(parent);
            }
        } else {
            clazz.setLevel(0);
            clazz.setIsLeaf(true);
        }
        
        baseMapper.insert(clazz);
        log.info("Created ontology class: {}", clazz.getId());
        
        OntologyClassDTO result = new OntologyClassDTO();
        BeanUtils.copyProperties(clazz, result);
        return result;
    }

    @Override
    @Transactional
    public OntologyClassDTO update(Long id, OntologyClassDTO dto) {
        OntologyClass clazz = baseMapper.selectById(id);
        if (clazz == null) {
            throw new RuntimeException("Class not found: " + id);
        }
        BeanUtils.copyProperties(dto, clazz, "id", "createTime", "creator");
        baseMapper.updateById(clazz);
        log.info("Updated ontology class: {}", id);
        return convertToDTO(clazz);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子类
        LambdaQueryWrapper<OntologyClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OntologyClass::getParentClassId, id);
        long subclassCount = baseMapper.selectCount(wrapper);
        if (subclassCount > 0) {
            throw new RuntimeException("Cannot delete class with subclasses");
        }
        baseMapper.deleteById(id);
        log.info("Deleted ontology class: {}", id);
    }

    @Override
    public List<OntologyClassDTO> getClassTree(Long ontologyId) {
        List<OntologyClass> allClasses = baseMapper.selectClassTree(ontologyId);
        Map<Long, List<OntologyClass>> childrenMap = allClasses.stream()
                .filter(c -> c.getParentClassId() != null)
                .collect(Collectors.groupingBy(OntologyClass::getParentClassId));
        
        return buildTree(allClasses, childrenMap, null);
    }

    @Override
    public List<OntologyClassDTO> getRootClasses(Long ontologyId) {
        List<OntologyClass> roots = baseMapper.selectRootClasses(ontologyId);
        return roots.stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<OntologyClassDTO> getSubclassTree(Long parentClassId) {
        List<OntologyClass> allClasses = new ArrayList<>();
        collectSubclasses(parentClassId, allClasses);
        
        Map<Long, List<OntologyClass>> childrenMap = allClasses.stream()
                .collect(Collectors.groupingBy(OntologyClass::getParentClassId));
        
        return buildTree(allClasses, childrenMap, parentClassId);
    }

    @Override
    @Transactional
    public List<OntologyClassDTO> batchCreate(List<OntologyClassDTO> dtoList) {
        return dtoList.stream().map(this::create).toList();
    }

    private OntologyClassDTO convertToDTO(OntologyClass clazz) {
        OntologyClassDTO dto = new OntologyClassDTO();
        BeanUtils.copyProperties(clazz, dto);
        return dto;
    }

    private List<OntologyClassDTO> buildTree(List<OntologyClass> allClasses,
                                              Map<Long, List<OntologyClass>> childrenMap,
                                              Long parentId) {
        List<OntologyClassDTO> result = new ArrayList<>();
        
        List<OntologyClass> parents = parentId == null 
                ? allClasses.stream().filter(c -> c.getParentClassId() == null).toList()
                : childrenMap.getOrDefault(parentId, new ArrayList<>());
        
        for (OntologyClass clazz : parents) {
            OntologyClassDTO dto = convertToDTO(clazz);
            List<OntologyClass> children = childrenMap.get(clazz.getId());
            if (children != null && !children.isEmpty()) {
                dto.setChildren(buildTree(allClasses, childrenMap, clazz.getId()));
            }
            result.add(dto);
        }
        
        return result;
    }

    private void collectSubclasses(Long parentId, List<OntologyClass> result) {
        List<OntologyClass> children = baseMapper.selectSubclasses(parentId);
        result.addAll(children);
        for (OntologyClass child : children) {
            collectSubclasses(child.getId(), result);
        }
    }
}
