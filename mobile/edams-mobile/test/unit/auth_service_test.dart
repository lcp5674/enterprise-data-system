// EDAMS Mobile - 认证服务单元测试
// 运行: flutter test test/unit/auth_service_test.dart

import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:edams_mobile/services/auth_service.dart';

void main() {
  group('AuthService 单元测试', () {
    setUp(() {
      SharedPreferences.setMockInitialValues({});
    });

    test('保存Token到本地存储', () async {
      final authService = AuthService();

      await authService.saveToken('test_jwt_token_12345');

      final token = await authService.getToken();
      expect(token, 'test_jwt_token_12345');
    });

    test('清除Token实现登出', () async {
      final authService = AuthService();
      await authService.saveToken('test_token');

      await authService.clearToken();

      final token = await authService.getToken();
      expect(token, isNull);
    });

    test('检查登录状态', () async {
      final authService = AuthService();

      // 无Token时返回false
      expect(await authService.isLoggedIn(), false);

      // 有Token时返回true
      await authService.saveToken('valid_token');
      expect(await authService.isLoggedIn(), true);
    });

    test('Token过期检查', () async {
      final authService = AuthService();

      // 设置已过期的Token（这里用无效格式测试）
      SharedPreferences.setMockInitialValues({
        'auth_token': 'expired.invalid.token',
        'token_time': DateTime.now()
            .subtract(const Duration(hours: 25))
            .millisecondsSinceEpoch
            .toString(),
      });

      final isExpired = await authService.isTokenExpired();
      expect(isExpired, true);
    });
  });
}
