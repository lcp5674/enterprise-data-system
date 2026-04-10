class User {
  final String id;
  final String username;
  final String name;
  final String email;
  final String? avatar;
  final String? phone;
  final String? department;
  final String? role;
  final String? token;
  final DateTime? lastLoginTime;

  User({
    required this.id,
    required this.username,
    required this.name,
    required this.email,
    this.avatar,
    this.phone,
    this.department,
    this.role,
    this.token,
    this.lastLoginTime,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id']?.toString() ?? '',
      username: json['username'] ?? '',
      name: json['name'] ?? '',
      email: json['email'] ?? '',
      avatar: json['avatar'],
      phone: json['phone'],
      department: json['department'],
      role: json['role'],
      token: json['token'],
      lastLoginTime: json['lastLoginTime'] != null
          ? DateTime.parse(json['lastLoginTime'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'name': name,
      'email': email,
      'avatar': avatar,
      'phone': phone,
      'department': department,
      'role': role,
      'token': token,
      'lastLoginTime': lastLoginTime?.toIso8601String(),
    };
  }

  User copyWith({
    String? id,
    String? username,
    String? name,
    String? email,
    String? avatar,
    String? phone,
    String? department,
    String? role,
    String? token,
    DateTime? lastLoginTime,
  }) {
    return User(
      id: id ?? this.id,
      username: username ?? this.username,
      name: name ?? this.name,
      email: email ?? this.email,
      avatar: avatar ?? this.avatar,
      phone: phone ?? this.phone,
      department: department ?? this.department,
      role: role ?? this.role,
      token: token ?? this.token,
      lastLoginTime: lastLoginTime ?? this.lastLoginTime,
    );
  }
}
