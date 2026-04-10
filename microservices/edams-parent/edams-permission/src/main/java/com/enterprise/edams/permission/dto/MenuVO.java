package com.enterprise.edams.permission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单VO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class MenuVO {

    private String id;
    private String name;
    private String title;
    private String icon;
    private String menuType;
    private String path;
    private String component;
    private String parentId;
    private Integer sortOrder;
    private Integer isHidden;
    private Integer isCache;
    private String meta;
    private Integer status;
    private String application;
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;

    /**
     * 子菜单列表
     */
    private List<MenuVO> children;

    /**
     * 关联的权限列表
     */
    private List<PermissionVO> permissions;
}
