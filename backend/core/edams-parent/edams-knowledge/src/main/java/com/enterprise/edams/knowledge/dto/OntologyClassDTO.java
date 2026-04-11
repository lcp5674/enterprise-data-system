package com.enterprise.edams.knowledge.dto;

import lombok.Data;

import java.util.List;

/**
 * 本体类DTO
 */
@Data
public class OntologyClassDTO {
    private Long id;
    private Long ontologyId;
    private Long parentClassId;
    private String className;
    private String classNameZh;
    private String description;
    private String icon;
    private String color;
    private Integer level;
    private Boolean isLeaf;
    private Integer subclassCount;
    private Integer instanceCount;
    private String properties;
    private Integer sortOrder;
    private String creator;
    private List<OntologyClassDTO> children;
}
