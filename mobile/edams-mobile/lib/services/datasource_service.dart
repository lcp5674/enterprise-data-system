import 'api_service.dart';

/// 数据源服务
class DatasourceService {
  static final ApiService _api = ApiService();

  /// 获取数据源列表
  static Future<List<Map<String, dynamic>>> getDatasources({
    int page = 1,
    int pageSize = 20,
    String? keyword,
    String? type,
    String? status,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (keyword != null && keyword.isNotEmpty) {
      params['keyword'] = keyword;
    }
    if (type != null && type.isNotEmpty) {
      params['type'] = type;
    }
    if (status != null && status.isNotEmpty) {
      params['status'] = status;
    }

    final response = await _api.get(
      '/datasources',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取数据源详情
  static Future<Map<String, dynamic>> getDatasourceDetail(String datasourceId) async {
    final response = await _api.get(
      '/datasources/$datasourceId',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 测试数据源连接
  static Future<Map<String, dynamic>> testConnection(String datasourceId) async {
    final response = await _api.post(
      '/datasources/$datasourceId/test',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 同步数据源
  static Future<bool> syncDatasource(String datasourceId) async {
    final response = await _api.post(
      '/datasources/$datasourceId/sync',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 获取同步状态
  static Future<Map<String, dynamic>> getSyncStatus(String datasourceId) async {
    final response = await _api.get(
      '/datasources/$datasourceId/sync/status',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 获取数据源下的表列表
  static Future<List<Map<String, dynamic>>> getDatasourceTables(
    String datasourceId, {
    String? database,
    int page = 1,
    int pageSize = 100,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (database != null && database.isNotEmpty) {
      params['database'] = database;
    }

    final response = await _api.get(
      '/datasources/$datasourceId/tables',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取数据源下的表结构
  static Future<Map<String, dynamic>> getTableStructure(
    String datasourceId,
    String database,
    String tableName,
  ) async {
    final response = await _api.get(
      '/datasources/$datasourceId/tables/$database/$tableName',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }
}
