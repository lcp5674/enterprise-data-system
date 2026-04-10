import 'package:flutter/material.dart';
import 'package:pull_to_refresh_flutter3/pull_to_refresh_flutter3.dart';
import '../../config/theme.dart';
import '../../models/data_asset.dart';
import '../../services/asset_service.dart';
import 'asset_detail_page.dart';

class AssetListPage extends StatefulWidget {
  const AssetListPage({super.key});

  @override
  State<AssetListPage> createState() => _AssetListPageState();
}

class _AssetListPageState extends State<AssetListPage> {
  final _searchController = TextEditingController();
  final _refreshController = RefreshController(initialRefresh: false);

  List<DataAsset> _assets = [];
  bool _isLoading = false;
  int _currentPage = 1;
  String? _selectedAssetType;
  bool _hasMore = true;

  final List<Map<String, dynamic>> _assetTypes = [
    {'value': null, 'label': '全部'},
    {'value': 'table', 'label': '数据表'},
    {'value': 'file', 'label': '文件'},
    {'value': 'api', 'label': 'API接口'},
    {'value': 'stream', 'label': '数据流'},
  ];

  @override
  void initState() {
    super.initState();
    _loadAssets();
  }

  @override
  void dispose() {
    _searchController.dispose();
    _refreshController.dispose();
    super.dispose();
  }

  Future<void> _loadAssets({bool refresh = false}) async {
    if (_isLoading) return;

    setState(() {
      _isLoading = true;
      if (refresh) {
        _currentPage = 1;
        _assets = [];
      }
    });

    try {
      final assets = await AssetService.searchAssets(
        keyword: _searchController.text.isNotEmpty
            ? _searchController.text
            : null,
        assetType: _selectedAssetType,
        page: _currentPage,
        pageSize: 20,
      );

      if (!mounted) return;

      setState(() {
        if (refresh) {
          _assets = assets;
        } else {
          _assets.addAll(assets);
        }
        _hasMore = assets.length >= 20;
        _isLoading = false;
      });

      if (refresh) {
        _refreshController.refreshCompleted();
      }
    } catch (e) {
      if (!mounted) return;
      setState(() => _isLoading = false);
      _refreshController.refreshFailed();
      _showMockData();
    }
  }

  void _showMockData() {
    setState(() {
      _assets = List.generate(
        10,
        (index) => DataAsset(
          id: 'asset_$index',
          name: '数据资产${index + 1}',
          code: 'ASSET_CODE_${index + 1}',
          description: '这是一个测试数据资产的描述信息',
          assetType: ['table', 'file', 'api', 'stream'][index % 4],
          status: 'active',
          owner: '张三',
          department: '技术部',
          recordCount: 10000 * (index + 1),
          storageSize: '${(index + 1) * 100}MB',
          createTime: DateTime.now().subtract(Duration(days: index * 30)),
          updateTime: DateTime.now().subtract(Duration(days: index)),
        ),
      );
      _hasMore = false;
      _isLoading = false;
    });
  }

  void _loadMore() {
    if (_hasMore && !_isLoading) {
      _currentPage++;
      _loadAssets();
    } else {
      _refreshController.loadNoData();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('资产列表'),
      ),
      body: Column(
        children: [
          // Search Bar
          Container(
            padding: const EdgeInsets.all(16),
            color: Colors.white,
            child: TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: '搜索资产名称或编码',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          _loadAssets(refresh: true);
                        },
                      )
                    : null,
              ),
              onSubmitted: (_) => _loadAssets(refresh: true),
            ),
          ),

          // Filter Chips
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            height: 50,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              itemCount: _assetTypes.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final type = _assetTypes[index];
                final isSelected = _selectedAssetType == type['value'];
                return FilterChip(
                  label: Text(type['label']),
                  selected: isSelected,
                  onSelected: (selected) {
                    setState(() {
                      _selectedAssetType = selected ? type['value'] : null;
                    });
                    _loadAssets(refresh: true);
                  },
                  selectedColor: AppTheme.primaryColor.withOpacity(0.2),
                  checkmarkColor: AppTheme.primaryColor,
                );
              },
            ),
          ),

          // Asset List
          Expanded(
            child: SmartRefresher(
              controller: _refreshController,
              enablePullDown: true,
              enablePullUp: _hasMore,
              onRefresh: () => _loadAssets(refresh: true),
              onLoading: _loadMore,
              child: _assets.isEmpty && !_isLoading
                  ? _buildEmptyState()
                  : ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: _assets.length,
                      itemBuilder: (context, index) {
                        return _buildAssetCard(_assets[index]);
                      },
                    ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.search_off,
            size: 64,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          Text(
            '未找到相关资产',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAssetCard(DataAsset asset) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => AssetDetailPage(assetId: asset.id),
            ),
          );
        },
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: AppTheme.primaryColor.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(
                      _getAssetIcon(asset.assetType),
                      color: AppTheme.primaryColor,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          asset.name,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          asset.code,
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                          ),
                        ),
                      ],
                    ),
                  ),
                  _buildStatusBadge(asset.status),
                ],
              ),
              if (asset.description != null) ...[
                const SizedBox(height: 12),
                Text(
                  asset.description!,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.grey[600],
                  ),
                ),
              ],
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: [
                  if (asset.owner != null)
                    _buildTag(Icons.person, asset.owner!),
                  if (asset.department != null)
                    _buildTag(Icons.business, asset.department!),
                  if (asset.recordCount != null)
                    _buildTag(Icons.format_list_numbered, '${asset.recordCount}条'),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusBadge(String? status) {
    Color color;
    switch (status) {
      case 'active':
        color = AppTheme.secondaryColor;
        break;
      case 'inactive':
        color = Colors.orange;
        break;
      case 'archived':
        color = Colors.grey;
        break;
      default:
        color = Colors.grey;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        asset.statusName,
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Widget _buildTag(IconData icon, String text) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(4),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: Colors.grey[600]),
          const SizedBox(width: 4),
          Text(
            text,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[600],
            ),
          ),
        ],
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
