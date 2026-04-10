package com.enterprise.edams.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户视图对象")
public class UserVO {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "部门ID")
    private String departmentId;

    @Schema(description = "部门名称")
    private String departmentName;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "上级ID")
    private String managerId;

    @Schema(description = "上级姓名")
    private String managerName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "MFA是否启用")
    private Integer mfaEnabled;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
