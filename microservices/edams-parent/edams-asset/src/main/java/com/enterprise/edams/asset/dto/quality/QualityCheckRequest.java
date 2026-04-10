package com.enterprise.edams.asset.dto.quality;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 质量检查请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class QualityCheckRequest {

    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    /**
     * 资产类型
     */
    private String assetType;

    /**
     * 数据源ID
     */
    private String datasourceId;

    /**
     * 检查类型: SCHEDULED, MANUAL, REALTIME
     */
    private String checkType;

    /**
     * 规则编码列表(指定执行哪些规则)
     */
    private List<String> ruleCodes;

    /**
     * 触发方式
     */
    private String triggerMode;

    /**
     * 执行参数
     */
    private Map<String, Object> parameters;

    /**
     * 是否异步执行
     */
    private boolean async = true;
}
