package com.enterprise.edams.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 资产标签创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产标签创建请求")
public class AssetTagCreateRequest {

    @NotBlank(message = "标签名称不能为空")
    @Schema(description = "标签名称", required = true)
    private String tagName;

    @Schema(description = "标签编码(可选,自动生成)")
    private String tagCode;

    @Schema(description = "标签颜色(HEX格式,如#FF5733)")
    private String color;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "所属分类")
    private String category;
}
