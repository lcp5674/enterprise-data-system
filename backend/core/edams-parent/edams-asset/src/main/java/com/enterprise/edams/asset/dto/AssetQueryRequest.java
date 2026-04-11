package com.enterprise.edams.asset.dto;

import com.enterprise.edams.common.enums.AssetStatus;
import com.enterprise.edams.common.enums.AssetType;
import com.enterprise.edams.common.enums.DataSensitivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 资产查询请求
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Schema(description = "资产查询请求")
public class AssetQueryRequest {

    @Schema(description = "搜索关键词(匹配名称/编码/描述)")
    private String keyword;

    @Schema(description = "资产名称(精确匹配)")
    private String assetName;

    @Schema(description = "资产编码")
    private String assetCode;

    @Schema(description = "资产类型列表")
    private List<AssetType> assetTypes;

    @Schema(description = "资产状态列表")
    private List<AssetStatus> statuses;

    @Schema(description = "敏感级别列表")
    private List<DataSensitivity> sensitivities;

    @Schema(description = "负责人ID")
    private Long ownerId;

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "所属目录ID")
    private Long catalogId;

    @Schema(description = "所属业务域ID")
    private Long domainId;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "最小质量评分")
    private Integer minQualityScore;

    @Schema(description = "最大质量评分")
    private Integer maxQualityScore;

    @Schema(description = "页码(默认1)")
    private Integer pageNum = 1;

    @Schema(description = "每页数量(默认10)")
    private Integer pageSize = 10;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "是否升序(默认降序)")
    private Boolean asc = false;
}
