import '../models/data_map.dart';
import 'api_service.dart';

class DataMapService {
  static final ApiService _api = ApiService();

  /// 获取资产血缘关系
  static Future<DataLineage> getAssetLineage(String assetId) async {
    final response = await _api.get(
      '/mobile/datamap/lineage/$assetId',
      parser: (json) => json,
    );
    return DataLineage.fromJson(response['data'] ?? {});
  }

  /// 获取上游资产
  static Future<List<DataLineageNode>> getUpstreamAssets(String assetId) async {
    final response = await _api.get(
      '/mobile/datamap/upstream/$assetId',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => DataLineageNode.fromJson(e)).toList();
  }

  /// 获取下游资产
  static Future<List<DataLineageNode>> getDownstreamAssets(String assetId) async {
    final response = await _api.get(
      '/mobile/datamap/downstream/$assetId',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => DataLineageNode.fromJson(e)).toList();
  }

  /// 获取影响分析
  static Future<Map<String, dynamic>> getImpactAnalysis(String assetId) async {
    final response = await _api.get(
      '/mobile/datamap/impact/$assetId',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 搜索血缘关系
  static Future<List<DataLineage>> searchLineage({
    String? keyword,
    int page = 1,
    int pageSize = 20,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (keyword != null && keyword.isNotEmpty) {
      params['keyword'] = keyword;
    }

    final response = await _api.get(
      '/mobile/datamap/search',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => DataLineage.fromJson(e)).toList();
  }
}
