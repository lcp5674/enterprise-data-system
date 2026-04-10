import '../models/data_asset.dart';
import '../models/dashboard.dart';
import 'api_service.dart';

class AssetService {
  static final ApiService _api = ApiService();

  /// 获取首页统计数据
  static Future<DashboardStats> getDashboardStats() async {
    final response = await _api.get(
      '/mobile/dashboard/stats',
      parser: (json) => json,
    );
    return DashboardStats.fromJson(response['data'] ?? {});
  }

  /// 搜索资产
  static Future<List<DataAsset>> searchAssets({
    String? keyword,
    String? assetType,
    String? department,
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
    if (assetType != null && assetType.isNotEmpty) {
      params['assetType'] = assetType;
    }
    if (department != null && department.isNotEmpty) {
      params['department'] = department;
    }

    final response = await _api.get(
      '/mobile/assets/search',
      queryParameters: params,
      parser: (json) => json,
    );

    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => DataAsset.fromJson(e)).toList();
  }

  /// 获取资产详情
  static Future<DataAsset> getAssetDetail(String assetId) async {
    final response = await _api.get(
      '/mobile/assets/$assetId',
      parser: (json) => json,
    );
    return DataAsset.fromJson(response['data']);
  }

  /// 获取最近访问资产
  static Future<List<RecentAsset>> getRecentAssets({int limit = 10}) async {
    final response = await _api.get(
      '/mobile/assets/recent',
      queryParameters: {'limit': limit.toString()},
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => RecentAsset.fromJson(e)).toList();
  }

  /// 获取收藏资产列表
  static Future<List<DataAsset>> getFavoriteAssets({
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/mobile/assets/favorites',
      queryParameters: {
        'page': page.toString(),
        'pageSize': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => DataAsset.fromJson(e)).toList();
  }

  /// 收藏/取消收藏资产
  static Future<bool> toggleFavorite(String assetId) async {
    final response = await _api.post(
      '/mobile/assets/$assetId/favorite',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 订阅/取消订阅资产变更
  static Future<bool> toggleSubscription(String assetId) async {
    final response = await _api.post(
      '/mobile/assets/$assetId/subscribe',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 获取资产统计概览
  static Future<Map<String, int>> getAssetStatistics() async {
    final response = await _api.get(
      '/mobile/assets/statistics',
      parser: (json) => json,
    );
    return Map<String, int>.from(response['data'] ?? {});
  }
}
