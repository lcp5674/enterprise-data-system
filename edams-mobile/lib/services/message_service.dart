import '../models/notification.dart';
import '../models/dashboard.dart';
import 'api_service.dart';

class MessageService {
  static final ApiService _api = ApiService();

  /// 获取通知列表
  static Future<List<AppNotification>> getNotifications({
    String? type,
    bool? isRead,
    int page = 1,
    int pageSize = 20,
  }) async {
    final params = <String, String>{
      'page': page.toString(),
      'pageSize': pageSize.toString(),
    };
    if (type != null && type.isNotEmpty) {
      params['type'] = type;
    }
    if (isRead != null) {
      params['isRead'] = isRead.toString();
    }

    final response = await _api.get(
      '/mobile/notifications',
      queryParameters: params,
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => AppNotification.fromJson(e)).toList();
  }

  /// 获取未读通知数量
  static Future<int> getUnreadCount() async {
    final response = await _api.get(
      '/mobile/notifications/unread-count',
      parser: (json) => json,
    );
    return response['data'] ?? 0;
  }

  /// 标记通知为已读
  static Future<bool> markAsRead(String notificationId) async {
    final response = await _api.post(
      '/mobile/notifications/$notificationId/read',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 标记所有通知为已读
  static Future<bool> markAllAsRead() async {
    final response = await _api.post(
      '/mobile/notifications/read-all',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 删除通知
  static Future<bool> deleteNotification(String notificationId) async {
    final response = await _api.delete(
      '/mobile/notifications/$notificationId',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }

  /// 获取待办事项
  static Future<List<TodoItem>> getTodoItems({int page = 1, int pageSize = 20}) async {
    final response = await _api.get(
      '/mobile/todos',
      queryParameters: {
        'page': page.toString(),
        'pageSize': pageSize.toString(),
      },
      parser: (json) => json,
    );
    final list = response['data']?['list'] ?? response['data'] ?? [];
    return (list as List).map((e) => TodoItem.fromJson(e)).toList();
  }

  /// 获取待办事项数量
  static Future<int> getTodoCount() async {
    final response = await _api.get(
      '/mobile/todos/count',
      parser: (json) => json,
    );
    return response['data'] ?? 0;
  }

  /// 完成待办事项
  static Future<bool> completeTodo(String todoId) async {
    final response = await _api.post(
      '/mobile/todos/$todoId/complete',
      parser: (json) => json,
    );
    return response['success'] == true || response['code'] == 200;
  }
}
