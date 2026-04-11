import 'package:flutter/material.dart';
import '../../config/theme.dart';
import '../../models/data_catalog.dart';
import '../../models/data_asset.dart';
import '../../services/catalog_service.dart';
import '../asset/asset_list_page.dart';

class CatalogPage extends StatefulWidget {
  const CatalogPage({super.key});

  @override
  State<CatalogPage> createState() => _CatalogPageState();
}

class _CatalogPageState extends State<CatalogPage> {
  List<DataCatalog> _catalogs = [];
  bool _isLoading = true;
  String? _expandedId;

  @override
  void initState() {
    super.initState();
    _loadCatalogs();
  }

  Future<void> _loadCatalogs() async {
    try {
      final catalogs = await CatalogService.getCatalogTree();
      if (mounted) {
        setState(() {
          _catalogs = catalogs;
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
      _catalogs = [
        DataCatalog(
          id: '1',
          name: '客户数据域',
          code: 'CUSTOMER',
          description: '客户相关数据',
          assetCount: 156,
          level: 0,
          children: [
            DataCatalog(
              id: '1-1',
              name: '客户基本信息',
              code: 'CUSTOMER_BASE',
              parentId: '1',
              assetCount: 45,
              level: 1,
            ),
            DataCatalog(
              id: '1-2',
              name: '客户行为数据',
              code: 'CUSTOMER_BEHAVIOR',
              parentId: '1',
              assetCount: 78,
              level: 1,
            ),
            DataCatalog(
              id: '1-3',
              name: '客户交易数据',
              code: 'CUSTOMER_TRANS',
              parentId: '1',
              assetCount: 33,
              level: 1,
            ),
          ],
        ),
        DataCatalog(
          id: '2',
          name: '产品数据域',
          code: 'PRODUCT',
          description: '产品相关数据',
          assetCount: 89,
          level: 0,
        ),
        DataCatalog(
          id: '3',
          name: '交易数据域',
          code: 'TRANSACTION',
          description: '交易相关数据',
          assetCount: 234,
          level: 0,
        ),
        DataCatalog(
          id: '4',
          name: '运营数据域',
          code: 'OPERATION',
          description: '运营相关数据',
          assetCount: 167,
          level: 0,
        ),
        DataCatalog(
          id: '5',
          name: '财务数据域',
          code: 'FINANCE',
          description: '财务相关数据',
          assetCount: 98,
          level: 0,
        ),
      ];
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('数据目录'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {},
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadCatalogs,
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: _catalogs.length,
                itemBuilder: (context, index) {
                  return _buildCatalogItem(_catalogs[index], 0);
                },
              ),
            ),
    );
  }

  Widget _buildCatalogItem(DataCatalog catalog, int depth) {
    final hasChildren = catalog.hasChildren;
    final isExpanded = _expandedId == catalog.id;
    final indent = depth * 20.0;

    return Column(
      children: [
        Container(
          margin: EdgeInsets.only(left: indent, bottom: 8),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            boxShadow: [
              BoxShadow(
                color: Colors.grey.withOpacity(0.1),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: InkWell(
            onTap: () {
              if (hasChildren) {
                setState(() {
                  _expandedId = isExpanded ? null : catalog.id;
                });
              } else {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => CatalogAssetsPage(catalog: catalog),
                  ),
                );
              }
            },
            borderRadius: BorderRadius.circular(12),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: _getCatalogColor(catalog.code).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(
                      Icons.folder,
                      color: _getCatalogColor(catalog.code),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          catalog.name,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          '${catalog.code} | ${catalog.assetCount}个资产',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (hasChildren)
                    Icon(
                      isExpanded ? Icons.expand_less : Icons.expand_more,
                      color: Colors.grey,
                    )
                  else
                    const Icon(
                      Icons.chevron_right,
                      color: Colors.grey,
                    ),
                ],
              ),
            ),
          ),
        ),
        if (hasChildren && isExpanded)
          ...catalog.children!.map((child) => _buildCatalogItem(child, depth + 1)),
      ],
    );
  }

  Color _getCatalogColor(String code) {
    final colors = [
      AppTheme.primaryColor,
      Colors.orange,
      Colors.purple,
      Colors.teal,
      Colors.pink,
    ];
    return colors[code.hashCode % colors.length];
  }
}

/// 目录资产列表页面
class CatalogAssetsPage extends StatefulWidget {
  final DataCatalog catalog;

  const CatalogAssetsPage({super.key, required this.catalog});

  @override
  State<CatalogAssetsPage> createState() => _CatalogAssetsPageState();
}

class _CatalogAssetsPageState extends State<CatalogAssetsPage> {
  List<DataAsset> _assets = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadAssets();
  }

  Future<void> _loadAssets() async {
    try {
      final assets = await CatalogService.getCatalogAssets(widget.catalog.id);
      if (mounted) {
        setState(() {
          _assets = assets;
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
      _assets = List.generate(
        10,
        (index) => DataAsset(
          id: 'asset_$index',
          name: '${widget.catalog.name}_资产${index + 1}',
          code: '${widget.catalog.code}_${index + 1}',
          assetType: 'table',
          status: 'active',
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.catalog.name),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _assets.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.folder_open, size: 64, color: Colors.grey[400]),
                      const SizedBox(height: 16),
                      Text(
                        '该目录下暂无资产',
                        style: TextStyle(color: Colors.grey[600]),
                      ),
                    ],
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _assets.length,
                  itemBuilder: (context, index) {
                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        leading: Icon(
                          Icons.table_chart,
                          color: AppTheme.primaryColor,
                        ),
                        title: Text(_assets[index].name),
                        subtitle: Text(_assets[index].code),
                        trailing: const Icon(Icons.chevron_right),
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => AssetListPage(),
                            ),
                          );
                        },
                      ),
                    );
                  },
                ),
    );
  }
}
