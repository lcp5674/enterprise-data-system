/// 数据血缘关系模型
class DataLineage {
  final String id;
  final String sourceAssetId;
  final String sourceAssetName;
  final String targetAssetId;
  final String targetAssetName;
  final String? flowType; // direct, transformation
  final String? description;
  final List<DataLineageNode>? nodes;
  final List<DataLineageEdge>? edges;

  DataLineage({
    required this.id,
    required this.sourceAssetId,
    required this.sourceAssetName,
    required this.targetAssetId,
    required this.targetAssetName,
    this.flowType,
    this.description,
    this.nodes,
    this.edges,
  });

  factory DataLineage.fromJson(Map<String, dynamic> json) {
    return DataLineage(
      id: json['id']?.toString() ?? '',
      sourceAssetId: json['sourceAssetId'] ?? '',
      sourceAssetName: json['sourceAssetName'] ?? '',
      targetAssetId: json['targetAssetId'] ?? '',
      targetAssetName: json['targetAssetName'] ?? '',
      flowType: json['flowType'],
      description: json['description'],
      nodes: json['nodes'] != null
          ? (json['nodes'] as List)
              .map((e) => DataLineageNode.fromJson(e))
              .toList()
          : null,
      edges: json['edges'] != null
          ? (json['edges'] as List)
              .map((e) => DataLineageEdge.fromJson(e))
              .toList()
          : null,
    );
  }
}

/// 数据血缘节点
class DataLineageNode {
  final String id;
  final String assetId;
  final String assetName;
  final String assetType;
  final double? x;
  final double? y;

  DataLineageNode({
    required this.id,
    required this.assetId,
    required this.assetName,
    required this.assetType,
    this.x,
    this.y,
  });

  factory DataLineageNode.fromJson(Map<String, dynamic> json) {
    return DataLineageNode(
      id: json['id']?.toString() ?? '',
      assetId: json['assetId'] ?? '',
      assetName: json['assetName'] ?? '',
      assetType: json['assetType'] ?? 'table',
      x: json['x']?.toDouble(),
      y: json['y']?.toDouble(),
    );
  }
}

/// 数据血缘边
class DataLineageEdge {
  final String id;
  final String source;
  final String target;

  DataLineageEdge({
    required this.id,
    required this.source,
    required this.target,
  });

  factory DataLineageEdge.fromJson(Map<String, dynamic> json) {
    return DataLineageEdge(
      id: json['id']?.toString() ?? '',
      source: json['source'] ?? '',
      target: json['target'] ?? '',
    );
  }
}
