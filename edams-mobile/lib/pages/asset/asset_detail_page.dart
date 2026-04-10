import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../config/theme.dart';
import '../../models/data_asset.dart';
import '../../services/asset_service.dart';
import '../datamap/lineage_page.dart';
import '../quality/quality_detail_page.dart';

class AssetDetailPage extends StatefulWidget {
  final String assetId;

  const AssetDetailPage({super.key, required this.assetId});

  @override
  State<AssetDetailPage> createState() => _AssetDetailPageState();
}

class _AssetDetailPageState extends State<AssetDetailPage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  DataAsset? _asset;
  bool _isLoading = true;
  bool _isFavorite = false;
  bool _isSubscribed = false;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 3, vsync: this);
    _loadAssetDetail();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadAssetDetail() async {
    try {
      final asset = await AssetService.getAssetDetail(widget.assetId);
      if (mounted) {
        setState(() {
          _asset = asset;
          _isFavorite = asset.isFavorite ?? false;
          _isSubscribed = asset.isSubscribed ?? false;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _asset = DataAsset(
            id: widget.assetId,
            name: '测试数据资产',
            code: 'TEST_ASSET_001',
            description: '这是一个测试数据资产的详细信息，用于演示移动端页面布局和功能展示。',
            assetType: 'table',
            status: 'active',
            owner: '张三',
            department: '技术部',
            recordCount: 100000,
            storageSize: '500MB',
            createTime: DateTime.now().subtract(const Duration(days: 365)),
            updateTime: DateTime.now().subtract(const Duration(days: 7)),
            tags: ['核心数据', '用户数据', '敏感'],
            technicalProperties: {
              'database': 'MySQL',
              'schema': 'public',
              'tableName': 'user_info',
              'columnCount': 15,
              'indexes': ['PRIMARY', 'INDEX_user_name'],
              'partitions': 12,
            },
          );
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('资产详情'),
        actions: [
          IconButton(
            icon: Icon(
              _isFavorite ? Icons.star : Icons.star_border,
              color: _isFavorite ? Colors.amber : null,
            ),
            onPressed: _toggleFavorite,
          ),
          IconButton(
            icon: Icon(
              _isSubscribed ? Icons.notifications_active : Icons.notifications_none,
            ),
            onPressed: _toggleSubscription,
          ),
          PopupMenuButton<String>(
            onSelected: (value) {},
            itemBuilder: (context) => [
              const PopupMenuItem(value: 'share', child: Text('分享')),
              const PopupMenuItem(value: 'export', child: Text('导出')),
            ],
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: '基本信息'),
            Tab(text: '技术属性'),
            Tab(text: '业务属性'),
          ],
        ),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _asset == null
              ? const Center(child: Text('加载失败'))
              : Column(
                  children: [
                    // Header Card
                    _buildHeaderCard(),
                    // Tab Content
                    Expanded(
                      child: TabBarView(
                        controller: _tabController,
                        children: [
                          _buildBasicInfoTab(),
                          _buildTechnicalTab(),
                          _buildBusinessTab(),
                        ],
                      ),
                    ),
                  ],
                ),
    );
  }

  Widget _buildHeaderCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      color: Colors.white,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppTheme.primaryColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  _getAssetIcon(_asset!.assetType),
                  color: AppTheme.primaryColor,
                  size: 32,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _asset!.name,
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _asset!.code,
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ),
              _buildStatusBadge(_asset!.status),
            ],
          ),
          if (_asset!.description != null) ...[
            const SizedBox(height: 12),
            Text(
              _asset!.description!,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[700],
              ),
            ),
          ],
          if (_asset!.tags != null && _asset!.tags!.isNotEmpty) ...[
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _asset!.tags!
                  .map((tag) => Chip(
                        label: Text(tag),
                        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                        visualDensity: VisualDensity.compact,
                      ))
                  .toList(),
            ),
          ],
          const SizedBox(height: 16),
          // Quick Actions
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildQuickAction(
                Icons.account_tree,
                '血缘关系',
                () => Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => LineagePage(assetId: _asset!.id),
                  ),
                ),
              ),
              _buildQuickAction(
                Icons.analytics,
                '质量分析',
                () => Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => QualityDetailPage(assetId: _asset!.id),
                  ),
                ),
              ),
              _buildQuickAction(
                Icons.history,
                '变更记录',
                () {},
              ),
              _buildQuickAction(
                Icons.download,
                '申请权限',
                () {},
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildQuickAction(IconData icon, String label, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          children: [
            Icon(icon, color: AppTheme.primaryColor),
            const SizedBox(height: 4),
            Text(label, style: const TextStyle(fontSize: 12)),
          ],
        ),
      ),
    );
  }

  Widget _buildBasicInfoTab() {
    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _buildInfoCard('基本信息', [
          _buildInfoRow('资产类型', _asset!.assetTypeName),
          _buildInfoRow('所属部门', _asset!.department ?? '-'),
          _buildInfoRow('负责人', _asset!.owner ?? '-'),
          _buildInfoRow('记录数量', _asset!.recordCount?.toString() ?? '-'),
          _buildInfoRow('存储大小', _asset!.storageSize ?? '-'),
        ]),
        const SizedBox(height: 16),
        _buildInfoCard('时间信息', [
          _buildInfoRow('创建时间', _asset!.createTime != null
              ? dateFormat.format(_asset!.createTime!)
              : '-'),
          _buildInfoRow('更新时间', _asset!.updateTime != null
              ? dateFormat.format(_asset!.updateTime!)
              : '-'),
        ]),
      ],
    );
  }

  Widget _buildTechnicalTab() {
    final props = _asset!.technicalProperties ?? {};
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _buildInfoCard('数据库信息', [
          _buildInfoRow('数据库类型', props['database']?.toString() ?? '-'),
          _buildInfoRow('Schema', props['schema']?.toString() ?? '-'),
          _buildInfoRow('表名', props['tableName']?.toString() ?? '-'),
        ]),
        const SizedBox(height: 16),
        _buildInfoCard('结构信息', [
          _buildInfoRow('字段数量', props['columnCount']?.toString() ?? '-'),
          _buildInfoRow('索引', props['indexes']?.toString() ?? '-'),
          _buildInfoRow('分区数', props['partitions']?.toString() ?? '-'),
        ]),
      ],
    );
  }

  Widget _buildBusinessTab() {
    final props = _asset!.businessProperties ?? {};
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _buildInfoCard('业务信息', [
          _buildInfoRow('业务分类', props['businessCategory']?.toString() ?? '-'),
          _buildInfoRow('数据域', props['dataDomain']?.toString() ?? '-'),
          _buildInfoRow('敏感级别', props['sensitivityLevel']?.toString() ?? '-'),
          _buildInfoRow('更新频率', props['updateFrequency']?.toString() ?? '-'),
        ]),
        const SizedBox(height: 16),
        _buildInfoCard('数据说明', [
          Padding(
            padding: const EdgeInsets.all(12),
            child: Text(
              _asset!.description ?? '暂无数据说明',
              style: TextStyle(
                color: Colors.grey[700],
                height: 1.5,
              ),
            ),
          ),
        ]),
      ],
    );
  }

  Widget _buildInfoCard(String title, List<Widget> children) {
    return Card(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              title,
              style: const TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
          const Divider(height: 1),
          ...children,
        ],
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: TextStyle(color: Colors.grey[600]),
          ),
          Text(
            value,
            style: const TextStyle(fontWeight: FontWeight.w500),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusBadge(String? status) {
    Color color;
    String text;

    switch (status) {
      case 'active':
        color = AppTheme.secondaryColor;
        text = '活跃';
        break;
      case 'inactive':
        color = Colors.orange;
        text = '停用';
        break;
      default:
        color = Colors.grey;
        text = '归档';
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Text(
        text,
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  IconData _getAssetIcon(String assetType) {
    switch (assetType) {
      case 'table':
        return Icons.table_chart;
      case 'file':
        return Icons.insert_drive_file;
      case 'api':
        return Icons.api;
      case 'stream':
        return Icons.stream;
      default:
        return Icons.inventory_2;
    }
  }

  Future<void> _toggleFavorite() async {
    final success = await AssetService.toggleFavorite(widget.assetId);
    if (success) {
      setState(() => _isFavorite = !_isFavorite);
    }
  }

  Future<void> _toggleSubscription() async {
    final success = await AssetService.toggleSubscription(widget.assetId);
    if (success) {
      setState(() => _isSubscribed = !_isSubscribed);
    }
  }
}
