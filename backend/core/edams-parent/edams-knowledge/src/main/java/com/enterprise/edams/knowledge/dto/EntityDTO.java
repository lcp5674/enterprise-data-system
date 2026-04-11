package com.enterprise.edams.knowledge.dto;

import lombok.Data;

/**
 * 实体DTO
 */
@Data
public class EntityDTO {
    private Long id;
    private Long ontologyId;
    private Long classId;
    private String className;
    private String name;
    private String uniqueId;
    private String alias;
    private String description;
    private String entityType;
    private String tags;
    private String properties;
    private String imageUrl;
    private Integer confidence;
    private String source;
    private String status;
    private Integer favoriteCount;
    private Long viewCount;
    private String creator;
}
