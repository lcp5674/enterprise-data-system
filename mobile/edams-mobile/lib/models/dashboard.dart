/// 首页统计数据模型
class DashboardStats {
  final int totalAssets;
  final int activeAssets;
  final int totalCatalogs;
  final double averageQualityScore;
  final int pendingTasks;
  final int unreadNotifications;
  final int myFavorites;
  final List<RecentAsset>? recentAssets;
  final List<TodoItem>? todoItems;

  DashboardStats({
    this.totalAssets = 0,
    this.activeAssets = 0,
    this.totalCatalogs = 0,
    this.averageQualityScore = 0,
    this.pendingTasks = 0,
    this.unreadNotifications = 0,
    this.myFavorites = 0,
    this.recentAssets,
    this.todoItems,
  });

  factory DashboardStats.fromJson(Map<String, dynamic> json) {
    return DashboardStats(
      totalAssets: json['totalAssets'] ?? 0,
      activeAssets: json['activeAssets'] ?? 0,
      totalCatalogs: json['totalCatalogs'] ?? 0,
      averageQualityScore: (json['averageQualityScore'] ?? 0).toDouble(),
      pendingTasks: json['pendingTasks'] ?? 0,
      unreadNotifications: json['unreadNotifications'] ?? 0,
      myFavorites: json['myFavorites'] ?? 0,
      recentAssets: json['recentAssets'] != null
          ? (json['recentAssets'] as List)
              .map((e) => RecentAsset.fromJson(e))
              .toList()
          : null,
      todoItems: json['todoItems'] != null
          ? (json['todoItems'] as List)
              .map((e) => TodoItem.fromJson(e))
              .toList()
          : null,
    );
  }
}

/// 最近访问资产
class RecentAsset {
  final String id;
  final String name;
  final String code;
  final String assetType;
  final DateTime accessTime;

  RecentAsset({
    required this.id,
    required this.name,
    required this.code,
    required this.assetType,
    required this.accessTime,
  });

  factory RecentAsset.fromJson(Map<String, dynamic> json) {
    return RecentAsset(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      code: json['code'] ?? '',
      assetType: json['assetType'] ?? 'table',
      accessTime: json['accessTime'] != null
          ? DateTime.parse(json['accessTime'])
          : DateTime.now(),
    );
  }
}

/// 待办事项
class TodoItem {
  final String id;
  final String title;
  final String? description;
  final String type; // approval, review, task
  final String? priority; // high, medium, low
  final DateTime dueTime;
  final bool isCompleted;

  TodoItem({
    required this.id,
    required this.title,
    this.description,
    required this.type,
    this.priority,
    required this.dueTime,
    this.isCompleted = false,
  });

  factory TodoItem.fromJson(Map<String, dynamic> json) {
    return TodoItem(
      id: json['id']?.toString() ?? '',
      title: json['title'] ?? '',
      description: json['description'],
      type: json['type'] ?? 'task',
      priority: json['priority'],
      dueTime: json['dueTime'] != null
          ? DateTime.parse(json['dueTime'])
          : DateTime.now(),
      isCompleted: json['isCompleted'] ?? false,
    );
  }
}
