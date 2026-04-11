/// 用户模型
class User {
  final String id;
  final String username;
  final String nickname;
  final String email;
  final String? phone;
  final String? avatar;
  final Department? department;
  final List<Role> roles;
  final List<String> permissions;
  final int status;
  final int userType;
  final DateTime? lastLoginTime;
  final String? lastLoginIp;
  final DateTime createdTime;
  final DateTime? updatedTime;

  User({
    required this.id,
    required this.username,
    required this.nickname,
    required this.email,
    this.phone,
    this.avatar,
    this.department,
    this.roles = const [],
    this.permissions = const [],
    this.status = 1,
    this.userType = 1,
    this.lastLoginTime,
    this.lastLoginIp,
    required this.createdTime,
    this.updatedTime,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id']?.toString() ?? '',
      username: json['username'] ?? '',
      nickname: json['nickname'] ?? json['name'] ?? '',
      email: json['email'] ?? '',
      phone: json['phone'],
      avatar: json['avatar'],
      department: json['department'] != null
          ? Department.fromJson(json['department'])
          : null,
      roles: json['roles'] != null
          ? (json['roles'] as List).map((e) => Role.fromJson(e)).toList()
          : [],
      permissions: json['permissions'] != null
          ? List<String>.from(json['permissions'])
          : [],
      status: json['status'] ?? 1,
      userType: json['userType'] ?? 1,
      lastLoginTime: json['lastLoginTime'] != null
          ? DateTime.parse(json['lastLoginTime'])
          : null,
      lastLoginIp: json['lastLoginIp'],
      createdTime: json['createdTime'] != null
          ? DateTime.parse(json['createdTime'])
          : DateTime.now(),
      updatedTime: json['updatedTime'] != null
          ? DateTime.parse(json['updatedTime'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'nickname': nickname,
      'email': email,
      'phone': phone,
      'avatar': avatar,
      'department': department?.toJson(),
      'roles': roles.map((e) => e.toJson()).toList(),
      'permissions': permissions,
      'status': status,
      'userType': userType,
      'lastLoginTime': lastLoginTime?.toIso8601String(),
      'lastLoginIp': lastLoginIp,
      'createdTime': createdTime.toIso8601String(),
      'updatedTime': updatedTime?.toIso8601String(),
    };
  }

  User copyWith({
    String? id,
    String? username,
    String? nickname,
    String? email,
    String? phone,
    String? avatar,
    Department? department,
    List<Role>? roles,
    List<String>? permissions,
    int? status,
    int? userType,
    DateTime? lastLoginTime,
    String? lastLoginIp,
    DateTime? createdTime,
    DateTime? updatedTime,
  }) {
    return User(
      id: id ?? this.id,
      username: username ?? this.username,
      nickname: nickname ?? this.nickname,
      email: email ?? this.email,
      phone: phone ?? this.phone,
      avatar: avatar ?? this.avatar,
      department: department ?? this.department,
      roles: roles ?? this.roles,
      permissions: permissions ?? this.permissions,
      status: status ?? this.status,
      userType: userType ?? this.userType,
      lastLoginTime: lastLoginTime ?? this.lastLoginTime,
      lastLoginIp: lastLoginIp ?? this.lastLoginIp,
      createdTime: createdTime ?? this.createdTime,
      updatedTime: updatedTime ?? this.updatedTime,
    );
  }

  /// 是否为管理员
  bool get isAdmin => roles.any((r) => r.code == 'ROLE_ADMIN' || r.code == 'ADMIN');

  /// 用户状态是否正常
  bool get isActive => status == 1;
}

/// 部门模型
class Department {
  final String id;
  final String name;
  final String path;
  final String? parentId;
  final int level;
  final int? memberCount;
  final UserBasic? leader;
  final DateTime? createTime;

  Department({
    required this.id,
    required this.name,
    required this.path,
    this.parentId,
    this.level = 0,
    this.memberCount,
    this.leader,
    this.createTime,
  });

  factory Department.fromJson(Map<String, dynamic> json) {
    return Department(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      path: json['path'] ?? '',
      parentId: json['parentId']?.toString(),
      level: json['level'] ?? 0,
      memberCount: json['memberCount'],
      leader: json['leader'] != null ? UserBasic.fromJson(json['leader']) : null,
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'path': path,
      'parentId': parentId,
      'level': level,
      'memberCount': memberCount,
      'leader': leader?.toJson(),
      'createTime': createTime?.toIso8601String(),
    };
  }
}

/// 角色模型
class Role {
  final String id;
  final String code;
  final String name;
  final String? description;
  final int? roleType;
  final int status;
  final List<String>? permissions;

  Role({
    required this.id,
    required this.code,
    required this.name,
    this.description,
    this.roleType,
    this.status = 1,
    this.permissions,
  });

  factory Role.fromJson(Map<String, dynamic> json) {
    return Role(
      id: json['id']?.toString() ?? '',
      code: json['code'] ?? '',
      name: json['name'] ?? '',
      description: json['description'],
      roleType: json['roleType'],
      status: json['status'] ?? 1,
      permissions: json['permissions'] != null
          ? List<String>.from(json['permissions'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'code': code,
      'name': name,
      'description': description,
      'roleType': roleType,
      'status': status,
      'permissions': permissions,
    };
  }
}

/// 用户基本信息
class UserBasic {
  final String id;
  final String name;
  final String? avatar;

  UserBasic({
    required this.id,
    required this.name,
    this.avatar,
  });

  factory UserBasic.fromJson(Map<String, dynamic> json) {
    return UserBasic(
      id: json['id']?.toString() ?? '',
      name: json['name'] ?? '',
      avatar: json['avatar'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'avatar': avatar,
    };
  }
}
