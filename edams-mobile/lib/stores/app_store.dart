import 'package:flutter/material.dart';
import '../config/app_config.dart';
import '../models/user.dart';
import '../services/auth_service.dart';

class AppStore extends ChangeNotifier {
  User? _currentUser;
  bool _isLoading = false;
  String? _error;
  bool _isLoggedIn = false;

  User? get currentUser => _currentUser;
  bool get isLoading => _isLoading;
  String? get error => _error;
  bool get isLoggedIn => _isLoggedIn;

  Future<void> init() async {
    await checkLoginStatus();
  }

  Future<void> checkLoginStatus() async {
    final token = AppConfig.prefs.getString(AppConfig.tokenKey);
    if (token != null && token.isNotEmpty) {
      _isLoggedIn = true;
      // 可以在这里加载用户信息
    }
    notifyListeners();
  }

  Future<bool> login(String username, String password) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final result = await AuthService.login(username, password);
      if (result != null) {
        _currentUser = result;
        _isLoggedIn = true;
        await AppConfig.prefs.setString(AppConfig.tokenKey, result.token ?? '');
        _isLoading = false;
        notifyListeners();
        return true;
      } else {
        _error = '登录失败';
        _isLoading = false;
        notifyListeners();
        return false;
      }
    } catch (e) {
      _error = e.toString();
      _isLoading = false;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    await AppConfig.prefs.remove(AppConfig.tokenKey);
    await AppConfig.prefs.remove(AppConfig.userInfoKey);
    _currentUser = null;
    _isLoggedIn = false;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
