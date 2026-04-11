package com.enterprise.edams.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    private String name;

    /** 角色编码（唯一，如：ROLE_ADMIN） */
    private String code;

    /** 角色描述 */
    private String description;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 数据权限类型：1-全部数据，2-本部门及下级，3-本部门，4-本人 */
    private Integer dataScope;

    /** 租户ID */
    private Long tenantId;
}
