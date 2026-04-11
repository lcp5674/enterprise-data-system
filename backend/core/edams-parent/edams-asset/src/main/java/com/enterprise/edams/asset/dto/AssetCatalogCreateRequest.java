package com.enterprise.edams.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资产目录创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产目录创建请求")
public class AssetCatalogCreateRequest {

    @NotBlank(message = "目录名称不能为空")
    @Schema(description = "目录名称", required = true)
    private String name;

    @Schema(description = "目录编码(可选,自动生成)")
    private String code;

    @NotNull(message = "父目录ID不能为空")
    @Schema(description = "父目录ID(0表示根目录)", required = true)
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "目录描述")
    private String description;

    @Schema(description = "图标")
    private String icon;
}
