import '../models/data_quality.dart';
import 'api_service.dart';

class QualityService {
  static final ApiService _api = ApiService();

  /// 获取资产质量评分
  static Future<DataQuality> getAssetQuality(String assetId) async {
    final response = await _api.get(
      '/mobile/quality/$assetId',
      parser: (json) => json,
    );
    return DataQuality.fromJson(response['data'] ?? {});
  }

  /// 获取质量问题列表
  static Future<List<QualityIssue>> getQualityIssues({
    String? assetId,
    String? level,
    int page = 1,
    int pageSize = 20,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (assetId != null) {
      params['assetId'] = assetId;
    }
    if (level != null) {
      params['level'] = level;
    }

    final response = await _api.get(
      '/mobile/quality/issues',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => QualityIssue.fromJson(e)).toList();
  }

  /// 获取质量趋势
  static Future<List<QualityTrend>> getQualityTrends({
    required String assetId,
    int days = 30,
  }) async {
    final response = await _api.get(
      '/mobile/quality/trends/$assetId',
      queryParameters: {'days': days.toString()},
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => QualityTrend.fromJson(e)).toList();
  }

  /// 获取全局质量概览
  static Future<Map<String, dynamic>> getQualityOverview() async {
    final response = await _api.get(
      '/mobile/quality/overview',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取质量评分排名
  static Future<List<DataQuality>> getQualityRanking({
    int limit = 10,
    String order = 'desc',
  }) async {
    final response = await _api.get(
      '/mobile/quality/ranking',
      queryParameters: {
        'limit': limit.toString(),
        'order': order,
      },
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => DataQuality.fromJson(e)).toList();
  }
}
