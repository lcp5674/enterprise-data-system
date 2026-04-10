package com.enterprise.edams.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class RolePermissionAssignRequest {

    /**
     * 角色ID
     */
    private String roleId;

    /**
     * 权限ID列表
     */
    private List<String> permissionIds;
}
