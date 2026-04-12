package com.enterprise.edams.catalog.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CatalogCreateRequest {
    @NotBlank(message = "目录名称不能为空")
    private String name;
    private String description;
    private String icon;
    private String type;
    private Long parentId;
    private Integer sortOrder;
}
