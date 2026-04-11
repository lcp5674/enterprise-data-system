package com.enterprise.edams.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.knowledge.dto.OntologyDTO;
import com.enterprise.edams.knowledge.entity.Ontology;

import java.util.List;

/**
 * 本体论服务接口
 */
public interface OntologyService extends IService<Ontology> {

    /**
     * 分页查询本体论
     */
    Page<Ontology> selectPage(OntologyDTO dto, int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    OntologyDTO getById(Long id);

    /**
     * 创建本体论
     */
    OntologyDTO create(OntologyDTO dto);

    /**
     * 更新本体论
     */
    OntologyDTO update(Long id, OntologyDTO dto);

    /**
     * 删除本体论
     */
    void delete(Long id);

    /**
     * 发布本体论
     */
    OntologyDTO publish(Long id);

    /**
     * 获取所有已发布的本体论
     */
    List<OntologyDTO> getPublishedOntologies();

    /**
     * 获取统计信息
     */
    Ontology getStatistics(Long id);
}
