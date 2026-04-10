import 'package:flutter/material.dart';
import '../../config/theme.dart';
import '../../models/data_map.dart';
import '../../services/datamap_service.dart';
import '../asset/asset_detail_page.dart';

class LineagePage extends StatefulWidget {
  final String assetId;
  final String? assetName;

  const LineagePage({super.key, required this.assetId, this.assetName});

  @override
  State<LineagePage> createState() => _LineagePageState();
}

class _LineagePageState extends State<LineagePage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<DataLineageNode> _upstreamAssets = [];
  List<DataLineageNode> _downstreamAssets = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _loadLineageData();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadLineageData() async {
    try {
      final upstream = await DataMapService.getUpstreamAssets(widget.assetId);
      final downstream = await DataMapService.getDownstreamAssets(widget.assetId);
      if (mounted) {
        setState(() {
          _upstreamAssets = upstream;
          _downstreamAssets = downstream;
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
      _upstreamAssets = [
        DataLineageNode(
          id: 'u1',
          assetId: 'asset_1',
          assetName: '原始用户数据表',
          assetType: 'table',
        ),
        DataLineageNode(
          id: 'u2',
          assetId: 'asset_2',
          assetName: '用户清洗表',
          assetType: 'table',
        ),
      ];
      _downstreamAssets = [
        DataLineageNode(
          id: 'd1',
          assetId: 'asset_3',
          assetName: '用户画像表',
          assetType: 'table',
        ),
        DataLineageNode(
          id: 'd2',
          assetId: 'asset_4',
          assetName: '用户分析报表',
          assetType: 'table',
        ),
        DataLineageNode(
          id: 'd3',
          assetId: 'asset_5',
          assetName: '营销数据API',
          assetType: 'api',
        ),
      ];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.assetName ?? '血缘关系'),
      ),
      body: Column(
        children: [
          // Current Asset
          Container(
            padding: const EdgeInsets.all(16),
            color: Colors.white,
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppTheme.primaryColor,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(
                    Icons.storage,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        '当前资产',
                        style: TextStyle(
                          fontSize: 12,
                          color: Colors.grey,
                        ),
                      ),
                      Text(
                        widget.assetName ?? widget.assetId,
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          // Tabs
          TabBar(
            controller: _tabController,
            tabs: [
              Tab(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.arrow_upward, size: 18),
                    const SizedBox(width: 4),
                    Text('上游(${_upstreamAssets.length})'),
                  ],
                ),
              ),
              Tab(
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.arrow_downward, size: 18),
                    const SizedBox(width: 4),
                    Text('下游(${_downstreamAssets.length})'),
                  ],
                ),
              ),
            ],
          ),
          // Content
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator())
                : TabBarView(
                    controller: _tabController,
                    children: [
                      _buildLineageList(_upstreamAssets, true),
                      _buildLineageList(_downstreamAssets, false),
                    ],
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildLineageList(List<DataLineageNode> assets, bool isUpstream) {
    if (assets.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              isUpstream ? Icons.arrow_upward : Icons.arrow_downward,
              size: 48,
              color: Colors.grey[400],
            ),
            const SizedBox(height: 16),
            Text(
              isUpstream ? '暂无上游资产' : '暂无下游资产',
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: assets.length,
      itemBuilder: (context, index) {
        return _buildLineageCard(assets[index], isUpstream);
      },
    );
  }

  Widget _buildLineageCard(DataLineageNode asset, bool isUpstream) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => AssetDetailPage(assetId: asset.assetId),
            ),
          );
        },
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: (isUpstream ? Colors.green : Colors.orange)
                      .withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  _getAssetIcon(asset.assetType),
                  color: isUpstream ? Colors.green : Colors.orange,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      asset.assetName,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      asset.assetType.toUpperCase(),
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[600],
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                Icons.chevron_right,
                color: Colors.grey[400],
              ),
            ],
          ),
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
}
