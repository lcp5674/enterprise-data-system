import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/app_config.dart';

class LocaleStore extends ChangeNotifier {
  Locale _locale = const Locale('zh', 'CN');

  Locale get locale => _locale;

  LocaleStore() {
    _loadLocale();
  }

  Future<void> _loadLocale() async {
    final prefs = AppConfig.prefs;
    final localeCode = prefs.getString(AppConfig.localeKey);
    if (localeCode != null) {
      final parts = localeCode.split('_');
      if (parts.length == 2) {
        _locale = Locale(parts[0], parts[1]);
        notifyListeners();
      }
    }
  }

  Future<void> setLocale(Locale locale) async {
    _locale = locale;
    await AppConfig.prefs.setString(
      AppConfig.localeKey,
      '${locale.languageCode}_${locale.countryCode}',
    );
    notifyListeners();
  }

  void toggleLocale() {
    if (_locale.languageCode == 'zh') {
      setLocale(const Locale('en', 'US'));
    } else {
      setLocale(const Locale('zh', 'CN'));
    }
  }

  bool get isZh => _locale.languageCode == 'zh';

  String get currentLanguage => isZh ? '中文' : 'English';
}
