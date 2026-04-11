package com.enterprise.dataplatform.lineage.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.List;
import java.util.Map;

/**
 * Graph node representation for Neo4j
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphNode {
    
    private Long id;
    private String assetId;
    private String name;
    private String type;
    private String datasourceId;
    private Map<String, Object> properties;
    private List<String> labels;

    /**
     * Convert from Neo4j Node
     */
    public static GraphNode fromNode(Node node) {
        return GraphNode.builder()
                .id(node.id())
                .properties(Map.copyOf(node.asMap()))
                .labels(List.copyOf(node.labels()))
                .build();
    }
}
