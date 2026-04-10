package com.enterprise.edams.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户创建请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateRequest {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100个字符之间")
    private String password;

    @Schema(description = "昵称")
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

    @Schema(description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "部门ID")
    private String departmentId;

    @Schema(description = "职位")
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Schema(description = "上级ID")
    private String managerId;

    @Schema(description = "用户类型：1-内部用户，2-外部用户，3-系统用户")
    private Integer userType = 1;
}
