import '../config/app_config.dart';
import '../models/user.dart';
import 'api_service.dart';

class AuthService {
  static final ApiService _api = ApiService();

  /// 用户登录
  static Future<User?> login(String username, String password) async {
    try {
      final response = await _api.post(
        '/auth/login',
        body: {
          'username': username,
          'password': password,
        },
        parser: (json) => json,
      );

      if (response['code'] == 200 || response['success'] == true) {
        final user = User.fromJson(response['data']);
        // 保存 token
        await AppConfig.prefs.setString(
          AppConfig.tokenKey,
          user.token ?? '',
        );
        _api.setToken(user.token);
        return user;
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// 获取当前用户信息
  static Future<User> getCurrentUser() async {
    final response = await _api.get(
      '/auth/current',
      parser: (json) => json,
    );

    return User.fromJson(response['data']);
  }

  /// 退出登录
  static Future<void> logout() async {
    try {
      await _api.post('/auth/logout');
    } catch (e) {
      // 忽略退出登录的错误
    }
    await AppConfig.prefs.remove(AppConfig.tokenKey);
    _api.setToken(null);
  }

  /// 修改密码
  static Future<bool> changePassword(
    String oldPassword,
    String newPassword,
  ) async {
    final response = await _api.post(
      '/auth/change-password',
      body: {
        'oldPassword': oldPassword,
        'newPassword': newPassword,
      },
      parser: (json) => json,
    );

    return response['code'] == 200 || response['success'] == true;
  }
}
