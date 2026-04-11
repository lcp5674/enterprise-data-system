package com.enterprise.edams.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.knowledge.dto.OntologyClassDTO;
import com.enterprise.edams.knowledge.entity.OntologyClass;

import java.util.List;

/**
 * 本体类服务接口
 */
public interface OntologyClassService extends IService<OntologyClass> {

    /**
     * 分页查询类
     */
    List<OntologyClassDTO> selectByOntologyId(Long ontologyId);

    /**
     * 根据ID查询
     */
    OntologyClassDTO getById(Long id);

    /**
     * 创建类
     */
    OntologyClassDTO create(OntologyClassDTO dto);

    /**
     * 更新类
     */
    OntologyClassDTO update(Long id, OntologyClassDTO dto);

    /**
     * 删除类
     */
    void delete(Long id);

    /**
     * 获取类的树形结构
     */
    List<OntologyClassDTO> getClassTree(Long ontologyId);

    /**
     * 获取根类列表
     */
    List<OntologyClassDTO> getRootClasses(Long ontologyId);

    /**
     * 获取子类的树
     */
    List<OntologyClassDTO> getSubclassTree(Long parentClassId);

    /**
     * 批量创建类
     */
    List<OntologyClassDTO> batchCreate(List<OntologyClassDTO> dtoList);
}
