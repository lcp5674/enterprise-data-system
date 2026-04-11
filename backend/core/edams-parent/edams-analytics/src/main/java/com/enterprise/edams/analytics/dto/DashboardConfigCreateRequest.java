package com.enterprise.edams.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘配置创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "仪表盘配置创建请求")
public class DashboardConfigCreateRequest {

    @NotBlank(message = "仪表盘名称不能为空")
    @Schema(description = "仪表盘名称", required = true)
    private String dashboardName;

    @Schema(description = "仪表盘编码")
    private String dashboardCode;

    @NotNull(message = "仪表盘类型不能为空")
    @Schema(description = "仪表盘类型: SYSTEM/USER/CUSTOM", required = true)
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

    @Schema(description = "是否默认仪表盘")
    private Boolean isDefault;

    @Schema(description = "所属用户ID")
    private Long userId;

    @Schema(description = "访问权限")
    private String accessLevel;
}
