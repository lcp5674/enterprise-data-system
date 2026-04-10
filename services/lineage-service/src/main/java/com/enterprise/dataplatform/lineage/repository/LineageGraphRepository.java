package com.enterprise.dataplatform.lineage.repository;

import com.enterprise.dataplatform.lineage.domain.event.GraphEdge;
import com.enterprise.dataplatform.lineage.domain.event.GraphNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Neo4j lineage graph repository for graph operations
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LineageGraphRepository {

    private final Driver driver;

    /**
     * Create or update asset node in Neo4j
     */
    public void upsertAssetNode(String assetId, String name, String type, 
                                 String datasourceId, Map<String, Object> properties) {
        String cypher = """
            MERGE (a:DataAsset {assetId: $assetId})
            SET a.name = $name,
                a.type = $type,
                a.datasourceId = $datasourceId,
                a.updatedAt = datetime()
            """;

        try (Session session = driver.session()) {
            Map<String, Object> params = new HashMap<>();
            params.put("assetId", assetId);
            params.put("name", name);
            params.put("type", type);
            params.put("datasourceId", datasourceId);
            params.putAll(properties);

            session.executeWrite(tx -> tx.run(cypher, params).consume());
            log.debug("Upserted asset node: {}", assetId);
        } catch (Exception e) {
            log.error("Failed to upsert asset node: {}", assetId, e);
            throw new RuntimeException("Failed to upsert asset node", e);
        }
    }

    /**
     * Create lineage relationship
     */
    public void createLineageRelation(String sourceAssetId, String targetAssetId,
                                       String lineageType, Map<String, Object> properties) {
        String cypher = """
            MATCH (source:DataAsset {assetId: $sourceAssetId})
            MATCH (target:DataAsset {assetId: $targetAssetId})
            MERGE (source)-[r:DEPENDS_ON]->(target)
            SET r.type = $lineageType,
                r.transformation = $transformation,
                r.updatedAt = datetime()
            """;

        try (Session session = driver.session()) {
            Map<String, Object> params = new HashMap<>();
            params.put("sourceAssetId", sourceAssetId);
            params.put("targetAssetId", targetAssetId);
            params.put("lineageType", lineageType);
            params.put("transformation", properties.getOrDefault("transformation", ""));

            session.executeWrite(tx -> tx.run(cypher, params).consume());
            log.debug("Created lineage relation: {} -> {}", sourceAssetId, targetAssetId);
        } catch (Exception e) {
            log.error("Failed to create lineage relation: {} -> {}", sourceAssetId, targetAssetId, e);
            throw new RuntimeException("Failed to create lineage relation", e);
        }
    }

    /**
     * Delete lineage relationship
     */
    public void deleteLineageRelation(String sourceAssetId, String targetAssetId) {
        String cypher = """
            MATCH (source:DataAsset {assetId: $sourceAssetId})-[r:DEPENDS_ON]->(target:DataAsset {assetId: $targetAssetId})
            DELETE r
            """;

        try (Session session = driver.session()) {
            Map<String, Object> params = Map.of(
                    "sourceAssetId", sourceAssetId,
                    "targetAssetId", targetAssetId
            );

            session.executeWrite(tx -> tx.run(cypher, params).consume());
            log.debug("Deleted lineage relation: {} -> {}", sourceAssetId, targetAssetId);
        } catch (Exception e) {
            log.error("Failed to delete lineage relation: {} -> {}", sourceAssetId, targetAssetId, e);
            throw new RuntimeException("Failed to delete lineage relation", e);
        }
    }

    /**
     * Get upstream lineage (data sources)
     */
    public List<GraphNode> getUpstreamLineage(String assetId, int depth) {
        String cypher = """
            MATCH path = (upstream:DataAsset)-[:DEPENDS_ON*1..%d]->(target:DataAsset {assetId: $assetId})
            RETURN DISTINCT upstream, LENGTH(path) AS distance
            ORDER BY distance
            """.formatted(depth);

        try (Session session = driver.session()) {
            Result result = session.executeRead(tx -> tx.run(cypher, 
                    Values.parameters("assetId", assetId)));

            List<GraphNode> nodes = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("upstream").asNode();
                nodes.add(toGraphNode(node));
            }
            return nodes;
        } catch (Exception e) {
            log.error("Failed to get upstream lineage: {}", assetId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get downstream lineage (data targets)
     */
    public List<GraphNode> getDownstreamLineage(String assetId, int depth) {
        String cypher = """
            MATCH path = (source:DataAsset {assetId: $assetId})-[:DEPENDS_ON*1..%d]->(downstream:DataAsset)
            RETURN DISTINCT downstream, LENGTH(path) AS distance
            ORDER BY distance
            """.formatted(depth);

        try (Session session = driver.session()) {
            Result result = session.executeRead(tx -> tx.run(cypher,
                    Values.parameters("assetId", assetId)));

            List<GraphNode> nodes = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("downstream").asNode();
                nodes.add(toGraphNode(node));
            }
            return nodes;
        } catch (Exception e) {
            log.error("Failed to get downstream lineage: {}", assetId, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get full lineage graph
     */
    public Map<String, Object> getFullLineageGraph(String assetId, int maxDepth) {
        String cypher = """
            MATCH path = (a:DataAsset {assetId: $assetId})-[:DEPENDS_ON*0..%d]-(related:DataAsset)
            RETURN COLLECT(DISTINCT a) AS nodes,
                   COLLECT(DISTINCT relationships(path)) AS rels
            """.formatted(maxDepth);

        try (Session session = driver.session()) {
            Result result = session.executeRead(tx -> tx.run(cypher,
                    Values.parameters("assetId", assetId)));

            if (result.hasNext()) {
                Record record = result.next();
                List<Node> nodeList = record.get("nodes").asList(Value::asNode);
                List<List<Relationship>> relList = record.get("rels").asList(v -> v.asList(Value::asRelationship));

                return Map.of(
                        "nodes", nodeList.stream().map(this::toGraphNode).toList(),
                        "edges", relList.stream().flatMap(List::stream)
                                .map(this::toGraphEdge).toList()
                );
            }
            return Map.of("nodes", List.of(), "edges", List.of());
        } catch (Exception e) {
            log.error("Failed to get full lineage graph: {}", assetId, e);
            return Map.of("nodes", List.of(), "edges", List.of());
        }
    }

    /**
     * Detect circular dependencies
     */
    public boolean hasCircularDependency(String assetId) {
        String cypher = """
            MATCH path = (a:DataAsset {assetId: $assetId})-[:DEPENDS_ON+]->(a)
            RETURN path
            LIMIT 1
            """;

        try (Session session = driver.session()) {
            Result result = session.executeRead(tx -> tx.run(cypher,
                    Values.parameters("assetId", assetId)));
            return result.hasNext();
        } catch (Exception e) {
            log.error("Failed to check circular dependency: {}", assetId, e);
            return false;
        }
    }

    /**
     * Batch import lineage relations
     */
    public void batchImportLineage(List<Map<String, Object>> relations) {
        String cypher = """
            UNWIND $relations AS rel
            MATCH (source:DataAsset {assetId: rel.sourceAssetId})
            MATCH (target:DataAsset {assetId: rel.targetAssetId})
            MERGE (source)-[r:DEPENDS_ON]->(target)
            SET r.type = rel.type,
                r.updatedAt = datetime()
            """;

        try (Session session = driver.session()) {
            session.executeWrite(tx -> tx.run(cypher,
                    Values.parameters("relations", relations.toArray()))).consume();
            log.info("Batch imported {} lineage relations", relations.size());
        } catch (Exception e) {
            log.error("Failed to batch import lineage", e);
            throw new RuntimeException("Failed to batch import lineage", e);
        }
    }

    private GraphNode toGraphNode(Node node) {
        Map<String, Object> props = new HashMap<>(node.asMap());
        return GraphNode.builder()
                .id(node.id())
                .properties(props)
                .labels(List.copyOf(node.labels()))
                .assetId((String) props.get("assetId"))
                .name((String) props.get("name"))
                .type((String) props.get("type"))
                .build();
    }

    private GraphEdge toGraphEdge(Relationship rel) {
        return GraphEdge.builder()
                .id(rel.id())
                .sourceId(rel.startNodeId())
                .targetId(rel.endNodeId())
                .type(rel.type())
                .properties(Map.copyOf(rel.asMap()))
                .build();
    }
}
