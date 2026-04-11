import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:intl/intl.dart';
import '../../config/theme.dart';
import '../../models/data_quality.dart';
import '../../services/quality_service.dart';

class QualityDetailPage extends StatefulWidget {
  final String assetId;

  const QualityDetailPage({super.key, required this.assetId});

  @override
  State<QualityDetailPage> createState() => _QualityDetailPageState();
}

class _QualityDetailPageState extends State<QualityDetailPage> {
  DataQuality? _quality;
  List<QualityIssue> _issues = [];
  List<QualityTrend> _trends = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final quality = await QualityService.getAssetQuality(widget.assetId);
      final issues = await QualityService.getQualityIssues(
        assetId: widget.assetId,
        pageSize: 20,
      );
      final trends = await QualityService.getQualityTrends(
        assetId: widget.assetId,
        days: 30,
      );
      if (mounted) {
        setState(() {
          _quality = quality;
          _issues = issues;
          _trends = trends;
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
      _quality = DataQuality(
        id: 'q1',
        assetId: widget.assetId,
        assetName: '测试资产',
        totalScore: 85.5,
        completenessScore: 92.0,
        accuracyScore: 88.0,
        timelinessScore: 80.0,
        consistencyScore: 85.0,
        uniquenessScore: 82.0,
        totalIssueCount: 12,
        criticalIssueCount: 1,
        majorIssueCount: 3,
        minorIssueCount: 8,
        lastCheckTime: DateTime.now(),
      );
      _trends = List.generate(
        30,
        (index) => QualityTrend(
          date: DateTime.now().subtract(Duration(days: 29 - index)),
          score: 80 + (index % 10).toDouble(),
        ),
      );
      _issues = List.generate(
        10,
        (index) => QualityIssue(
          id: 'issue_$index',
          assetId: widget.assetId,
          ruleName: '质量规则${index + 1}',
          description: '检测到数据质量问题',
          level: ['critical', 'major', 'minor'][index % 3],
          errorCount: (index + 1) * 5,
          detectTime: DateTime.now().subtract(Duration(hours: index)),
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('质量详情'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _quality == null
              ? const Center(child: Text('加载失败'))
              : SingleChildScrollView(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      _buildScoreCard(),
                      const SizedBox(height: 16),
                      _buildDimensionChart(),
                      const SizedBox(height: 16),
                      _buildTrendChart(),
                      const SizedBox(height: 16),
                      _buildIssueList(),
                    ],
                  ),
                ),
    );
  }

  Widget _buildScoreCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Row(
          children: [
            SizedBox(
              width: 80,
              height: 80,
              child: Stack(
                children: [
                  CircularProgressIndicator(
                    value: _quality!.totalScore / 100,
                    strokeWidth: 8,
                    backgroundColor: Colors.grey[200],
                    valueColor: AlwaysStoppedAnimation(
                      _getScoreColor(_quality!.totalScore),
                    ),
                  ),
                  Center(
                    child: Text(
                      _quality!.totalScore.toStringAsFixed(1),
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: _getScoreColor(_quality!.totalScore),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _quality!.scoreLevel,
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: _getScoreColor(_quality!.totalScore),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '最近检测: ${_quality!.lastCheckTime != null ? DateFormat('MM-dd HH:mm').format(_quality!.lastCheckTime!) : '-'}',
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDimensionChart() {
    final dimensions = [
      {'name': '完整性', 'score': _quality!.completenessScore, 'color': Colors.blue},
      {'name': '准确性', 'score': _quality!.accuracyScore, 'color': Colors.green},
      {'name': '时效性', 'score': _quality!.timelinessScore, 'color': Colors.orange},
      {'name': '一致性', 'score': _quality!.consistencyScore, 'color': Colors.purple},
      {'name': '唯一性', 'score': _quality!.uniquenessScore, 'color': Colors.teal},
    ];

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '质量维度',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 200,
              child: RadarChart(
                RadarChartData(
                  dataSets: [
                    RadarDataSet(
                      dataEntries: dimensions
                          .map((d) => RadarEntry(value: d['score'] as double))
                          .toList(),
                      fillColor: AppTheme.primaryColor.withOpacity(0.2),
                      borderColor: AppTheme.primaryColor,
                      borderWidth: 2,
                    ),
                  ],
                  radarBackgroundColor: Colors.transparent,
                  borderData: FlBorderData(show: false),
                  radarBorderData: const BorderSide(color: Colors.grey, width: 1),
                  titlePositionPercentageOffset: 0.2,
                  titleTextStyle: const TextStyle(fontSize: 12),
                  getTitle: (index, angle) {
                    return RadarChartTitle(
                      text: dimensions[index]['name'] as String,
                    );
                  },
                  tickCount: 5,
                  tickBorderData: const BorderSide(color: Colors.grey, width: 0.5),
                  gridBorderData: const BorderSide(color: Colors.grey, width: 0.5),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTrendChart() {
    if (_trends.isEmpty) return const SizedBox.shrink();

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              '质量趋势（30天）',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 150,
              child: LineChart(
                LineChartData(
                  gridData: FlGridData(
                    show: true,
                    drawVerticalLine: false,
                    horizontalInterval: 20,
                    getDrawingHorizontalLine: (value) => FlLine(
                      color: Colors.grey[300],
                      strokeWidth: 1,
                    ),
                  ),
                  titlesData: FlTitlesData(
                    leftTitles: AxisTitles(
                      sideTitles: SideTitles(
                        showTitles: true,
                        reservedSize: 30,
                        getTitlesWidget: (value, meta) {
                          return Text(
                            value.toInt().toString(),
                            style: const TextStyle(fontSize: 10),
                          );
                        },
                      ),
                    ),
                    bottomTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    topTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                    rightTitles: const AxisTitles(
                      sideTitles: SideTitles(showTitles: false),
                    ),
                  ),
                  borderData: FlBorderData(show: false),
                  minY: 0,
                  maxY: 100,
                  lineBarsData: [
                    LineChartBarData(
                      spots: _trends
                          .asMap()
                          .entries
                          .map((e) => FlSpot(e.key.toDouble(), e.value.score))
                          .toList(),
                      isCurved: true,
                      color: AppTheme.primaryColor,
                      barWidth: 2,
                      dotData: const FlDotData(show: false),
                      belowBarData: BarAreaData(
                        show: true,
                        color: AppTheme.primaryColor.withOpacity(0.1),
                      ),
                    ),
                  ],
                ),
              ),
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
        Text(
          '质量问题（${_issues.length}）',
          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 12),
        if (_issues.isEmpty)
          Card(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Center(
                child: Column(
                  children: [
                    Icon(Icons.check_circle, size: 48, color: Colors.green[300]),
                    const SizedBox(height: 8),
                    const Text('暂无质量问题'),
                  ],
                ),
              ),
            ),
          )
        else
          ...(_issues.map((issue) => Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  leading: Icon(
                    Icons.warning,
                    color: _getLevelColor(issue.level),
                  ),
                  title: Text(issue.ruleName),
                  subtitle: Text(
                    '${issue.levelName} | ${issue.errorCount}条异常',
                    style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                  ),
                ),
              ))),
      ],
    );
  }

  Color _getScoreColor(double score) {
    if (score >= 90) return Colors.green;
    if (score >= 80) return Colors.blue;
    if (score >= 60) return Colors.orange;
    return Colors.red;
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
