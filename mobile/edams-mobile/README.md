# 企业数据资产管理系统 - 移动端

> Enterprise Data Asset Management System - Mobile App

基于 Flutter 构建的企业数据资产管理移动端应用，提供数据资产浏览、搜索、血缘分析、质量监控等功能。

## 功能特性

### 核心功能
- **首页仪表盘**: 系统概览、快捷入口、最近访问、待办事项
- **数据资产模块**: 资产搜索、列表展示、详情查看、收藏订阅
- **数据目录模块**: 目录树形结构、分类浏览
- **数据地图模块**: 血缘关系查看、影响分析
- **数据质量模块**: 质量评分展示、问题列表、趋势分析
- **消息通知模块**: 通知列表、分类筛选、已读管理
- **个人设置**: 语言切换、主题设置、账号管理

### 技术特性
- Flutter 3.x + Dart
- Provider 状态管理
- REST API 集成
- 本地缓存支持
- 二维码扫描集成

## 项目结构

```
edams-mobile/
├── lib/
│   ├── main.dart                 # 应用入口
│   ├── config/                   # 配置
│   │   ├── app_config.dart       # 应用配置
│   │   └── theme.dart            # 主题配置
│   ├── models/                   # 数据模型
│   │   ├── user.dart
│   │   ├── data_asset.dart
│   │   ├── data_catalog.dart
│   │   ├── data_map.dart
│   │   ├── data_quality.dart
│   │   ├── dashboard.dart
│   │   └── notification.dart
│   ├── services/                 # API 服务
│   │   ├── api_service.dart
│   │   ├── auth_service.dart
│   │   ├── asset_service.dart
│   │   ├── catalog_service.dart
│   │   ├── datamap_service.dart
│   │   ├── quality_service.dart
│   │   └── message_service.dart
│   ├── stores/                   # 状态管理
│   │   ├── locale_store.dart
│   │   └── app_store.dart
│   ├── pages/                    # 页面
│   │   ├── splash_page.dart
│   │   ├── login_page.dart
│   │   ├── main_page.dart
│   │   ├── home/
│   │   │   └── home_page.dart
│   │   ├── asset/
│   │   │   ├── asset_list_page.dart
│   │   │   └── asset_detail_page.dart
│   │   ├── catalog/
│   │   │   └── catalog_page.dart
│   │   ├── datamap/
│   │   │   ├── datamap_page.dart
│   │   │   └── lineage_page.dart
│   │   ├── quality/
│   │   │   ├── quality_page.dart
│   │   │   └── quality_detail_page.dart
│   │   ├── message/
│   │   │   └── message_page.dart
│   │   ├── settings/
│   │   │   └── settings_page.dart
│   │   └── scan/
│   │       └── scan_page.dart
│   └── widgets/                  # 通用组件
├── pubspec.yaml
└── README.md
```

## 快速开始

### 环境要求
- Flutter SDK >= 3.0.0
- Dart SDK >= 3.0.0
- iOS 12.0+ / Android 5.0+

### 安装步骤

1. **克隆项目**
```bash
cd enterprise-data-system/edams-mobile
```

2. **安装依赖**
```bash
flutter pub get
```

3. **配置环境**
   - 编辑 `lib/config/app_config.dart` 修改 API 地址
   ```dart
   static const String baseUrl = 'http://your-api-server:8080/api';
   ```

4. **运行应用**
```bash
# Debug 模式
flutter run

# Release 模式
flutter run --release
```

### 构建发布

**Android**
```bash
# Debug APK
flutter build apk --debug

# Release APK
flutter build apk --release

# 生成 AAB (Google Play)
flutter build appbundle
```

**iOS**
```bash
# Debug
flutter run -d <device_id>

# Release (需要配置签名)
flutter build ios --release
```

## 配置说明

### API 配置
编辑 `lib/config/app_config.dart`:
```dart
class AppConfig {
  static const String baseUrl = 'http://localhost:8080/api';
  static const Duration apiTimeout = Duration(seconds: 30);
  // ...
}
```

### 本地化配置
应用支持中英文切换，可通过设置页面修改。

### 主题配置
编辑 `lib/config/theme.dart` 自定义应用主题色。

## 依赖说明

| 依赖包 | 版本 | 说明 |
|-------|------|------|
| provider | ^6.1.1 | 状态管理 |
| http | ^1.1.0 | 网络请求 |
| shared_preferences | ^2.2.2 | 本地存储 |
| fl_chart | ^0.65.0 | 图表展示 |
| mobile_scanner | ^3.5.5 | 二维码扫描 |
| cached_network_image | ^3.3.0 | 图片缓存 |

## 与后端集成

应用通过 REST API 与后端服务通信，API 路径格式：

```
GET/POST/PUT/DELETE /api/{module}/{action}
```

主要 API 端点：

| 模块 | 端点 | 说明 |
|-----|------|------|
| Dashboard | `/mobile/dashboard/stats` | 首页统计 |
| Asset | `/mobile/assets/search` | 资产搜索 |
| Asset | `/mobile/assets/{id}` | 资产详情 |
| Catalog | `/mobile/catalogs/tree` | 目录树 |
| DataMap | `/mobile/datamap/lineage/{id}` | 血缘关系 |
| Quality | `/mobile/quality/{assetId}` | 质量评分 |
| Notification | `/mobile/notifications` | 通知列表 |

## 开发指南

### 添加新页面
1. 在 `pages/` 下创建页面文件夹
2. 创建页面组件并继承 `StatefulWidget`
3. 在 `main_page.dart` 中添加导航入口

### 添加新模型
1. 在 `models/` 下创建模型文件
2. 实现 `fromJson()` 和 `toJson()` 方法

### 添加新 API
1. 在 `services/` 下创建服务文件
2. 继承 `ApiService` 或使用其方法

## 常见问题

### Q: 应用无法连接 API
A: 检查 `lib/config/app_config.dart` 中的 `baseUrl` 配置，确保后端服务正常运行。

### Q: 二维码扫描不工作
A: 需要在 `pubspec.yaml` 中配置相机权限，并确保在 iOS 的 `Info.plist` 中添加相机使用说明。

### Q: 如何切换主题
A: 在设置页面点击深色模式开关，或修改 `lib/config/theme.dart` 自定义主题。

## 许可证

Copyright © 2024 Enterprise Data Team. All rights reserved.
