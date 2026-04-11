package com.enterprise.edams.analytics.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 仪表盘配置实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("edams_dashboard_config")
public class DashboardConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 仪表盘名称 */
    @TableField("dashboard_name")
    private String dashboardName;

    /** 仪表盘编码 */
    @TableField("dashboard_code")
    private String dashboardCode;

    /** 仪表盘类型: SYSTEM/USER/CUSTOM */
    @TableField("dashboard_type")
    private String dashboardType;

    /** 布局配置(JSON) */
    @TableField("layout_config")
    private String layoutConfig;

    /** 组件配置(JSON数组) */
    @TableField("widget_config")
    private String widgetConfig;

    /** 主题: LIGHT/DARK/COLORFUL */
    @TableField("theme")
    private String theme;

    /** 刷新间隔(秒),0表示不自动刷新 */
    @TableField("refresh_interval")
    private Integer refreshInterval;

    /** 描述 */
    @TableField("description")
    private String description;

    /** 状态: 0-禁用, 1-启用 */
    @TableField("status")
    private Integer status;

    /** 是否默认仪表盘 */
    @TableField("is_default")
    private Boolean isDefault;

    /** 所属用户ID(用户仪表盘) */
    @TableField("user_id")
    private Long userId;

    /** 访问权限: PUBLIC/DEPT/PRIVATE */
    @TableField("access_level")
    private String accessLevel;

    /** 创建人ID */
    @TableField("creator_id")
    private Long creatorId;

    /** 创建人名称 */
    @TableField("creator_name")
    private String creatorName;

    /** 最后访问时间 */
    @TableField("last_access_time")
    private String lastAccessTime;

    /** 访问次数 */
    @TableField("access_count")
    private Integer accessCount;

    /** 排序号 */
    @TableField("sort_order")
    private Integer sortOrder;
}
