package com.enterprise.edams.asset.dto;

import com.enterprise.edams.common.enums.AssetType;
import com.enterprise.edams.common.enums.DataSensitivity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 资产创建请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产创建请求")
public class AssetCreateRequest {

    @NotBlank(message = "资产名称不能为空")
    @Schema(description = "资产名称", required = true)
    private String assetName;

    @Schema(description = "资产编码(可选,自动生成)")
    private String assetCode;

    @NotNull(message = "资产类型不能为空")
    @Schema(description = "资产类型: DATABASE/TABLE/VIEW/FILE/API", required = true)
    private AssetType assetType;

    @Schema(description = "资产描述")
    private String description;

    @NotNull(message = "负责人ID不能为空")
    @Schema(description = "负责人ID", required = true)
    private Long ownerId;

    @Schema(description = "敏感级别: PUBLIC/INTERNAL/CONFIDENTIAL/SECRET")
    private DataSensitivity sensitivity;

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "数据源名称")
    private String datasourceName;

    @Schema(description = "数据库名称")
    private String databaseName;

    @Schema(description = "Schema名称")
    private String schemaName;

    @Schema(description = "表/视图名称")
    private String tableName;

    @Schema(description = "所属目录ID")
    private Long catalogId;

    @Schema(description = "所属业务域ID")
    private Long domainId;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "扩展属性(JSON字符串)")
    private String properties;
}
