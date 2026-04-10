package com.enterprise.edams.asset.feign.fallback;

import com.enterprise.edams.asset.dto.lineage.CreateLineageRequest;
import com.enterprise.edams.asset.dto.lineage.ImpactAnalysisDTO;
import com.enterprise.edams.asset.dto.lineage.LineageGraphDTO;
import com.enterprise.edams.asset.feign.LineageFeignClient;
import com.enterprise.edams.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 血缘服务Feign客户端降级处理
 * 当血缘服务不可用时，返回默认值并记录日志
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class LineageFeignClientFallback implements LineageFeignClient {

    @Override
    public Result<String> createLineage(CreateLineageRequest request) {
        log.error("Feign调用血缘服务创建血缘关系失败, sourceAssetId: {}, targetAssetId: {}",
                request != null ? request.getSourceAssetId() : "null",
                request != null ? request.getTargetAssetId() : "null");
        return Result.success(null);
    }

    @Override
    public Result<Void> deleteLineage(String lineageId) {
        log.error("Feign调用血缘服务删除血缘关系失败, lineageId: {}", lineageId);
        return Result.success(null);
    }

    @Override
    public Result<LineageGraphDTO> getLineageGraph(String assetId, String direction, Integer depth, Boolean includeTasks) {
        log.error("Feign调用血缘服务查询血缘图谱失败, assetId: {}", assetId);
        return Result.success(null);
    }

    @Override
    public Result<ImpactAnalysisDTO> analyzeImpact(String assetId, Integer depth) {
        log.error("Feign调用血缘服务进行影响分析失败, assetId: {}", assetId);
        return Result.success(null);
    }

    @Override
    public Result<LineageGraphDTO> traceLineage(String assetId, Integer depth) {
        log.error("Feign调用血缘服务进行溯源分析失败, assetId: {}", assetId);
        return Result.success(null);
    }

    @Override
    public Result<LineageGraphDTO> getTableLineage(String assetId, String direction, Integer depth) {
        log.error("Feign调用血缘服务获取表级血缘失败, assetId: {}", assetId);
        return Result.success(null);
    }

    @Override
    public Result<LineageGraphDTO> getFieldLineage(String assetId, String direction, Integer depth) {
        log.error("Feign调用血缘服务获取字段级血缘失败, assetId: {}", assetId);
        return Result.success(null);
    }

    @Override
    public Result<Void> verifyLineage(String lineageId, String method) {
        log.error("Feign调用血缘服务验证血缘关系失败, lineageId: {}, method: {}", lineageId, method);
        return Result.success(null);
    }
}
