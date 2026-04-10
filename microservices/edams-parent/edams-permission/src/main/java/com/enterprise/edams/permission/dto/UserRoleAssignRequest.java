package com.enterprise.edams.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户角色分配请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class UserRoleAssignRequest {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 角色ID列表
     */
    private List<String> roleIds;
}
