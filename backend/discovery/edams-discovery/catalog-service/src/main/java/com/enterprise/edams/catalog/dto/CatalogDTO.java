package com.enterprise.edams.catalog.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CatalogDTO {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String type;
    private Long parentId;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CatalogDTO> children;
}
