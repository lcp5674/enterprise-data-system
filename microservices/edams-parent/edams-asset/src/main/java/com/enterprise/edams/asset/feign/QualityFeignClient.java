package com.enterprise.edams.asset.feign;

import com.enterprise.edams.asset.feign.fallback.QualityFeignClientFallback;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.asset.dto.quality.QualityCheckRequest;
import com.enterprise.edams.asset.dto.quality.QualityCheckResultDTO;
import com.enterprise.edams.asset.dto.quality.QualityRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 质量服务Feign客户端
 * 用于资产服务调用质量服务执行质量检查
 *
 * @author Backend Team
 * @version 1.0.0
 */
@FeignClient(
    name = "quality-service",
    url = "${feign.quality.url:}",
    fallback = QualityFeignClientFallback.class
)
public interface QualityFeignClient {

    /**
     * 执行质量检查
     *
     * @param request 质量检查请求
     * @return 质量检查结果ID
     */
    @PostMapping("/api/v1/quality/checks/execute")
    Result<String> executeQualityCheck(@RequestBody QualityCheckRequest request);

    /**
     * 批量执行质量检查
     *
     * @param assetIds 资产ID列表
     * @return 批量检查结果
     */
    @PostMapping("/api/v1/quality/checks/batch")
    Result<List<String>> batchExecuteQualityCheck(@RequestBody List<String> assetIds);

    /**
     * 查询质量检查结果
     *
     * @param checkId 检查ID
     * @return 质量检查结果
     */
    @GetMapping("/api/v1/quality/checks/{checkId}")
    Result<QualityCheckResultDTO> getQualityCheckResult(@PathVariable String checkId);

    /**
     * 查询资产的质量历史
     *
     * @param assetId 资产ID
     * @param limit   返回记录数
     * @return 质量检查结果列表
     */
    @GetMapping("/api/v1/quality/checks/asset/{assetId}")
    Result<List<QualityCheckResultDTO>> getAssetQualityHistory(
            @PathVariable String assetId,
            @RequestParam(defaultValue = "10") Integer limit);

    /**
     * 获取资产的质量得分
     *
     * @param assetId 资产ID
     * @return 质量得分
     */
    @GetMapping("/api/v1/quality/scores/{assetId}")
    Result<Double> getAssetQualityScore(@PathVariable String assetId);

    /**
     * 获取资产关联的质量规则
     *
     * @param assetId 资产ID
     * @return 质量规则列表
     */
    @GetMapping("/api/v1/quality/rules/asset/{assetId}")
    Result<List<QualityRuleDTO>> getAssetQualityRules(@PathVariable String assetId);

    /**
     * 查询质量告警
     *
     * @param assetId  资产ID
     * @param severity  严重程度
     * @param status    状态
     * @return 告警列表
     */
    @GetMapping("/api/v1/quality/alerts")
    Result<List<Object>> getQualityAlerts(
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status);

    /**
     * 触发质量告警处理
     *
     * @param alertId 告警ID
     * @param action  处理动作
     * @param comment 处理备注
     * @return 操作结果
     */
    @PostMapping("/api/v1/quality/alerts/{alertId}/handle")
    Result<Void> handleQualityAlert(
            @PathVariable String alertId,
            @RequestParam String action,
            @RequestParam(required = false) String comment);
}
