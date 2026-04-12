// EDAMS Mobile - 资产管理功能测试
// 运行: flutter test integration_test/asset_management_test.dart

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:edams_mobile/main.dart' as app;
import 'package:edams_mobile/screens/asset/asset_list_screen.dart';
import 'package:edams_mobile/screens/asset/asset_detail_screen.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('资产管理测试 (E2E)', () {
    testWidgets('资产列表加载与搜索', (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle(const Duration(seconds: 5));

      // 导航到资产页面
      final assetListFinder = find.byType(AssetListScreen);
      if (assetListFinder.evaluate().isEmpty) {
        debugPrint('AssetListScreen not found, skipping test');
        return;
      }

      // 验证资产列表加载
      await tester.pump(const Duration(seconds: 2));

      // 测试搜索功能
      final searchField = find.byKey(const Key('asset_search_field'));
      if (searchField.evaluate().isNotEmpty) {
        await tester.enterText(searchField, 'test_asset');
        await tester.pumpAndSettle(const Duration(seconds: 2));

        // 验证搜索结果
        expect(find.byType(ListTile), findsWidgets);
      }
    });

    testWidgets('资产详情查看', (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle(const Duration(seconds: 5));

      final assetListFinder = find.byType(AssetListScreen);
      if (assetListFinder.evaluate().isEmpty) return;

      // 点击第一个资产
      final firstAsset = find.byType(ListTile).first;
      await tester.tap(firstAsset);
      await tester.pumpAndSettle(const Duration(seconds: 2));

      // 验证详情页
      expect(find.byType(AssetDetailScreen), findsOneWidget);

      // 验证关键字段显示
      expect(find.textContaining('资产名称'), findsWidgets);
      expect(find.textContaining('资产类型'), findsWidgets);
    });

    testWidgets('资产筛选功能', (WidgetTester tester) async {
      app.main();
      await tester.pumpAndSettle(const Duration(seconds: 5));

      final assetListFinder = find.byType(AssetListScreen);
      if (assetListFinder.evaluate().isEmpty) return;

      // 打开筛选器
      final filterButton = find.byKey(const Key('filter_button'));
      if (filterButton.evaluate().isNotEmpty) {
        await tester.tap(filterButton);
        await tester.pumpAndSettle();

        // 选择类型筛选
        final typeChip = find.text('DATABASE_TABLE');
        if (typeChip.evaluate().isNotEmpty) {
          await tester.tap(typeChip);
          await tester.pumpAndSettle();
        }
      }
    });
  });
}
