import '../models/user.dart';
import 'api_service.dart';

/// 角色服务
class RoleService {
  static final ApiService _api = ApiService();

  /// 获取角色列表
  static Future<List<Role>> getRoles({
    int page = 1,
    int pageSize = 20,
    String? keyword,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (keyword != null && keyword.isNotEmpty) {
      params['keyword'] = keyword;
    }

    final response = await _api.get(
      '/roles',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => Role.fromJson(e)).toList();
  }

  /// 获取角色详情
  static Future<Role> getRoleDetail(String roleId) async {
    final response = await _api.get(
      '/roles/$roleId',
      parser: (json) => json,
    );
    return Role.fromJson(response['data']);
  }

  /// 获取角色树
  static Future<List<Map<String, dynamic>>> getRoleTree() async {
    final response = await _api.get(
      '/roles/tree',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return List<Map<String, dynamic>>.from(list);
  }

  /// 获取角色的权限列表
  static Future<List<String>> getRolePermissions(String roleId) async {
    final response = await _api.get(
      '/roles/$roleId/permissions',
      parser: (json) => json,
    );
    final list = response['data'] ?? [];
    return List<String>.from(list);
  }

  /// 获取角色的用户列表
  static Future<List<Map<String, dynamic>>> getRoleUsers(
    String roleId, {
    int page = 1,
    int pageSize = 20,
  }) async {
    final response = await _api.get(
      '/roles/$roleId/users',
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
