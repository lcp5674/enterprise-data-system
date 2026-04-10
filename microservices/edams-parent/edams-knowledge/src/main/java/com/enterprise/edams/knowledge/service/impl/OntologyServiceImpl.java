package com.enterprise.edams.knowledge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.knowledge.dto.OntologyDTO;
import com.enterprise.edams.knowledge.entity.EntityType;
import com.enterprise.edams.knowledge.entity.Ontology;
import com.enterprise.edams.knowledge.entity.RelationType;
import com.enterprise.edams.knowledge.mapper.EntityTypeMapper;
import com.enterprise.edams.knowledge.mapper.OntologyMapper;
import com.enterprise.edams.knowledge.mapper.RelationTypeMapper;
import com.enterprise.edams.knowledge.service.OntologyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 本体服务实现类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OntologyServiceImpl extends ServiceImpl<OntologyMapper, Ontology>
        implements OntologyService {

    private final OntologyMapper ontologyMapper;
    private final EntityTypeMapper entityTypeMapper;
    private final RelationTypeMapper relationTypeMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ontology createOntology(OntologyDTO dto) {
        log.info("创建本体: {}", dto.getName());

        Ontology ontology = new Ontology();
        ontology.setOntologyId(UUID.randomUUID().toString().replace("-", ""));
        ontology.setName(dto.getName());
        ontology.setDescription(dto.getDescription());
        ontology.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        ontology.setNamespace(dto.getNamespace());
        ontology.setStatus("DRAFT");
        ontology.setGraphId(dto.getGraphId());
        ontology.setTenantId(dto.getTenantId());
        ontology.setCreator(dto.getCreator());

        // 序列化实体类型
        if (dto.getEntityTypes() != null) {
            ontology.setEntityTypes(toJson(dto.getEntityTypes()));
        }
        // 序列化关系类型
        if (dto.getRelationTypes() != null) {
            ontology.setRelationTypes(toJson(dto.getRelationTypes()));
        }
        // 序列化属性定义
        if (dto.getProperties() != null) {
            ontology.setProperties(toJson(dto.getProperties()));
        }
        // 序列化约束规则
        if (dto.getConstraints() != null) {
            ontology.setConstraints(toJson(dto.getConstraints()));
        }

        this.save(ontology);
        log.info("本体创建成功: {}", ontology.getOntologyId());

        return ontology;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ontology updateOntology(String ontologyId, OntologyDTO dto) {
        log.info("更新本体: {}", ontologyId);

        Ontology ontology = this.getByOntologyId(ontologyId);
        if (ontology == null) {
            throw new RuntimeException("本体不存在: " + ontologyId);
        }

        if (dto.getName() != null) ontology.setName(dto.getName());
        if (dto.getDescription() != null) ontology.setDescription(dto.getDescription());
        if (dto.getVersion() != null) ontology.setVersion(dto.getVersion());
        if (dto.getNamespace() != null) ontology.setNamespace(dto.getNamespace());
        if (dto.getEntityTypes() != null) ontology.setEntityTypes(toJson(dto.getEntityTypes()));
        if (dto.getRelationTypes() != null) ontology.setRelationTypes(toJson(dto.getRelationTypes()));
        if (dto.getProperties() != null) ontology.setProperties(toJson(dto.getProperties()));
        if (dto.getConstraints() != null) ontology.setConstraints(toJson(dto.getConstraints()));

        ontology.setUpdater(dto.getCreator());

        this.updateById(ontology);
        log.info("本体更新成功: {}", ontologyId);

        return ontology;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOntology(String ontologyId) {
        log.info("删除本体: {}", ontologyId);

        Ontology ontology = this.getByOntologyId(ontologyId);
        if (ontology == null) {
            throw new RuntimeException("本体不存在: " + ontologyId);
        }

        // 删除关联的实体类型和关系类型
        entityTypeMapper.delete(entityTypeMapper.new LambdaQueryWrapper()
                .eq(EntityType::getOntologyId, ontologyId));
        relationTypeMapper.delete(relationTypeMapper.new LambdaQueryWrapper()
                .eq(RelationType::getOntologyId, ontologyId));

        this.removeById(ontology.getId());
        log.info("本体删除成功: {}", ontologyId);
    }

    @Override
    public OntologyDTO getOntologyDetail(String ontologyId) {
        Ontology ontology = this.getByOntologyId(ontologyId);
        if (ontology == null) {
            throw new RuntimeException("本体不存在: " + ontologyId);
        }

        return toDTO(ontology);
    }

    @Override
    public List<OntologyDTO> listByGraphId(String graphId) {
        return ontologyMapper.selectByGraphId(graphId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addEntityType(String ontologyId, OntologyDTO.EntityTypeDTO dto) {
        log.info("添加实体类型到本体: {} - {}", ontologyId, dto.getTypeName());

        Ontology ontology = this.getByOntologyId(ontologyId);
        if (ontology == null) {
            throw new RuntimeException("本体不存在: " + ontologyId);
        }

        EntityType entityType = new EntityType();
        entityType.setTypeId(UUID.randomUUID().toString().replace("-", ""));
        entityType.setTypeName(dto.getTypeName());
        entityType.setDescription(dto.getDescription());
        entityType.setOntologyId(ontologyId);
        entityType.setParentTypeId(dto.getParentTypeId());
        entityType.setAttributes(toJson(dto.getAttributes()));
        entityType.setValidationRules(toJson(dto.getValidationRules()));
        entityType.setIcon(dto.getIcon());
        entityType.setColor(dto.getColor());
        entityType.setStatus("ACTIVE");
        entityType.setTenantId(ontology.getTenantId());

        entityTypeMapper.insert(entityType);
        log.info("实体类型添加成功: {}", entityType.getTypeId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRelationType(String ontologyId, OntologyDTO.RelationTypeDTO dto) {
        log.info("添加关系类型到本体: {} - {}", ontologyId, dto.getTypeName());

        Ontology ontology = this.getByOntologyId(ontologyId);
        if (ontology == null) {
            throw new RuntimeException("本体不存在: " + ontologyId);
        }

        RelationType relationType = new RelationType();
        relationType.setTypeId(UUID.randomUUID().toString().replace("-", ""));
        relationType.setTypeName(dto.getTypeName());
        relationType.setDescription(dto.getDescription());
        relationType.setDirection(dto.getDirection());
        relationType.setSourceTypeConstraint(dto.getSourceTypeConstraint());
        relationType.setTargetTypeConstraint(dto.getTargetTypeConstraint());
        relationType.setOntologyId(ontologyId);
        relationType.setAttributes(toJson(dto.getAttributes()));
        relationType.setReversible(dto.getReversible());
        relationType.setInverseRelationTypeId(dto.getInverseRelationTypeId());
        relationType.setCardinality(dto.getCardinality());
        relationType.setStatus("ACTIVE");
        relationType.setTenantId(ontology.getTenantId());

        relationTypeMapper.insert(relationType);
        log.info("关系类型添加成功: {}", relationType.getTypeId());
    }

    @Override
    public ValidationResult validateOntology(String ontologyId) {
        log.info("验证本体完整性: {}", ontologyId);

        Ontology ontology = this.getByOntologyId(ontologyId);
        if (ontology == null) {
            return new ValidationResult(false, List.of("本体不存在"), List.of());
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 验证基本属性
        if (ontology.getName() == null || ontology.getName().isEmpty()) {
            errors.add("本体名称不能为空");
        }
        if (ontology.getNamespace() == null || ontology.getNamespace().isEmpty()) {
            warnings.add("本体命名空间未设置");
        }

        // 验证实体类型
        List<EntityType> entityTypes = entityTypeMapper.selectByOntologyId(ontologyId);
        if (entityTypes.isEmpty()) {
            warnings.add("本体未定义实体类型");
        }

        // 验证关系类型
        List<RelationType> relationTypes = relationTypeMapper.selectByOntologyId(ontologyId);
        if (relationTypes.isEmpty()) {
            warnings.add("本体未定义关系类型");
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    @Override
    public String exportOntology(String ontologyId, String format) {
        OntologyDTO dto = getOntologyDetail(ontologyId);

        return switch (format.toUpperCase()) {
            case "JSON" -> toJson(dto);
            case "RDF" -> convertToRDF(dto);
            case "OWL" -> convertToOWL(dto);
            default -> throw new IllegalArgumentException("不支持的导出格式: " + format);
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ontology importOntology(String ontologyDefinition, String format) {
        log.info("导入本体定义, 格式: {}", format);

        OntologyDTO dto = switch (format.toUpperCase()) {
            case "JSON" -> parseOntology(ontologyDefinition);
            case "RDF" -> parseFromRDF(ontologyDefinition);
            case "OWL" -> parseFromOWL(ontologyDefinition);
            default -> throw new IllegalArgumentException("不支持的导入格式: " + format);
        };

        return createOntology(dto);
    }

    private Ontology getByOntologyId(String ontologyId) {
        return lambdaQuery().eq(Ontology::getOntologyId, ontologyId).one();
    }

    private OntologyDTO toDTO(Ontology ontology) {
        return OntologyDTO.builder()
                .ontologyId(ontology.getOntologyId())
                .name(ontology.getName())
                .description(ontology.getDescription())
                .version(ontology.getVersion())
                .namespace(ontology.getNamespace())
                .status(ontology.getStatus())
                .graphId(ontology.getGraphId())
                .entityTypes(parseJsonList(ontology.getEntityTypes(), OntologyDTO.EntityTypeDTO.class))
                .relationTypes(parseJsonList(ontology.getRelationTypes(), OntologyDTO.RelationTypeDTO.class))
                .properties(parseJsonList(ontology.getProperties(), OntologyDTO.PropertyDefinitionDTO.class))
                .constraints(parseJsonList(ontology.getConstraints(), OntologyDTO.ConstraintRuleDTO.class))
                .createdTime(ontology.getCreatedTime())
                .updatedTime(ontology.getUpdatedTime())
                .creator(ontology.getCreator())
                .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return null;
        }
    }

    private <T> List<T> parseJsonList(String json, Class<T> clazz) {
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("JSON解析失败", e);
            return List.of();
        }
    }

    private String convertToRDF(OntologyDTO dto) {
        // RDF转换逻辑
        return "{}";
    }

    private String convertToOWL(OntologyDTO dto) {
        // OWL转换逻辑
        return "{}";
    }

    private OntologyDTO parseOntology(String json) {
        try {
            return objectMapper.readValue(json, OntologyDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("本体定义解析失败", e);
        }
    }

    private OntologyDTO parseFromRDF(String rdf) {
        // RDF解析逻辑
        return null;
    }

    private OntologyDTO parseFromOWL(String owl) {
        // OWL解析逻辑
        return null;
    }
}
