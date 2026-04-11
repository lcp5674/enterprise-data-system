/// 消息通知模型
class AppNotification {
  final String id;
  final String title;
  final String content;
  final String type; // system, asset, quality, workflow
  final bool isRead;
  final DateTime createTime;
  final Map<String, dynamic>? extraData;

  AppNotification({
    required this.id,
    required this.title,
    required this.content,
    required this.type,
    this.isRead = false,
    required this.createTime,
    this.extraData,
  });

  factory AppNotification.fromJson(Map<String, dynamic> json) {
    return AppNotification(
      id: json['id']?.toString() ?? '',
      title: json['title'] ?? '',
      content: json['content'] ?? '',
      type: json['type'] ?? 'system',
      isRead: json['isRead'] ?? false,
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'])
          : DateTime.now(),
      extraData: json['extraData'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'content': content,
      'type': type,
      'isRead': isRead,
      'createTime': createTime.toIso8601String(),
      'extraData': extraData,
    };
  }

  AppNotification copyWith({
    String? id,
    String? title,
    String? content,
    String? type,
    bool? isRead,
    DateTime? createTime,
    Map<String, dynamic>? extraData,
  }) {
    return AppNotification(
      id: id ?? this.id,
      title: title ?? this.title,
      content: content ?? this.content,
      type: type ?? this.type,
      isRead: isRead ?? this.isRead,
      createTime: createTime ?? this.createTime,
      extraData: extraData ?? this.extraData,
    );
  }

  String get typeName {
    switch (type) {
      case 'system':
        return '系统通知';
      case 'asset':
        return '资产变更';
      case 'quality':
        return '质量告警';
      case 'workflow':
        return '流程审批';
      default:
        return type;
    }
  }
}
