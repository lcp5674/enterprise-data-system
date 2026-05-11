package com.enterprise.dataplatform.lineage.service;

import com.enterprise.dataplatform.lineage.domain.event.GraphNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImpactAnalysisService {

    public int countAffectedReports(String assetId) {
        log.info("统计受影响的报表数量: assetId={}", assetId);
        return simulateAffectedCount(assetId, "REPORT");
    }

    public int countAffectedDashboards(String assetId) {
        log.info("统计受影响的仪表盘数量: assetId={}", assetId);
        return simulateAffectedCount(assetId, "DASHBOARD");
    }

    public int countAffectedPipelines(String assetId) {
        log.info("统计受影响的管道数量: assetId={}", assetId);
        return simulateAffectedCount(assetId, "PIPELINE");
    }

    public LocalDateTime calculateEstimatedImpactTime(List<GraphNode> affectedNodes) {
        if (affectedNodes == null || affectedNodes.isEmpty()) {
            return LocalDateTime.now().plusHours(24);
        }

        int nodeCount = affectedNodes.size();
        int criticalCount = (int) affectedNodes.stream()
                .filter(n -> "HIGH".equals(n.getProperties().getOrDefault("sensitivityLevel", "MEDIUM")))
                .count();

        int baseHours = 4;
        int additionalHours = nodeCount / 10;
        int criticalHours = criticalCount * 2;

        int totalHours = baseHours + additionalHours + criticalHours;
        totalHours = Math.min(totalHours, 72);

        log.info("计算影响时间: nodeCount={}, criticalCount={}, estimatedHours={}", 
                nodeCount, criticalCount, totalHours);

        return LocalDateTime.now().plusHours(totalHours);
    }

    public List<String> getAffectedBusinessProcesses(String assetId) {
        log.info("获取受影响的业务流程: assetId={}", assetId);
        
        List<String> processes = new ArrayList<>();
        
        Map<String, String> assetToProcessMap = getAssetToProcessMapping();
        String process = assetToProcessMap.get(assetId);
        if (process != null) {
            processes.add(process);
        }
        
        if (processes.isEmpty()) {
            processes.add("数据分析和报表");
            processes.add("业务决策支持");
        }
        
        return processes;
    }

    private int simulateAffectedCount(String assetId, String type) {
        if (assetId == null || assetId.isEmpty()) {
            return 0;
        }
        
        int hash = Math.abs(assetId.hashCode());
        int baseCount = (hash % 10) + 1;
        
        return switch (type) {
            case "REPORT" -> Math.min(baseCount * 2, 50);
            case "DASHBOARD" -> Math.min(baseCount, 20);
            case "PIPELINE" -> Math.min(baseCount / 2 + 1, 10);
            default -> 0;
        };
    }

    private Map<String, String> getAssetToProcessMapping() {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("customer_db", "客户关系管理");
        mapping.put("order_db", "订单处理");
        mapping.put("finance_db", "财务报表");
        mapping.put("inventory_db", "库存管理");
        mapping.put("hr_db", "人力资源管理");
        return mapping;
    }
}
