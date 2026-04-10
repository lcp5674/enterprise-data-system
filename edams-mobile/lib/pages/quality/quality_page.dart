import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../config/theme.dart';
import '../../models/data_quality.dart';
import '../../services/quality_service.dart';

class QualityPage extends StatefulWidget {
  const QualityPage({super.key});

  @override
  State<QualityPage> createState() => _QualityPageState();
}

class _QualityPageState extends State<QualityPage> {
  List<QualityIssue> _issues = [];
  Map<String, dynamic>? _overview;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final overview = await QualityService.getQualityOverview();
      final issues = await QualityService.getQualityIssues(pageSize: 10);
      if (mounted) {
        setState(() {
          _overview = overview;
          _issues = issues;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() => _isLoading = false);
        _showMockData();
      }
    }
  }

  void _showMockData() {
    setState(() {
      _overview = {
        'averageScore': 87.5,
        'totalAssets': 1234,
        'excellentCount': 456,
        'goodCount': 567,
        'normalCount': 123,
        'poorCount': 88,
      };
      _issues = List.generate(
        10,
        (index) => QualityIssue(
          id: 'issue_$index',
          assetId: 'asset_$index',
          ruleName: '数据质量规则${index + 1}',
          description: '检测到数据质量问题，请及时处理',
          level: ['critical', 'major', 'minor'][index % 3],
          category: '完整性',
          errorCount: (index + 1) * 10,
          detectTime: DateTime.now().subtract(Duration(hours: index)),
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('数据质量'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadData,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _buildScoreCard(),
                    const SizedBox(height: 24),
                    _buildDistributionChart(),
                    const SizedBox(height: 24),
                    _buildIssueList(),
                  ],
                ),
              ),
            ),
    );
  }

  Widget _buildScoreCard() {
    final score = (_overview?['averageScore'] ?? 0).toDouble();
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Row(
          children: [
            SizedBox(
              width: 100,
              height: 100,
              child: Stack(
                children: [
                  CircularProgressIndicator(
                    value: score / 100,
                    strokeWidth: 10,
                    backgroundColor: Colors.grey[200],
                    valueColor: AlwaysStoppedAnimation(_getScoreColor(score)),
                  ),
                  Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          score.toStringAsFixed(1),
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: _getScoreColor(score),
                          ),
                        ),
                        Text(
                          _getScoreLevel(score),
                          style: const TextStyle(fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 24),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '综合质量评分',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '评估资产数: ${_overview?['totalAssets'] ?? 0}',
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '优秀资产: ${_overview?['excellentCount'] ?? 0}',
                    style: const TextStyle(color: Colors.green),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDistributionChart() {
    final data = [
      {'label': '优秀', 'value': _overview?['excellentCount'] ?? 0, 'color': Colors.green},
      {'label': '良好', 'value': _overview?['goodCount'] ?? 0, 'color': Colors.blue},
      {'label': '一般', 'value': _overview?['normalCount'] ?? 0, 'color': Colors.orange},
      {'label': '较差', 'value': _overview?['poorCount'] ?? 0, 'color': Colors.red},
    ];

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '质量分布',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 200,
              child: PieChart(
                PieChartData(
                  sectionsSpace: 2,
                  centerSpaceRadius: 40,
                  sections: data
                      .where((d) => d['value'] > 0)
                      .map((d) => PieChartSectionData(
                            color: d['color'] as Color,
                            value: (d['value'] as int).toDouble(),
                            title: '${d['value']}',
                            radius: 50,
                            titleStyle: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ))
                      .toList(),
                ),
              ),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 16,
              runSpacing: 8,
              children: data
                  .map((d) => Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            width: 12,
                            height: 12,
                            decoration: BoxDecoration(
                              color: d['color'] as Color,
                              shape: BoxShape.circle,
                            ),
                          ),
                          const SizedBox(width: 4),
                          Text('${d['label']}: ${d['value']}'),
                        ],
                      ))
                  .toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildIssueList() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              '质量问题',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            TextButton(
              onPressed: () {},
              child: const Text('查看全部'),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (_issues.isEmpty)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(32),
              child: Center(
                child: Column(
                  children: [
                    Icon(Icons.check_circle, size: 48, color: Colors.green[300]),
                    const SizedBox(height: 8),
                    Text(
                      '暂无质量问题',
                      style: TextStyle(color: Colors.grey[600]),
                    ),
                  ],
                ),
              ),
            ),
          )
        else
          ...(_issues.take(5).map((issue) => _buildIssueCard(issue))),
      ],
    );
  }

  Widget _buildIssueCard(QualityIssue issue) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: _getLevelColor(issue.level).withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(
            Icons.warning,
            color: _getLevelColor(issue.level),
          ),
        ),
        title: Text(issue.ruleName),
        subtitle: Text(
          '${issue.levelName} | ${issue.errorCount}条异常',
          style: TextStyle(fontSize: 12, color: Colors.grey[600]),
        ),
        trailing: const Icon(Icons.chevron_right),
        onTap: () {},
      ),
    );
  }

  Color _getScoreColor(double score) {
    if (score >= 90) return Colors.green;
    if (score >= 80) return Colors.blue;
    if (score >= 60) return Colors.orange;
    return Colors.red;
  }

  String _getScoreLevel(double score) {
    if (score >= 90) return '优秀';
    if (score >= 80) return '良好';
    if (score >= 60) return '一般';
    return '较差';
  }

  Color _getLevelColor(String level) {
    switch (level) {
      case 'critical':
        return Colors.red;
      case 'major':
        return Colors.orange;
      default:
        return Colors.blue;
    }
  }
}
