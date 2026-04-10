import '../models/user.dart';
import 'api_service.dart';

/// 部门服务
class DepartmentService {
  static final ApiService _api = ApiService();

  /// 获取部门树
  static Future<List<Department>> getDepartmentTree() async {
    final response = await _api.get(
      '/departments/tree',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => Department.fromJson(e)).toList();
  }

  /// 获取部门列表
  static Future<List<Department>> getDepartments({
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
      '/departments',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => Department.fromJson(e)).toList();
  }

  /// 获取部门详情
  static Future<Department> getDepartmentDetail(String departmentId) async {
    final response = await _api.get(
      '/departments/$departmentId',
      parser: (json) => json,
    );
    return Department.fromJson(response['data']);
  }

  /// 获取部门下的用户列表
  static Future<List<Map<String, dynamic>>> getDepartmentUsers(
    String departmentId, {
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/departments/$departmentId/users',
      queryParameters: {
        'page': page.toString(),
        'pageSize': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取部门子结构
  static Future<List<Department>> getDepartmentSubtree(String departmentId) async {
    final response = await _api.get(
      '/departments/$departmentId/tree',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return (list as List).map((e) => Department.fromJson(e)).toList();
  }

  /// 获取部门下的资产列表
  static Future<List<Map<String, dynamic>>> getDepartmentAssets(
    String departmentId, {
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/departments/$departmentId/assets',
      queryParameters: {
        'page': page.toString(),
        'pageSize': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }
}
