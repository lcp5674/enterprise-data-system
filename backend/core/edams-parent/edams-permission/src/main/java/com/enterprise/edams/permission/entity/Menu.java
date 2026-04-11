package com.enterprise.edams.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 菜单实体（前端路由/导航菜单）
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class Menu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 菜单名称 */
    private String name;

    /** 菜单编码（唯一） */
    private String code;

    /** 父菜单ID */
    private Long parentId;

    /** 排序号 */
    private Integer sortOrder;

    /** 菜单类型：0-目录，1-菜单，2-按钮 */
    private Integer type;

    /** 前端路由路径（如：/assets） */
    private String path;

    /** 组件路径（如：/views/assets/index.vue） */
    private String component;

    /** 图标名称 */
    private String icon;

    /** 权限标识 */
    private String permission;

    /** 是否外链：0-否，1-是 */
    private Integer isExternal = 0;

    /** 是否缓存：0-否，1-是 */
    private Integer isCache = 0;

    /** 是否隐藏：0-否，1-是 */
    private Integer isHidden = 0;

    /** 状态：0-禁用，1-启用 */
    private Integer status = 1;

    /** 描述 */
    private String remark;

    /** 租户ID */
    private Long tenantId;
}
