package com.enterprise.edams.asset.dto;

import com.enterprise.edams.common.enums.DataSensitivity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 资产更新请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产更新请求")
public class AssetUpdateRequest {

    @NotBlank(message = "资产名称不能为空")
    @Schema(description = "资产名称", required = true)
    private String assetName;

    @Schema(description = "资产描述")
    private String description;

    @NotNull(message = "负责人ID不能为空")
    @Schema(description = "负责人ID", required = true)
    private Long ownerId;

    @Schema(description = "敏感级别")
    private DataSensitivity sensitivity;

    @Schema(description = "所属目录ID")
    private Long catalogId;

    @Schema(description = "所属业务域ID")
    private Long domainId;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "扩展属性(JSON字符串)")
    private String properties;
}
