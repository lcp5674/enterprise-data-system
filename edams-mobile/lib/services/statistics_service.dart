import 'api_service.dart';

/// 统计服务
class StatisticsService {
  static final ApiService _api = ApiService();

  /// 获取统计概览
  static Future<Map<String, dynamic>> getOverview() async {
    final response = await _api.get(
      '/statistics/overview',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取趋势数据
  static Future<List<Map<String, dynamic>>> getTrend({
    required String type,
    String period = 'DAY',
    int days = 30,
  }) async {
    final response = await _api.get(
      '/statistics/trend',
      queryParameters: {
        'type': type,
        'period': period,
        'days': days.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取分布数据
  static Future<Map<String, dynamic>> getDistribution({
    required String type,
    String? dimension,
  }) async {
    final params = <String, String>{
      'type': type,
    };
    if (dimension != null) {
      params['dimension'] = dimension;
    }

    final response = await _api.get(
      '/statistics/distribution',
      queryParameters: params,
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取资产统计概览
  static Future<Map<String, dynamic>> getAssetStatisticsOverview() async {
    final response = await _api.get(
      '/assets/statistics/overview',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取资产趋势统计
  static Future<List<Map<String, dynamic>>> getAssetTrend({
    String period = 'DAY',
    int days = 30,
  }) async {
    final response = await _api.get(
      '/assets/statistics/trend',
      queryParameters: {
        'period': period,
        'days': days.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取资产分布统计
  static Future<Map<String, dynamic>> getAssetDistribution({
    String dimension = 'type',
  }) async {
    final response = await _api.get(
      '/assets/statistics/distribution',
      queryParameters: {'dimension': dimension},
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取质量统计
  static Future<Map<String, dynamic>> getQualityStatistics() async {
    final response = await _api.get(
      '/assets/statistics/quality',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }
}
