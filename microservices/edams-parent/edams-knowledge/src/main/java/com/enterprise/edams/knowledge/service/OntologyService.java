package com.enterprise.edams.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.knowledge.dto.OntologyDTO;
import com.enterprise.edams.knowledge.entity.Ontology;

import java.util.List;

/**
 * 本体服务接口
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
public interface OntologyService extends IService<Ontology> {

    /**
     * 创建本体
     *
     * @param dto 本体信息
     * @return 创建的本体
     */
    Ontology createOntology(OntologyDTO dto);

    /**
     * 更新本体
     *
     * @param ontologyId 本体ID
     * @param dto 本体信息
     * @return 更新后的本体
     */
    Ontology updateOntology(String ontologyId, OntologyDTO dto);

    /**
     * 删除本体
     *
     * @param ontologyId 本体ID
     */
    void deleteOntology(String ontologyId);

    /**
     * 获取本体详情
     *
     * @param ontologyId 本体ID
     * @return 本体详情
     */
    OntologyDTO getOntologyDetail(String ontologyId);

    /**
     * 获取图谱下的本体列表
     *
     * @param graphId 图谱ID
     * @return 本体列表
     */
    List<OntologyDTO> listByGraphId(String graphId);

    /**
     * 添加实体类型
     *
     * @param ontologyId 本体ID
     * @param entityType 实体类型定义
     */
    void addEntityType(String ontologyId, OntologyDTO.EntityTypeDTO entityType);

    /**
     * 添加关系类型
     *
     * @param ontologyId 本体ID
     * @param relationType 关系类型定义
     */
    void addRelationType(String ontologyId, OntologyDTO.RelationTypeDTO relationType);

    /**
     * 验证本体完整性
     *
     * @param ontologyId 本体ID
     * @return 验证结果
     */
    ValidationResult validateOntology(String ontologyId);

    /**
     * 导出本体定义
     *
     * @param ontologyId 本体ID
     * @param format 导出格式 (JSON, RDF, OWL)
     * @return 导出的本体定义
     */
    String exportOntology(String ontologyId, String format);

    /**
     * 导入本体定义
     *
     * @param ontologyDefinition 本体定义
     * @param format 导入格式
     * @return 导入结果
     */
    Ontology importOntology(String ontologyDefinition, String format);

    /**
     * 验证结果
     */
    record ValidationResult(boolean valid, List<String> errors, List<String> warnings) {}
}
