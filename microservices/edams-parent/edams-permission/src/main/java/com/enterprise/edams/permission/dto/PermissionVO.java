package com.enterprise.edams.permission.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限VO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class PermissionVO {

    private String id;
    private String name;
    private String code;
    private String permissionType;
    private String module;
    private String resourcePath;
    private String httpMethod;
    private String parentId;
    private String parentName;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;
}
