package com.enterprise.edams.asset.feign;

import com.enterprise.edams.asset.feign.fallback.LineageFeignClientFallback;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.asset.dto.lineage.LineageGraphDTO;
import com.enterprise.edams.asset.dto.lineage.ImpactAnalysisDTO;
import com.enterprise.edams.asset.dto.lineage.CreateLineageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 血缘服务Feign客户端
 * 用于资产服务调用血缘服务查询和管理血缘关系
 *
 * @author Backend Team
 * @version 1.0.0
 */
@FeignClient(
    name = "lineage-service",
    url = "${feign.lineage.url:}",
    fallback = LineageFeignClientFallback.class
)
public interface LineageFeignClient {

    /**
     * 创建血缘关系
     *
     * @param request 血缘创建请求
     * @return 血缘关系ID
     */
    @PostMapping("/api/v1/lineage")
    Result<String> createLineage(@RequestBody CreateLineageRequest request);

    /**
     * 删除血缘关系
     *
     * @param lineageId 血缘关系ID
     * @return 操作结果
     */
    @DeleteMapping("/api/v1/lineage/{lineageId}")
    Result<Void> deleteLineage(@PathVariable String lineageId);

    /**
     * 查询血缘图谱
     *
     * @param assetId       资产ID
     * @param direction     方向: UPSTREAM, DOWNSTREAM, BOTH
     * @param depth         深度
     * @param includeTasks  是否包含任务
     * @return 血缘图谱
     */
    @GetMapping("/api/v1/lineage/graph")
    Result<LineageGraphDTO> getLineageGraph(
            @RequestParam String assetId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false, defaultValue = "3") Integer depth,
            @RequestParam(required = false) Boolean includeTasks);

    /**
     * 影响分析
     *
     * @param assetId 资产ID
     * @param depth   深度
     * @return 影响分析结果
     */
    @GetMapping("/api/v1/lineage/impact/{assetId}")
    Result<ImpactAnalysisDTO> analyzeImpact(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "5") Integer depth);

    /**
     * 溯源分析
     *
     * @param assetId 资产ID
     * @param depth   深度
     * @return 血缘图谱
     */
    @GetMapping("/api/v1/lineage/trace/{assetId}")
    Result<LineageGraphDTO> traceLineage(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "5") Integer depth);

    /**
     * 获取表级血缘
     *
     * @param assetId   资产ID
     * @param direction 方向
     * @param depth     深度
     * @return 血缘图谱
     */
    @GetMapping("/api/v1/lineage/table/{assetId}")
    Result<LineageGraphDTO> getTableLineage(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "UPSTREAM") String direction,
            @RequestParam(required = false, defaultValue = "3") Integer depth);

    /**
     * 获取字段级血缘
     *
     * @param assetId   资产ID
     * @param direction 方向
     * @param depth     深度
     * @return 血缘图谱
     */
    @GetMapping("/api/v1/lineage/field/{assetId}")
    Result<LineageGraphDTO> getFieldLineage(
            @PathVariable String assetId,
            @RequestParam(required = false, defaultValue = "UPSTREAM") String direction,
            @RequestParam(required = false, defaultValue = "2") Integer depth);

    /**
     * 验证血缘关系
     *
     * @param lineageId 血缘关系ID
     * @param method    验证方法
     * @return 操作结果
     */
    @PostMapping("/api/v1/lineage/{lineageId}/verify")
    Result<Void> verifyLineage(
            @PathVariable String lineageId,
            @RequestParam String method);
}
