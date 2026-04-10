package com.enterprise.edams.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限编码
     */
    private String code;

    /**
     * 权限类型: MENU=菜单权限, BUTTON=按钮权限, API=接口权限, DATA=数据权限, FIELD=字段权限
     */
    private String permissionType;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 资源路径/接口路径
     */
    private String resourcePath;

    /**
     * HTTP方法: GET, POST, PUT, DELETE
     */
    private String httpMethod;

    /**
     * 父权限ID
     */
    private String parentId;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 状态: 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
