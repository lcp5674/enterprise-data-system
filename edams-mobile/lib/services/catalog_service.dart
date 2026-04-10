import '../models/data_catalog.dart';
import '../models/data_asset.dart';
import 'api_service.dart';

class CatalogService {
  static final ApiService _api = ApiService();

  /// 获取目录树
  static Future<List<DataCatalog>> getCatalogTree() async {
    final response = await _api.get(
      '/mobile/catalogs/tree',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => DataCatalog.fromJson(e)).toList();
  }

  /// 获取目录列表（扁平）
  static Future<List<DataCatalog>> getCatalogs({
    String? parentId,
    int page = 1,
    int pageSize = 50,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (parentId != null) {
      params['parentId'] = parentId;
    }

    final response = await _api.get(
      '/mobile/catalogs',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => DataCatalog.fromJson(e)).toList();
  }

  /// 获取目录详情
  static Future<DataCatalog> getCatalogDetail(String catalogId) async {
    final response = await _api.get(
      '/mobile/catalogs/$catalogId',
      parser: (json) => json,
    );
    return DataCatalog.fromJson(response['data']);
  }

  /// 获取目录下的资产列表
  static Future<List<DataAsset>> getCatalogAssets(
    String catalogId, {
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/mobile/catalogs/$catalogId/assets',
      queryParameters: {
        'page': page.toString(),
        'pageSize': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => DataAsset.fromJson(e)).toList();
  }

  /// 获取所有分类（顶级目录）
  static Future<List<DataCatalog>> getTopLevelCatalogs() async {
    final response = await _api.get(
      '/mobile/catalogs/top',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => DataCatalog.fromJson(e)).toList();
  }
}
