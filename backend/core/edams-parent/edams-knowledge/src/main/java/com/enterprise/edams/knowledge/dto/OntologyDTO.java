package com.enterprise.edams.knowledge.dto;

import lombok.Data;

/**
 * 本体论DTO
 */
@Data
public class OntologyDTO {
    private Long id;
    private String name;
    private String description;
    private String version;
    private String namespace;
    private Integer rootClassCount;
    private Integer totalClassCount;
    private Long totalEntityCount;
    private Long totalRelationCount;
    private String status;
    private String creator;
    private String modifier;
}
