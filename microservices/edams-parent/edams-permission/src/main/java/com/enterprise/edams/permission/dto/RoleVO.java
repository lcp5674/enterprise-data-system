package com.enterprise.edams.permission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色VO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class RoleVO {

    private String id;
    private String name;
    private String code;
    private String description;
    private String roleType;
    private String dataScope;
    private Integer status;
    private Integer sortOrder;
    private Integer isDefault;
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;

    /**
     * 关联的权限列表
     */
    private List<PermissionVO> permissions;

    /**
     * 关联的权限ID列表
     */
    private List<String> permissionIds;

    /**
     * 关联的用户数量
     */
    private Long userCount;
}
