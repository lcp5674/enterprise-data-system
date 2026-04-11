import 'api_service.dart';

/// 用户服务
class UserService {
  static final ApiService _api = ApiService();

  /// 获取当前用户信息
  static Future<Map<String, dynamic>> getCurrentUser() async {
    final response = await _api.get(
      '/auth/me',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 更新用户资料
  static Future<bool> updateProfile(Map<String, dynamic> data) async {
    final response = await _api.put(
      '/users/me/profile',
      body: data,
      parser: (json) => json,
    );
    return response['code'] == 200 || response['success'] == true;
  }

  /// 修改密码
  static Future<bool> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    final response = await _api.post(
      '/users/me/password',
      body: {
        'oldPassword': oldPassword,
        'newPassword': newPassword,
      },
      parser: (json) => json,
    );
    return response['code'] == 200 || response['success'] == true;
  }

  /// 上传头像
  static Future<String?> uploadAvatar(String filePath) async {
    // 使用 multipart 上传
    final response = await _api.post(
      '/users/me/avatar',
      body: {'filePath': filePath},
      parser: (json) => json,
    );
    return response['data']?['url'];
  }

  /// 获取用户偏好设置
  static Future<Map<String, dynamic>> getPreferences() async {
    final response = await _api.get(
      '/users/me/preferences',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 更新用户偏好设置
  static Future<bool> updatePreferences(Map<String, dynamic> preferences) async {
    final response = await _api.put(
      '/users/me/preferences',
      body: preferences,
      parser: (json) => json,
    );
    return response['code'] == 200 || response['success'] == true;
  }

  /// 获取工作台数据
  static Future<Map<String, dynamic>> getWorkbench() async {
    final response = await _api.get(
      '/users/me/workbench',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }
}
