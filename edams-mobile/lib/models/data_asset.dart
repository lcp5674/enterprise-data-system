/// 数据资产模型
class DataAsset {
  final String id;
  final String name;
  final String code;
  final String? description;
  final String assetType; // table, file, api, stream
  final String? dataType; // 结构化, 半结构化, 非结构化
  final String? status; // active, inactive, archived
  final String? owner;
  final String? department;
  final int? recordCount;
  final String? storageSize;
  final DateTime? createTime;
  final DateTime? updateTime;
  final Map<String, dynamic>? technicalProperties; // 技术属性
  final Map<String, dynamic>? businessProperties; // 业务属性
  final List<String>? tags;
  final bool? isFavorite;
  final bool? isSubscribed;

  DataAsset({
    required this.id,
    required this.name,
    required this.code,
    this.description,
    required this.assetType,
    this.dataType,
    this.status,
    this.owner,
    this.department,
    this.recordCount,
    this.storageSize,
    this.createTime,
    this.updateTime,
    this.technicalProperties,
    this.businessProperties,
    this.tags,
    this.isFavorite,
    this.isSubscribed,
  });

  factory DataAsset.fromJson(Map<String, dynamic> json) {
    return DataAsset(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      code: json['code'] ?? '',
      description: json['description'],
      assetType: json['assetType'] ?? 'table',
      dataType: json['dataType'],
      status: json['status'] ?? 'active',
      owner: json['owner'],
      department: json['department'],
      recordCount: json['recordCount'],
      storageSize: json['storageSize'],
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'])
          : null,
      updateTime: json['updateTime'] != null
          ? DateTime.parse(json['updateTime'])
          : null,
      technicalProperties: json['technicalProperties'],
      businessProperties: json['businessProperties'],
      tags: json['tags'] != null ? List<String>.from(json['tags']) : null,
      isFavorite: json['isFavorite'] ?? false,
      isSubscribed: json['isSubscribed'] ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'code': code,
      'description': description,
      'assetType': assetType,
      'dataType': dataType,
      'status': status,
      'owner': owner,
      'department': department,
      'recordCount': recordCount,
      'storageSize': storageSize,
      'createTime': createTime?.toIso8601String(),
      'updateTime': updateTime?.toIso8601String(),
      'technicalProperties': technicalProperties,
      'businessProperties': businessProperties,
      'tags': tags,
      'isFavorite': isFavorite,
      'isSubscribed': isSubscribed,
    };
  }

  String get assetTypeName {
    switch (assetType) {
      case 'table':
        return '数据表';
      case 'file':
        return '文件';
      case 'api':
        return 'API接口';
      case 'stream':
        return '数据流';
      default:
        return assetType;
    }
  }

  String get statusName {
    switch (status) {
      case 'active':
        return '活跃';
      case 'inactive':
        return '停用';
      case 'archived':
        return '归档';
      default:
        return status ?? '';
    }
  }
}
