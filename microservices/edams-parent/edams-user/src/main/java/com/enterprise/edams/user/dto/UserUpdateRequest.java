package com.enterprise.edams.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateRequest {

    @Schema(description = "昵称")
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "部门ID")
    private String departmentId;

    @Schema(description = "职位")
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    @Schema(description = "上级ID")
    private String managerId;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;
}
