package com.enterprise.edams.asset.dto;

import com.enterprise.edams.common.enums.AssetStatus;
import com.enterprise.edams.common.enums.AssetType;
import com.enterprise.edams.common.enums.DataSensitivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产数据传输对象")
public class AssetDTO {

    @Schema(description = "资产ID")
    private Long id;

    @Schema(description = "资产名称")
    private String assetName;

    @Schema(description = "资产编码")
    private String assetCode;

    @Schema(description = "资产类型: DATABASE/TABLE/VIEW/FILE/API")
    private AssetType assetType;

    @Schema(description = "资产描述")
    private String description;

    @Schema(description = "负责人ID")
    private Long ownerId;

    @Schema(description = "负责人名称")
    private String ownerName;

    @Schema(description = "敏感级别")
    private DataSensitivity sensitivity;

    @Schema(description = "资产状态")
    private AssetStatus status;

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

    @Schema(description = "字段数量")
    private Integer columnCount;

    @Schema(description = "数据行数")
    private Long rowCount;

    @Schema(description = "数据大小(字节)")
    private Long dataSize;

    @Schema(description = "质量评分(0-100)")
    private BigDecimal qualityScore;

    @Schema(description = "所属目录ID")
    private Long catalogId;

    @Schema(description = "所属业务域ID")
    private Long domainId;

    @Schema(description = "所属业务域名称")
    private String domainName;

    @Schema(description = "标签列表")
    private List<String> tagList;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "最后同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "创建人")
    private String createdBy;

    @Schema(description = "更新人")
    private String updatedBy;
}
