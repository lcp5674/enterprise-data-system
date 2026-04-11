/// 数据目录模型
class DataCatalog {
  final String id;
  final String name;
  final String code;
  final String? description;
  final String? parentId;
  final int level;
  final int sortOrder;
  final int assetCount;
  final String? icon;
  final String? color;
  final List<DataCatalog>? children;
  final DateTime? createTime;
  final DateTime? updateTime;

  DataCatalog({
    required this.id,
    required this.name,
    required this.code,
    this.description,
    this.parentId,
    this.level = 0,
    this.sortOrder = 0,
    this.assetCount = 0,
    this.icon,
    this.color,
    this.children,
    this.createTime,
    this.updateTime,
  });

  factory DataCatalog.fromJson(Map<String, dynamic> json) {
    return DataCatalog(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      code: json['code'] ?? '',
      description: json['description'],
      parentId: json['parentId']?.toString(),
      level: json['level'] ?? 0,
      sortOrder: json['sortOrder'] ?? 0,
      assetCount: json['assetCount'] ?? 0,
      icon: json['icon'],
      color: json['color'],
      children: json['children'] != null
          ? (json['children'] as List)
              .map((e) => DataCatalog.fromJson(e))
              .toList()
          : null,
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'])
          : null,
      updateTime: json['updateTime'] != null
          ? DateTime.parse(json['updateTime'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'code': code,
      'description': description,
      'parentId': parentId,
      'level': level,
      'sortOrder': sortOrder,
      'assetCount': assetCount,
      'icon': icon,
      'color': color,
      'children': children?.map((e) => e.toJson()).toList(),
      'createTime': createTime?.toIso8601String(),
      'updateTime': updateTime?.toIso8601String(),
    };
  }

  bool get hasChildren => children != null && children!.isNotEmpty;
}
