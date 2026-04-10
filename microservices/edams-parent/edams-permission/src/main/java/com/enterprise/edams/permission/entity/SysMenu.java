package com.enterprise.edams.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单类型: CATALOG=目录, MENU=菜单, BUTTON=按钮
     */
    private String menuType;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 父菜单ID
     */
    private String parentId;

    /**
     * 菜单排序
     */
    private Integer sortOrder;

    /**
     * 是否隐藏: 0=显示, 1=隐藏
     */
    private Integer isHidden;

    /**
     * 是否缓存: 0=不缓存, 1=缓存
     */
    private Integer isCache;

    /**
     * 菜单元数据(JSON格式)
     */
    private String meta;

    /**
     * 状态: 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 所属应用: WEB=PC端, MOBILE=移动端
     */
    private String application;
}
