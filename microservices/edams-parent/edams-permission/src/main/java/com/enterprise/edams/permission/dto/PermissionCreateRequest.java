package com.enterprise.edams.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限创建请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class PermissionCreateRequest {

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称不能超过50个字符")
    private String name;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码不能超过100个字符")
    private String code;

    @NotBlank(message = "权限类型不能为空")
    private String permissionType;

    @Size(max = 50, message = "模块名称不能超过50个字符")
    private String module;

    @Size(max = 200, message = "资源路径不能超过200个字符")
    private String resourcePath;

    private String httpMethod;

    private String parentId;

    @Size(max = 200, message = "权限描述不能超过200个字符")
    private String description;

    private Integer status;

    private Integer sortOrder;
}
