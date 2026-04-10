class AppConfig {
  static const String appName = '企业数据资产管理';
  static const String appVersion = '1.0.0';

  // API 配置
  static const String baseUrl = 'http://localhost:8080/api';
  static const Duration apiTimeout = Duration(seconds: 30);

  // 本地存储 Key
  static const String tokenKey = 'auth_token';
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
}

// 动态 import shared_preferences
import 'package:shared_preferences/shared_preferences.dart';
