/// 数据质量模型
class DataQuality {
  final String id;
  final String assetId;
  final String assetName;
  final double totalScore; // 0-100
  final double completenessScore; // 完整性
  final double accuracyScore; // 准确性
  final double timelinessScore; // 时效性
  final double consistencyScore; // 一致性
  final double uniquenessScore; // 唯一性
  final int totalIssueCount;
  final int criticalIssueCount;
  final int majorIssueCount;
  final int minorIssueCount;
  final DateTime? lastCheckTime;
  final List<QualityIssue>? issues;
  final List<QualityTrend>? trends;

  DataQuality({
    required this.id,
    required this.assetId,
    required this.assetName,
    required this.totalScore,
    this.completenessScore = 0,
    this.accuracyScore = 0,
    this.timelinessScore = 0,
    this.consistencyScore = 0,
    this.uniquenessScore = 0,
    this.totalIssueCount = 0,
    this.criticalIssueCount = 0,
    this.majorIssueCount = 0,
    this.minorIssueCount = 0,
    this.lastCheckTime,
    this.issues,
    this.trends,
  });

  factory DataQuality.fromJson(Map<String, dynamic> json) {
    return DataQuality(
      id: json['id']?.toString() ?? '',
      assetId: json['assetId'] ?? '',
      assetName: json['assetName'] ?? '',
      totalScore: (json['totalScore'] ?? 0).toDouble(),
      completenessScore: (json['completenessScore'] ?? 0).toDouble(),
      accuracyScore: (json['accuracyScore'] ?? 0).toDouble(),
      timelinessScore: (json['timelinessScore'] ?? 0).toDouble(),
      consistencyScore: (json['consistencyScore'] ?? 0).toDouble(),
      uniquenessScore: (json['uniquenessScore'] ?? 0).toDouble(),
      totalIssueCount: json['totalIssueCount'] ?? 0,
      criticalIssueCount: json['criticalIssueCount'] ?? 0,
      majorIssueCount: json['majorIssueCount'] ?? 0,
      minorIssueCount: json['minorIssueCount'] ?? 0,
      lastCheckTime: json['lastCheckTime'] != null
          ? DateTime.parse(json['lastCheckTime'])
          : null,
      issues: json['issues'] != null
          ? (json['issues'] as List)
              .map((e) => QualityIssue.fromJson(e))
              .toList()
          : null,
      trends: json['trends'] != null
          ? (json['trends'] as List)
              .map((e) => QualityTrend.fromJson(e))
              .toList()
          : null,
    );
  }

  String get scoreLevel {
    if (totalScore >= 90) return '优秀';
    if (totalScore >= 80) return '良好';
    if (totalScore >= 60) return '一般';
    return '较差';
  }
}

/// 质量问题
class QualityIssue {
  final String id;
  final String assetId;
  final String ruleName;
  final String? description;
  final String level; // critical, major, minor
  final String? category;
  final int errorCount;
  final DateTime? detectTime;
  final String? status;

  QualityIssue({
    required this.id,
    required this.assetId,
    required this.ruleName,
    this.description,
    required this.level,
    this.category,
    this.errorCount = 0,
    this.detectTime,
    this.status,
  });

  factory QualityIssue.fromJson(Map<String, dynamic> json) {
    return QualityIssue(
      id: json['id']?.toString() ?? '',
      assetId: json['assetId'] ?? '',
      ruleName: json['ruleName'] ?? '',
      description: json['description'],
      level: json['level'] ?? 'minor',
      category: json['category'],
      errorCount: json['errorCount'] ?? 0,
      detectTime: json['detectTime'] != null
          ? DateTime.parse(json['detectTime'])
          : null,
      status: json['status'],
    );
  }

  String get levelName {
    switch (level) {
      case 'critical':
        return '严重';
      case 'major':
        return '重要';
      case 'minor':
        return '一般';
      default:
        return level;
    }
  }
}

/// 质量趋势
class QualityTrend {
  final DateTime date;
  final double score;

  QualityTrend({
    required this.date,
    required this.score,
  });

  factory QualityTrend.fromJson(Map<String, dynamic> json) {
    return QualityTrend(
      date: DateTime.parse(json['date']),
      score: (json['score'] ?? 0).toDouble(),
    );
  }
}
