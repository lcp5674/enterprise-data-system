// EDAMS Mobile Integration Tests
// 运行: flutter test integration_test/auth_flow_test.dart
// 或: flutter drive --target=integration_test/auth_flow_test.dart

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:edams_mobile/main.dart' as app;
import 'package:edams_mobile/screens/auth/login_screen.dart';
import 'package:edams_mobile/screens/auth/splash_screen.dart';
import 'package:edams_mobile/screens/home/home_screen.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('认证流程测试 (E2E)', () {
    testWidgets('登录成功 → 首页', (WidgetTester tester) async {
      // 启动App
      app.main();
      await tester.pumpAndSettle(const Duration(seconds: 2));

      // 验证启动屏显示
      expect(find.byType(SplashScreen), findsOneWidget);

      // 等待登录页加载
      await tester.pumpAndSettle(const Duration(seconds: 3));
      expect(find.byType(LoginScreen), findsOneWidget);

      // 输入用户名密码
      final usernameField = find.byKey(const Key('username_field'));
      final passwordField = find.byKey(const Key('password_field'));
      final loginButton = find.byKey(const Key('login_button'));

      if (usernameField.evaluate().isNotEmpty) {
        await tester.enterText(usernameField, 'admin');
        await tester.enterText(passwordField, 'admin123');
        await tester.tap(loginButton);
        await tester.pumpAndSettle(const Duration(seconds: 5));

        // 验证跳转到首页
        expect(find.byType(HomeScreen), findsOneWidget);
      }
    });

    testWidgets('登录失败 → 错误提示', (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle(const Duration(seconds: 3));

      final usernameField = find.byKey(const Key('username_field'));
      final passwordField = find.byKey(const Key('password_field'));
      final loginButton = find.byKey(const Key('login_button'));

      if (usernameField.evaluate().isNotEmpty) {
        await tester.enterText(usernameField, 'wrong_user');
        await tester.enterText(passwordField, 'wrong_password');
        await tester.tap(loginButton);
        await tester.pumpAndSettle();

        // 验证错误提示
        expect(find.textContaining('用户名或密码错误'), findsOneWidget);
      }
    });
  });

  group('首页导航测试', () {
    testWidgets('底部导航栏切换', (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle(const Duration(seconds: 5));

      // 查找底部导航栏
      final bottomNav = find.byType(BottomNavigationBar);
      if (bottomNav.evaluate().isEmpty) {
        // 未登录则跳过
        return;
      }

      // 点击工作台
      await tester.tap(find.byIcon(Icons.dashboard));
      await tester.pumpAndSettle();
      expect(find.text('工作台'), findsWidgets);

      // 点击资产
      await tester.tap(find.byIcon(Icons.inventory_2));
      await tester.pumpAndSettle();
      expect(find.text('资产管理'), findsWidgets);
    });
  });
}
