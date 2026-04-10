package com.enterprise.edams.aiops.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 根因分析服务
 * 
 * 功能:
 * - 基于血缘关系分析故障影响范围
 * - 关联分析多指标异常
 * - 提供问题定位建议
 *
 * @author AIOps Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RootCauseAnalysisService {

    private final MetricsAnalysisService metricsAnalysisService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RCA_CACHE_PREFIX = "aiops:rca:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 根因分析结果
     */
    public static class RootCauseResult {
        private String incidentId;
        private String serviceName;
        private String rootCause;
        private String rootCauseType;
        private double confidence;
        private List<String> affectedServices;
        private List<String> affectedComponents;
        private List<ImpactAnalysis> impactAnalysis;
        private List<String> relatedAnomalies;
        private List<String> recommendedActions;
        private LocalDateTime analyzedAt;
        private Duration analysisDuration;

        public String getIncidentId() { return incidentId; }
        public void setIncidentId(String incidentId) { this.incidentId = incidentId; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getRootCause() { return rootCause; }
        public void setRootCause(String rootCause) { this.rootCause = rootCause; }
        public String getRootCauseType() { return rootCauseType; }
        public void setRootCauseType(String rootCauseType) { this.rootCauseType = rootCauseType; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public List<String> getAffectedServices() { return affectedServices; }
        public void setAffectedServices(List<String> affectedServices) { this.affectedServices = affectedServices; }
        public List<String> getAffectedComponents() { return affectedComponents; }
        public void setAffectedComponents(List<String> affectedComponents) { this.affectedComponents = affectedComponents; }
        public List<ImpactAnalysis> getImpactAnalysis() { return impactAnalysis; }
        public void setImpactAnalysis(List<ImpactAnalysis> impactAnalysis) { this.impactAnalysis = impactAnalysis; }
        public List<String> getRelatedAnomalies() { return relatedAnomalies; }
        public void setRelatedAnomalies(List<String> relatedAnomalies) { this.relatedAnomalies = relatedAnomalies; }
        public List<String> getRecommendedActions() { return recommendedActions; }
        public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }
        public LocalDateTime getAnalyzedAt() { return analyzedAt; }
        public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
        public Duration getAnalysisDuration() { return analysisDuration; }
        public void setAnalysisDuration(Duration analysisDuration) { this.analysisDuration = analysisDuration; }
    }

    /**
     * 影响分析
     */
    public static class ImpactAnalysis {
        private String targetService;
        private String impactType;
        private int severity;
        private String description;

        public String getTargetService() { return targetService; }
        public void setTargetService(String targetService) { this.targetService = targetService; }
        public String getImpactType() { return impactType; }
        public void setImpactType(String impactType) { this.impactType = impactType; }
        public int getSeverity() { return severity; }
        public void setSeverity(int severity) { this.severity = severity; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 执行根因分析
     */
    public RootCauseResult analyzeRootCause(String incidentId, String serviceName, 
                                              LocalDateTime startTime, LocalDateTime endTime) {
        long startMs = System.currentTimeMillis();
        log.info("开始根因分析 - 事件ID: {}, 服务: {}, 时间范围: {} ~ {}", 
                incidentId, serviceName, startTime, endTime);

        // 尝试从缓存获取
        String cacheKey = RCA_CACHE_PREFIX + incidentId;
        @SuppressWarnings("unchecked")
        RootCauseResult cached = (RootCauseResult) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取根因分析结果");
            return cached;
        }

        // 收集相关异常
        List<String> relatedAnomalies = collectRelatedAnomalies(serviceName, startTime, endTime);

        // 构建血缘关系图
        Map<String, Object> lineageGraph = buildLineageGraph(serviceName);

        // 分析影响范围
        List<ImpactAnalysis> impactAnalysis = analyzeImpact(serviceName, lineageGraph);

        // 确定根因
        String rootCause = determineRootCause(serviceName, relatedAnomalies, lineageGraph);
        String rootCauseType = classifyRootCause(rootCause);

        // 获取受影响的服务和组件
        List<String> affectedServices = extractAffectedServices(impactAnalysis);
        List<String> affectedComponents = extractAffectedComponents(lineageGraph);

        // 生成建议措施
        List<String> recommendedActions = generateRecommendedActions(rootCause, rootCauseType, 
                relatedAnomalies);

        // 计算置信度
        double confidence = calculateConfidence(relatedAnomalies, impactAnalysis, rootCause);

        RootCauseResult result = new RootCauseResult();
        result.setIncidentId(incidentId);
        result.setServiceName(serviceName);
        result.setRootCause(rootCause);
        result.setRootCauseType(rootCauseType);
        result.setConfidence(confidence);
        result.setAffectedServices(affectedServices);
        result.setAffectedComponents(affectedComponents);
        result.setImpactAnalysis(impactAnalysis);
        result.setRelatedAnomalies(relatedAnomalies);
        result.setRecommendedActions(recommendedActions);
        result.setAnalyzedAt(LocalDateTime.now());
        result.setAnalysisDuration(Duration.ofMillis(System.currentTimeMillis() - startMs));

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);

        log.info("根因分析完成 - 耗时: {}ms, 根因: {}, 置信度: {}", 
                result.getAnalysisDuration().toMillis(), rootCause, confidence);

        return result;
    }

    /**
     * 收集相关异常
     */
    private List<String> collectRelatedAnomalies(String serviceName, 
                                                  LocalDateTime startTime, LocalDateTime endTime) {
        // 简化实现：返回模拟的异常列表
        List<String> anomalies = new ArrayList<>();
        
        // 模拟检测到的异常
        int anomalyCount = ThreadLocalRandom.current().nextInt(1, 5);
        for (int i = 0; i < anomalyCount; i++) {
            anomalies.add(String.format("ANOMALY-%s-%d", serviceName, i + 1));
        }

        return anomalies;
    }

    /**
     * 构建血缘关系图
     */
    private Map<String, Object> buildLineageGraph(String serviceName) {
        Map<String, Object> graph = new HashMap<>();
        
        // 模拟血缘关系
        graph.put("service", serviceName);
        graph.put("dependencies", List.of(
                Map.of("name", "database", "type", "postgresql", "critical", true),
                Map.of("name", "cache", "type", "redis", "critical", false),
                Map.of("name", "message-queue", "type", "kafka", "critical", false)
        ));
        graph.put("dependents", List.of(
                Map.of("name", "gateway", "type", "api-gateway"),
                Map.of("name", "notification-service", "type", "notification")
        ));

        return graph;
    }

    /**
     * 分析影响范围
     */
    @SuppressWarnings("unchecked")
    private List<ImpactAnalysis> analyzeImpact(String serviceName, Map<String, Object> lineageGraph) {
        List<ImpactAnalysis> impacts = new ArrayList<>();

        // 分析下游依赖影响
        List<Map<String, Object>> dependencies = (List<Map<String, Object>>) lineageGraph.get("dependencies");
        if (dependencies != null) {
            for (Map<String, Object> dep : dependencies) {
                ImpactAnalysis impact = new ImpactAnalysis();
                impact.setTargetService((String) dep.get("name"));
                impact.setImpactType("DEPENDENCY_FAILURE");
                impact.setSeverity((Boolean) dep.getOrDefault("critical", false) ? 3 : 2);
                impact.setDescription(String.format("由于%s故障导致的%s服务不可用", 
                        serviceName, dep.get("name")));
                impacts.add(impact);
            }
        }

        // 分析上游调用方影响
        List<Map<String, Object>> dependents = (List<Map<String, Object>>) lineageGraph.get("dependents");
        if (dependents != null) {
            for (Map<String, Object> dep : dependents) {
                ImpactAnalysis impact = new ImpactAnalysis();
                impact.setTargetService((String) dep.get("name"));
                impact.setImpactType("UPSTREAM_UNAVAILABLE");
                impact.setSeverity(2);
                impact.setDescription(String.format("%s无法调用%s", dep.get("name"), serviceName));
                impacts.add(impact);
            }
        }

        return impacts;
    }

    /**
     * 确定根因
     */
    private String determineRootCause(String serviceName, List<String> anomalies, 
                                       Map<String, Object> lineageGraph) {
        // 简化实现：根据异常类型确定根因
        if (anomalies.isEmpty()) {
            return "UNKNOWN: 未检测到明显异常";
        }

        // 模拟根因判定
        String[] possibleCauses = {
                "DATABASE_CONNECTION_POOL_EXHAUSTION: 数据库连接池耗尽",
                "HIGH_CPU_USAGE: CPU使用率过高",
                "MEMORY_LEAK: 内存泄漏",
                "NETWORK_LATENCY: 网络延迟增加",
                "DOWNSTREAM_SERVICE_TIMEOUT: 下游服务超时"
        };

        return possibleCauses[ThreadLocalRandom.current().nextInt(possibleCauses.length)];
    }

    /**
     * 分类根因类型
     */
    private String classifyRootCause(String rootCause) {
        String upperCause = rootCause.toUpperCase();
        if (upperCause.contains("DATABASE") || upperCause.contains("DB")) {
            return "DATABASE";
        } else if (upperCause.contains("CPU")) {
            return "COMPUTE";
        } else if (upperCause.contains("MEMORY") || upperCause.contains("LEAK")) {
            return "MEMORY";
        } else if (upperCause.contains("NETWORK")) {
            return "NETWORK";
        } else if (upperCause.contains("TIMEOUT") || upperCause.contains("DOWNSTREAM")) {
            return "DEPENDENCY";
        }
        return "UNKNOWN";
    }

    /**
     * 提取受影响的服务
     */
    private List<String> extractAffectedServices(List<ImpactAnalysis> impacts) {
        return impacts.stream()
                .map(ImpactAnalysis::getTargetService)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 提取受影响的组件
     */
    private List<String> extractAffectedComponents(Map<String, Object> lineageGraph) {
        List<String> components = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> deps = (List<Map<String, Object>>) lineageGraph.get("dependencies");
        if (deps != null) {
            components.addAll(deps.stream()
                    .map(d -> (String) d.get("name"))
                    .collect(Collectors.toList()));
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dependents = (List<Map<String, Object>>) lineageGraph.get("dependents");
        if (dependents != null) {
            components.addAll(dependents.stream()
                    .map(d -> (String) d.get("name"))
                    .collect(Collectors.toList()));
        }

        return components;
    }

    /**
     * 生成建议措施
     */
    private List<String> generateRecommendedActions(String rootCause, String rootCauseType,
                                                    List<String> relatedAnomalies) {
        List<String> actions = new ArrayList<>();

        switch (rootCauseType) {
            case "DATABASE" -> {
                actions.add("1. 检查数据库连接池配置");
                actions.add("2. 优化慢查询");
                actions.add("3. 考虑增加连接池大小或启用连接复用");
                actions.add("4. 检查数据库服务器资源使用情况");
            }
            case "COMPUTE" -> {
                actions.add("1. 检查是否存在死循环或高负载进程");
                actions.add("2. 考虑水平扩展增加实例数");
                actions.add("3. 优化计算密集型操作");
            }
            case "MEMORY" -> {
                actions.add("1. 检查内存泄漏");
                actions.add("2. 增加JVM堆内存或应用内存限制");
                actions.add("3. 优化内存使用，释放不必要的缓存");
            }
            case "NETWORK" -> {
                actions.add("1. 检查网络连接稳定性");
                actions.add("2. 优化重试机制和超时配置");
                actions.add("3. 考虑使用连接池减少连接创建开销");
            }
            case "DEPENDENCY" -> {
                actions.add("1. 检查下游服务的可用性和响应时间");
                actions.add("2. 实现熔断和降级机制");
                actions.add("3. 增加超时重试配置");
            }
            default -> {
                actions.add("1. 收集更多诊断信息");
                actions.add("2. 进行更深入的分析");
            }
        }

        return actions;
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(List<String> anomalies, List<ImpactAnalysis> impacts, 
                                        String rootCause) {
        double confidence = 0.5;

        // 异常数量越多，置信度越高
        confidence += Math.min(0.2, anomalies.size() * 0.05);

        // 影响范围越明确，置信度越高
        if (!impacts.isEmpty()) {
            confidence += 0.1;
        }

        // 能确定具体根因
        if (!rootCause.startsWith("UNKNOWN")) {
            confidence += 0.15;
        }

        return Math.min(0.95, confidence);
    }

    /**
     * 获取血缘关系图
     */
    public Map<String, Object> getLineageGraph(String serviceName) {
        return buildLineageGraph(serviceName);
    }
}
