package com.enterprise.edams.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色创建请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50个字符")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码不能超过50个字符")
    private String code;

    @Size(max = 200, message = "角色描述不能超过200个字符")
    private String description;

    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    private String dataScope;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortOrder;

    private Integer isDefault;

    /**
     * 权限ID列表
     */
    private List<String> permissionIds;
}
