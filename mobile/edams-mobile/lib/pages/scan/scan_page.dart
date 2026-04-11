import 'package:flutter/material.dart';
import '../../config/theme.dart';
import '../asset/asset_detail_page.dart';

class ScanPage extends StatefulWidget {
  const ScanPage({super.key});

  @override
  State<ScanPage> createState() => _ScanPageState();
}

class _ScanPageState extends State<ScanPage> {
  bool _isScanning = true;
  String? _scannedResult;
  bool _hasPermission = true;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('扫码查询'),
        actions: [
          IconButton(
            icon: Icon(_isScanning ? Icons.flash_off : Icons.flash_on),
            onPressed: () {
              // Toggle flash
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Camera Preview Area
          Expanded(
            child: _hasPermission
                ? Stack(
                    children: [
                      // Camera preview would go here
                      // Using a placeholder for mobile_scanner integration
                      Container(
                        color: Colors.black,
                        child: Center(
                          child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(
                                Icons.qr_code_scanner,
                                size: 100,
                                color: Colors.white.withOpacity(0.3),
                              ),
                              const SizedBox(height: 16),
                              Text(
                                _isScanning
                                    ? '将二维码放入框内扫描'
                                    : '扫描已暂停',
                                style: TextStyle(
                                  color: Colors.white.withOpacity(0.7),
                                  fontSize: 16,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                      // Scan Frame
                      Center(
                        child: Container(
                          width: 250,
                          height: 250,
                          decoration: BoxDecoration(
                            border: Border.all(
                              color: AppTheme.primaryColor,
                              width: 3,
                            ),
                            borderRadius: BorderRadius.circular(16),
                          ),
                          child: Stack(
                            children: [
                              // Corner decorations
                              Positioned(
                                top: 0,
                                left: 0,
                                child: _buildCorner(true, true),
                              ),
                              Positioned(
                                top: 0,
                                right: 0,
                                child: _buildCorner(true, false),
                              ),
                              Positioned(
                                bottom: 0,
                                left: 0,
                                child: _buildCorner(false, true),
                              ),
                              Positioned(
                                bottom: 0,
                                right: 0,
                                child: _buildCorner(false, false),
                              ),
                              // Scanning line animation
                              if (_isScanning)
                                Positioned(
                                  top: 0,
                                  left: 0,
                                  right: 0,
                                  child: Container(
                                    height: 2,
                                    color: AppTheme.primaryColor,
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                      // Result overlay
                      if (_scannedResult != null)
                        Positioned(
                          bottom: 0,
                          left: 0,
                          right: 0,
                          child: Container(
                            padding: const EdgeInsets.all(16),
                            color: Colors.black.withOpacity(0.8),
                            child: Column(
                              children: [
                                Text(
                                  '扫描结果: $_scannedResult',
                                  style: const TextStyle(color: Colors.white),
                                ),
                                const SizedBox(height: 12),
                                Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceEvenly,
                                  children: [
                                    ElevatedButton(
                                      onPressed: () {
                                        setState(() {
                                          _scannedResult = null;
                                          _isScanning = true;
                                        });
                                      },
                                      child: const Text('继续扫描'),
                                    ),
                                    ElevatedButton(
                                      onPressed: () => _openAssetDetail(),
                                      child: const Text('查看详情'),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          ),
                        ),
                    ],
                  )
                : _buildPermissionDenied(),
          ),

          // Bottom Actions
          Container(
            padding: const EdgeInsets.all(24),
            color: Colors.white,
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    _buildScanAction(
                      icon: Icons.qr_code,
                      label: '扫码',
                      isSelected: true,
                      onTap: () {},
                    ),
                    _buildScanAction(
                      icon: Icons.keyboard,
                      label: '手动输入',
                      isSelected: false,
                      onTap: () => _showManualInput(),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                if (_isScanning)
                  const Text(
                    '支持扫描资产二维码快速查询',
                    style: TextStyle(color: Colors.grey),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCorner(bool isTop, bool isLeft) {
    return Container(
      width: 30,
      height: 30,
      decoration: BoxDecoration(
        border: Border(
          top: isTop
              ? const BorderSide(color: AppTheme.primaryColor, width: 4)
              : BorderSide.none,
          bottom: !isTop
              ? const BorderSide(color: AppTheme.primaryColor, width: 4)
              : BorderSide.none,
          left: isLeft
              ? const BorderSide(color: AppTheme.primaryColor, width: 4)
              : BorderSide.none,
          right: !isLeft
              ? const BorderSide(color: AppTheme.primaryColor, width: 4)
              : BorderSide.none,
        ),
      ),
    );
  }

  Widget _buildPermissionDenied() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.camera_alt,
            size: 64,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          const Text(
            '相机权限被拒绝',
            style: TextStyle(fontSize: 18),
          ),
          const SizedBox(height: 8),
          Text(
            '请在设置中开启相机权限以使用扫码功能',
            style: TextStyle(color: Colors.grey[600]),
          ),
          const SizedBox(height: 24),
          ElevatedButton(
            onPressed: () {
              // Open app settings
            },
            child: const Text('打开设置'),
          ),
        ],
      ),
    );
  }

  Widget _buildScanAction({
    required IconData icon,
    required String label,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        decoration: BoxDecoration(
          color: isSelected
              ? AppTheme.primaryColor.withOpacity(0.1)
              : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Icon(
              icon,
              color: isSelected ? AppTheme.primaryColor : Colors.grey,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                color: isSelected ? AppTheme.primaryColor : Colors.grey,
                fontSize: 12,
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showManualInput() {
    final controller = TextEditingController();

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('手动输入'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(
            labelText: '资产编码',
            hintText: '请输入资产编码',
          ),
          autofocus: true,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              if (controller.text.isNotEmpty) {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => AssetDetailPage(assetId: controller.text),
                  ),
                );
              }
            },
            child: const Text('查询'),
          ),
        ],
      ),
    );
  }

  void _openAssetDetail() {
    if (_scannedResult != null) {
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (_) => AssetDetailPage(assetId: _scannedResult!),
        ),
      );
    }
  }

  // This method would be called by the mobile_scanner package
  void onDetect(String code) {
    setState(() {
      _isScanning = false;
      _scannedResult = code;
    });
  }
}
