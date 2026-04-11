package com.enterprise.edams.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资产标签DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产标签数据传输对象")
public class AssetTagDTO {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签编码")
    private String tagCode;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "所属分类")
    private String category;

    @Schema(description = "状态: 0-禁用, 1-启用")
    private Integer status;

    @Schema(description = "使用次数")
    private Integer usageCount;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
