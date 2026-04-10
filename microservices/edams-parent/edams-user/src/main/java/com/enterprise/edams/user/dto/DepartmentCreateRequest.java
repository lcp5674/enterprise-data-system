package com.enterprise.edams.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门创建请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Schema(description = "部门创建请求")
public class DepartmentCreateRequest {

    @Schema(description = "部门名称")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String name;

    @Schema(description = "部门编码")
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String code;

    @Schema(description = "上级部门ID")
    private String parentId;

    @Schema(description = "排序")
    private Integer sortOrder = 0;

    @Schema(description = "负责人ID")
    private String leaderId;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "描述")
    private String description;
}
