package com.enterprise.dataplatform.lineage.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Graph edge representation for Neo4j
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdge {
    
    private Long id;
    private Long sourceId;
    private Long targetId;
    private String type;
    private Map<String, Object> properties;
}
