import 'package:shared_preferences/shared_preferences.dart';

class AppConfig {
  static const String appName = '企业数据资产管理';
  static const String appVersion = '1.0.0';

  // API 配置
  // 开发环境: http://localhost:8080/api
  // 测试环境: https://api-test.example.com/api
  // 生产环境: https://api.example.com/api
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://localhost:8080/api',
  );

  // API 超时配置
  static const Duration apiTimeout = Duration(
    seconds: 30,
  );

  // 连接超时
  static const Duration connectTimeout = Duration(
    seconds: 15,
  );

  // 接收超时
  static const Duration receiveTimeout = Duration(
    seconds: 30,
  );

  // 本地存储 Key
  static const String tokenKey = 'auth_token';
  static const String refreshTokenKey = 'auth_refresh_token';
  static const String userInfoKey = 'user_info';
  static const String localeKey = 'app_locale';
  static const String themeKey = 'app_theme';
  static const String cacheKey = 'data_cache';

  // 分页配置
  static const int defaultPageSize = 20;

  // 缓存配置
  static const Duration cacheExpiration = Duration(hours: 1);

  static late SharedPreferences _prefs;

  static Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  static SharedPreferences get prefs => _prefs;

  // 获取 API 基础URL
  static String getApiUrl(String path) {
    return '$baseUrl$path';
  }

  // 环境检测
  static bool get isDevelopment {
    return baseUrl.contains('localhost') || baseUrl.contains('dev');
  }

  static bool get isProduction {
    return baseUrl.contains('api.') && !baseUrl.contains('dev') && !baseUrl.contains('test');
  }

  static bool get isTest {
    return baseUrl.contains('test');
  }
}
