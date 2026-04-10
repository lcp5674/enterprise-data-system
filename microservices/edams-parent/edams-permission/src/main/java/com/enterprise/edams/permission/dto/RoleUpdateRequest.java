package com.enterprise.edams.permission.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色更新请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class RoleUpdateRequest {

    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String name;

    @Size(max = 200, message = "角色描述不能超过200个字符")
    private String description;

    private String dataScope;

    private Integer status;

    private Integer sortOrder;

    private Integer isDefault;

    /**
     * 权限ID列表
     */
    private List<String> permissionIds;
}
