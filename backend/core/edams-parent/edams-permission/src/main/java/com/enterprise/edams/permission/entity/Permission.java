package com.enterprise.edams.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 权限实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_permission")
public class Permission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 权限名称（如：用户管理、资产查询） */
    private String name;

    /** 权限编码（如：user:manage, asset:query） */
    private String code;

    /** 权限类型：1-菜单权限，2-按钮权限，3-数据权限，4-API接口权限 */
    private Integer type;

    /** 父级权限ID（顶级为0） */
    private Long parentId;

    /** 关联的菜单ID（如果是按钮/接口类型） */
    private Long menuId;

    /** 请求路径（API接口类型使用） */
    private String path;

    /** HTTP方法（GET/POST/PUT/DELETE） */
    private String method;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 描述 */
    private String description;
}
