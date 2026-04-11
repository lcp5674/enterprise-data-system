import '../config/app_config.dart';
import '../models/user.dart';
import 'api_service.dart';

/// MFA 状态
class MFAStatus {
  final bool enabled;
  final String? secret;
  final String? qrCode;
  final List<String>? backupCodes;

  MFAStatus({
    required this.enabled,
    this.secret,
    this.qrCode,
    this.backupCodes,
  });

  factory MFAStatus.fromJson(Map<String, dynamic> json) {
    return MFAStatus(
      enabled: json['enabled'] ?? false,
      secret: json['secret'],
      qrCode: json['qrCode'],
      backupCodes: json['backupCodes'] != null
          ? List<String>.from(json['backupCodes'])
          : null,
    );
  }
}

/// 登录响应
class LoginResult {
  final String accessToken;
  final String refreshToken;
  final int expiresIn;
  final User user;

  LoginResult({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
    required this.user,
  });

  factory LoginResult.fromJson(Map<String, dynamic> json) {
    return LoginResult(
      accessToken: json['accessToken'] ?? '',
      refreshToken: json['refreshToken'] ?? '',
      expiresIn: json['expiresIn'] ?? 7200,
      user: User.fromJson(json['user'] ?? {}),
    );
  }
}

/// 认证服务
class AuthService {
  static final ApiService _api = ApiService();

  /// 用户登录
  static Future<LoginResult?> login({
    required String username,
    required String password,
    String? captcha,
    String? captchaId,
  }) async {
    try {
      final response = await _api.post(
        '/auth/login',
        body: {
          'loginType': 'PASSWORD',
          'username': username,
          'password': password,
          if (captcha != null) 'captcha': captcha,
          if (captchaId != null) 'captchaId': captchaId,
          'loginSource': 'APP',
        },
        parser: (json) => json,
      );

      if (response['code'] == 200 || response['success'] == true) {
        final result = LoginResult.fromJson(response['data'] ?? {});

        // 保存 token
        await AppConfig.prefs.setString(
          AppConfig.tokenKey,
          result.accessToken,
        );
        if (result.refreshToken.isNotEmpty) {
          await AppConfig.prefs.setString(
            AppConfig.refreshTokenKey,
            result.refreshToken,
          );
        }
        _api.setToken(result.accessToken);
        return result;
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// MFA 验证登录
  static Future<LoginResult?> verifyMFA({
    required String sessionToken,
    required String code,
  }) async {
    try {
      final response = await _api.post(
        '/auth/mfa/verify',
        body: {
          'sessionToken': sessionToken,
          'code': code,
        },
        parser: (json) => json,
      );

      if (response['code'] == 200 || response['success'] == true) {
        final result = LoginResult.fromJson(response['data'] ?? {});

        // 保存 token
        await AppConfig.prefs.setString(
          AppConfig.tokenKey,
          result.accessToken,
        );
        if (result.refreshToken.isNotEmpty) {
          await AppConfig.prefs.setString(
            AppConfig.refreshTokenKey,
            result.refreshToken,
          );
        }
        _api.setToken(result.accessToken);
        return result;
      }
      return null;
    } catch (e) {
      rethrow;
    }
  }

  /// 退出登录
  static Future<void> logout() async {
    try {
      await _api.post('/auth/logout');
    } catch (e) {
      // 忽略退出登录的错误
    }
    await AppConfig.prefs.remove(AppConfig.tokenKey);
    await AppConfig.prefs.remove(AppConfig.refreshTokenKey);
    _api.setToken(null);
  }

  /// 刷新 Token
  static Future<LoginResult?> refreshToken() async {
    try {
      final refreshToken = AppConfig.prefs.getString(AppConfig.refreshTokenKey);
      if (refreshToken == null || refreshToken.isEmpty) {
        return null;
      }

      final response = await _api.post(
        '/auth/refresh',
        body: {'refreshToken': refreshToken},
        parser: (json) => json,
      );

      if (response['code'] == 200 || response['success'] == true) {
        final result = LoginResult.fromJson(response['data'] ?? {});

        // 保存新的 token
        await AppConfig.prefs.setString(
          AppConfig.tokenKey,
          result.accessToken,
        );
        if (result.refreshToken.isNotEmpty) {
          await AppConfig.prefs.setString(
            AppConfig.refreshTokenKey,
            result.refreshToken,
          );
        }
        _api.setToken(result.accessToken);
        return result;
      }
      return null;
    } catch (e) {
      // Token 刷新失败，清除登录状态
      await AppConfig.prefs.remove(AppConfig.tokenKey);
      await AppConfig.prefs.remove(AppConfig.refreshTokenKey);
      _api.setToken(null);
      rethrow;
    }
  }

  /// 获取当前用户信息
  static Future<User> getCurrentUser() async {
    final response = await _api.get(
      '/auth/me',
      parser: (json) => json,
    );

    return User.fromJson(response['data'] ?? response);
  }

  /// 获取验证码
  static Future<Map<String, dynamic>> getCaptcha() async {
    final response = await _api.post(
      '/auth/captcha',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 修改密码
  static Future<bool> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    final response = await _api.post(
      '/auth/password/change',
      body: {
        'oldPassword': oldPassword,
        'newPassword': newPassword,
      },
      parser: (json) => json,
    );

    return response['code'] == 200 || response['success'] == true;
  }

  // ========== MFA 相关接口 ==========

  /// 获取 MFA 状态
  static Future<MFAStatus> getMFAStatus() async {
    final response = await _api.get(
      '/auth/mfa/status',
      parser: (json) => json,
    );
    return MFAStatus.fromJson(response['data'] ?? {});
  }

  /// 获取 MFA 设置信息
  static Future<Map<String, dynamic>> getMFASetup() async {
    final response = await _api.get(
      '/auth/mfa/setup',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 设置 MFA
  static Future<Map<String, dynamic>> setupMFA(String type) async {
    final response = await _api.post(
      '/auth/mfa/setup/$type',
      parser: (json) => json,
    );
    return response['data'] ?? {};
  }

  /// 启用 MFA
  static Future<bool> enableMFA(String code) async {
    final response = await _api.post(
      '/auth/mfa/enable',
      body: {'code': code},
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 禁用 MFA
  static Future<bool> disableMFA(String code) async {
    final response = await _api.post(
      '/auth/mfa/disable',
      body: {'code': code},
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 发送邮箱验证码
  static Future<bool> sendEmailCode() async {
    final response = await _api.post(
      '/auth/mfa/send-email-code',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 发送短信验证码
  static Future<bool> sendSmsCode() async {
    final response = await _api.post(
      '/auth/mfa/send-sms-code',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 检查是否已登录
  static bool get isLoggedIn {
    final token = AppConfig.prefs.getString(AppConfig.tokenKey);
    return token != null && token.isNotEmpty;
  }

  /// 获取存储的 Token
  static String? get storedToken {
    return AppConfig.prefs.getString(AppConfig.tokenKey);
  }

  /// 初始化登录状态
  static void initAuthState() {
    final token = AppConfig.prefs.getString(AppConfig.tokenKey);
    if (token != null && token.isNotEmpty) {
      _api.setToken(token);
    }
  }
}
