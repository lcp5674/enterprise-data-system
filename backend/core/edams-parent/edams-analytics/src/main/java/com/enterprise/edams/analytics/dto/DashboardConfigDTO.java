package com.enterprise.edams.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘配置DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "仪表盘配置数据传输对象")
public class DashboardConfigDTO {

    @Schema(description = "仪表盘ID")
    private Long id;

    @Schema(description = "仪表盘名称")
    private String dashboardName;

    @Schema(description = "仪表盘编码")
    private String dashboardCode;

    @Schema(description = "仪表盘类型: SYSTEM/USER/CUSTOM")
    private String dashboardType;

    @Schema(description = "布局配置")
    private Map<String, Object> layoutConfig;

    @Schema(description = "组件配置列表")
    private List<Map<String, Object>> widgetConfig;

    @Schema(description = "主题: LIGHT/DARK/COLORFUL")
    private String theme;

    @Schema(description = "刷新间隔(秒)")
    private Integer refreshInterval;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;

    @Schema(description = "是否默认仪表盘")
    private Boolean isDefault;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "访问权限")
    private String accessLevel;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人名称")
    private String creatorName;

    @Schema(description = "最后访问时间")
    private String lastAccessTime;

    @Schema(description = "访问次数")
    private Integer accessCount;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private String createdTime;

    @Schema(description = "更新时间")
    private String updatedTime;
}
