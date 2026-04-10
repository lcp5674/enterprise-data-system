package com.enterprise.edams.asset.feign.fallback;

import com.enterprise.edams.asset.dto.quality.QualityCheckRequest;
import com.enterprise.edams.asset.dto.quality.QualityCheckResultDTO;
import com.enterprise.edams.asset.dto.quality.QualityRuleDTO;
import com.enterprise.edams.asset.feign.QualityFeignClient;
import com.enterprise.edams.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 质量服务Feign客户端降级处理
 * 当质量服务不可用时，返回默认值并记录日志
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class QualityFeignClientFallback implements QualityFeignClient {

    @Override
    public Result<String> executeQualityCheck(QualityCheckRequest request) {
        log.error("Feign调用质量服务执行质量检查失败, assetId: {}", 
                request != null ? request.getAssetId() : "null");
        return Result.success(null);
    }

    @Override
    public Result<List<String>> batchExecuteQualityCheck(List<String> assetIds) {
        log.error("Feign调用质量服务批量执行质量检查失败, assetIds: {}", assetIds);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<QualityCheckResultDTO> getQualityCheckResult(String checkId) {
        log.error("Feign调用质量服务查询检查结果失败, checkId: {}", checkId);
        return Result.success(null);
    }

    @Override
    public Result<List<QualityCheckResultDTO>> getAssetQualityHistory(String assetId, Integer limit) {
        log.error("Feign调用质量服务查询质量历史失败, assetId: {}", assetId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<Double> getAssetQualityScore(String assetId) {
        log.error("Feign调用质量服务查询质量得分失败, assetId: {}", assetId);
        return Result.success(0.0);
    }

    @Override
    public Result<List<QualityRuleDTO>> getAssetQualityRules(String assetId) {
        log.error("Feign调用质量服务获取资产规则失败, assetId: {}", assetId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<List<Object>> getQualityAlerts(String assetId, String severity, String status) {
        log.error("Feign调用质量服务查询告警失败, assetId: {}, severity: {}, status: {}", 
                assetId, severity, status);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<Void> handleQualityAlert(String alertId, String action, String comment) {
        log.error("Feign调用质量服务处理告警失败, alertId: {}, action: {}", alertId, action);
        return Result.success(null);
    }
}
