package com.enterprise.dataplatform.lineage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for lineage graph data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageGraphResponse {

    private List<LineageNode> nodes;

    private List<LineageEdge> edges;

    private LineageStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineageNode {
        private String id;
        private String name;
        private String alias;
        private String type;
        private Integer level;
        private String database;
        private String businessDomain;
        private Map<String, Object> properties;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineageEdge {
        private String source;
        private String target;
        private String transform;
        private String taskName;
        private String taskId;
        private String scheduleTime;
        private String lineageType;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineageStatistics {
        private Integer upstreamCount;
        private Integer downstreamCount;
        private Integer maxDepth;
        private Boolean hasCycle;
    }
}
