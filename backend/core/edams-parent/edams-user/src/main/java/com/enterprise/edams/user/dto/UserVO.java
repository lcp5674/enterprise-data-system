package com.enterprise.edams.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户视图对象
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer gender;
    /** 性别描述 */
    private String genderText;
    private Integer status;
    /** 状态描述 */
    private String statusText;
    private Long departmentId;
    private String departmentName;
    private Long tenantId;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer mfaEnabled;

    public static UserVO fromEntity(com.enterprise.edams.user.entity.User entity) {
        return UserVO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .realName(entity.getRealName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .avatar(entity.getAvatar())
                .gender(entity.getGender())
                .genderText(getGenderText(entity.getGender()))
                .status(entity.getStatus())
                .statusText(getStatusText(entity.getStatus()))
                .departmentId(entity.getDepartmentId())
                .tenantId(entity.getTenantId())
                .lastLoginTime(entity.getLastLoginTime())
                .lastLoginIp(entity.getLastLoginIp())
                .mfaEnabled(entity.getMfaEnabled())
                .build();
    }

    private static String getGenderText(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    private static String getStatusText(Integer status) {
        if (status == null) return "未知";
        return status == 1 ? "启用" : "禁用";
    }
}
