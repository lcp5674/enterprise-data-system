package com.enterprise.edams.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户信息DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private Integer status;

    private Long departmentId;

    private String departmentName;

    private Long tenantId;

    private LocalDateTime lastLoginTime;

    private Set<String> roles;

    private Set<String> permissions;

    /**
     * MFA是否启用
     */
    private Boolean mfaEnabled;

    public static UserInfoDTO fromEntity(Object user) {
        return null;
    }
}
