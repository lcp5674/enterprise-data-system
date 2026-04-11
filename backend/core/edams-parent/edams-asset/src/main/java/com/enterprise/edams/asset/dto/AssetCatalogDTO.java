package com.enterprise.edams.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 资产目录DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产目录数据传输对象")
public class AssetCatalogDTO {

    @Schema(description = "目录ID")
    private Long id;

    @Schema(description = "目录名称")
    private String name;

    @Schema(description = "目录编码")
    private String code;

    @Schema(description = "父目录ID")
    private Long parentId;

    @Schema(description = "目录层级")
    private Integer level;

    @Schema(description = "目录路径")
    private String path;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "目录描述")
    private String description;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;

    @Schema(description = "资产数量")
    private Integer assetCount;

    @Schema(description = "是否叶子节点")
    private Boolean isLeaf;

    @Schema(description = "子目录列表")
    private List<AssetCatalogDTO> children;

    @Schema(description = "创建时间")
    private String createdTime;

    @Schema(description = "更新时间")
    private String updatedTime;
}
