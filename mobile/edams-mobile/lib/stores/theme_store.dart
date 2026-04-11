import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/app_config.dart';

class ThemeStore extends ChangeNotifier {
  ThemeMode _themeMode = ThemeMode.light;

  ThemeMode get themeMode => _themeMode;

  bool get isDark => _themeMode == ThemeMode.dark;

  String get currentTheme => isDark ? '深色模式' : '浅色模式';

  ThemeStore() {
    _loadTheme();
  }

  Future<void> _loadTheme() async {
    final prefs = AppConfig.prefs;
    final themeCode = prefs.getString(AppConfig.themeKey);
    if (themeCode != null) {
      _themeMode = themeCode == 'dark' ? ThemeMode.dark : ThemeMode.light;
      notifyListeners();
    }
  }

  Future<void> setTheme(ThemeMode mode) async {
    _themeMode = mode;
    await AppConfig.prefs.setString(
      AppConfig.themeKey,
      mode == ThemeMode.dark ? 'dark' : 'light',
    );
    notifyListeners();
  }

  Future<void> toggleTheme() async {
    await setTheme(isDark ? ThemeMode.light : ThemeMode.dark);
  }
}
