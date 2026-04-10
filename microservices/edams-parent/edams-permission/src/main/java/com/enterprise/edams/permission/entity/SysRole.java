package com.enterprise.edams.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型: SYSTEM=系统角色, BUSINESS=业务角色
     */
    private String roleType;

    /**
     * 数据权限范围: ALL=全部数据, DEPT=本部门数据, DEPT_AND_CHILD=本部门及子部门, SELF=仅本人数据, CUSTOM=自定义
     */
    private String dataScope;

    /**
     * 状态: 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否为默认角色
     */
    private Integer isDefault;
}
