/**
 * 血缘图页面
 */

import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  Space,
  Button,
  Input,
  Select,
  Typography,
  Row,
  Col,
  Tag,
  Tooltip,
  Drawer,
  Descriptions,
  message,
  Spin,
  Dropdown,
} from 'antd';
import type { MenuProps } from 'antd';
import {
  SearchOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  FullscreenOutlined,
  ReloadOutlined,
  DownloadOutlined,
  PauseOutlined,
  PlayCircleOutlined,
  InfoCircleOutlined,
  DatabaseOutlined,
  ApiOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons';
import { useSearchParams } from '@umijs/max';
import * as lineageService from '../../services/lineage';
import type { LineageGraph, LineageNode, LineageEdge } from '../../types';
import styles from './index.less';

const { Title, Text } = Typography;
const { Search } = Input;

const LineageGraph: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [graphData, setGraphData] = useState<{ nodes: any[]; edges: any[] }>({ nodes: [], edges: [] });
  const [selectedNode, setSelectedNode] = useState<any>(null);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [direction, setDirection] = useState<'BOTH' | 'UPSTREAM' | 'DOWNSTREAM'>('BOTH');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(false);
  
  const containerRef = useRef<HTMLDivElement>(null);
  const graphInstanceRef = useRef<any>(null);

  // 获取血缘数据
  const fetchLineage = async (assetId?: string) => {
    setLoading(true);
    try {
      const params: any = {
        direction,
        depth: 3,
      };
      
      if (assetId) {
        params.assetId = assetId;
      } else if (searchKeyword) {
        params.keyword = searchKeyword;
      }
      
      const result = await lineageService.getLineageGraph(params);
      setGraphData({
        nodes: result.nodes || [],
        edges: result.edges || [],
      });
      
      // 如果有初始资产ID参数，展开该节点
      const initialAssetId = searchParams.get('assetId');
      if (initialAssetId && !assetId) {
        await fetchLineage(initialAssetId);
      }
    } catch (error) {
      console.error('获取血缘数据失败:', error);
      message.error('获取血缘数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    // 初始加载
    const initialAssetId = searchParams.get('assetId');
    if (initialAssetId) {
      fetchLineage(initialAssetId);
    }
  }, []);

  // 搜索资产
  const handleSearch = () => {
    if (searchKeyword) {
      fetchLineage();
    }
  };

  // 方向切换
  const handleDirectionChange = (value: 'BOTH' | 'UPSTREAM' | 'DOWNSTREAM') => {
    setDirection(value);
    if (graphData.nodes.length > 0) {
      fetchLineage();
    }
  };

  // 全屏切换
  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      containerRef.current?.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  };

  // 导出图片
  const handleExport = () => {
    message.info('导出功能开发中');
  };

  // 刷新
  const handleRefresh = () => {
    fetchLineage();
  };

  // 点击节点
  const handleNodeClick = (node: any) => {
    setSelectedNode(node);
    setDrawerVisible(true);
  };

  // 节点操作菜单
  const getNodeActions = (node: any): MenuProps => ({
    items: [
      {
        key: 'viewDetail',
        label: '查看资产详情',
        onClick: () => {
          if (node.assetId) {
            window.open(`/assets/detail/${node.assetId}`, '_blank');
          }
        },
      },
      {
        key: 'expand',
        label: '展开下游',
        onClick: () => fetchLineage(node.assetId),
      },
      {
        key: 'trace',
        label: '追踪血缘',
        onClick: () => {
          setDirection('BOTH');
          fetchLineage(node.assetId);
        },
      },
    ],
  });

  // 获取节点类型图标
  const getNodeIcon = (type: string) => {
    switch (type) {
      case 'TABLE':
      case 'VIEW':
        return <DatabaseOutlined />;
      case 'API':
        return <ApiOutlined />;
      default:
        return <DatabaseOutlined />;
    }
  };

  // 获取节点颜色
  const getNodeColor = (type: string) => {
    switch (type) {
      case 'TABLE':
        return '#1890ff';
      case 'VIEW':
        return '#52c41a';
      case 'API':
        return '#722ed1';
      default:
        return '#8c8c8c';
    }
  };

  // 简单的可视化渲染（实际项目建议使用 @antv/g6）
  const renderSimpleGraph = () => {
    const { nodes, edges } = graphData;
    
    if (nodes.length === 0) {
      return (
        <div className={styles.empty}>
          <Text type="secondary">暂无血缘数据</Text>
          <Text type="secondary">请搜索资产或选择数据进行血缘分析</Text>
        </div>
      );
    }

    return (
      <div className={styles.graphContainer}>
        {nodes.map((node) => (
          <Dropdown menu={getNodeActions(node)} trigger={['contextMenu']} key={node.id}>
            <div
              className={`${styles.node} ${selectedNode?.id === node.id ? styles.nodeSelected : ''}`}
              style={{ borderColor: getNodeColor(node.type) }}
              onClick={() => handleNodeClick(node)}
            >
              <div className={styles.nodeIcon} style={{ backgroundColor: getNodeColor(node.type) }}>
                {getNodeIcon(node.type)}
              </div>
              <div className={styles.nodeInfo}>
                <Text strong className={styles.nodeName}>{node.name}</Text>
                <Text type="secondary" className={styles.nodeType}>{node.type}</Text>
              </div>
            </div>
          </Dropdown>
        ))}
      </div>
    );
  };

  return (
    <div className={styles.container} ref={containerRef}>
      {/* 顶部工具栏 */}
      <Card className={styles.toolbarCard} size="small">
        <Row align="middle" gutter={16}>
          <Col flex="none">
            <Space>
              <Search
                placeholder="搜索资产名称"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onSearch={handleSearch}
                style={{ width: 240 }}
                allowClear
              />
              <Select
                value={direction}
                onChange={handleDirectionChange}
                style={{ width: 120 }}
                options={[
                  { value: 'BOTH', label: '双向血缘' },
                  { value: 'UPSTREAM', label: '上游血缘' },
                  { value: 'DOWNSTREAM', label: '下游血缘' },
                ]}
              />
              <Button icon={<SearchOutlined />} onClick={handleSearch}>
                查询
              </Button>
            </Space>
          </Col>
          <Col flex="auto">
            <Space>
              <Tooltip title="放大">
                <Button icon={<ZoomInOutlined />} disabled />
              </Tooltip>
              <Tooltip title="缩小">
                <Button icon={<ZoomOutOutlined />} disabled />
              </Tooltip>
              <Tooltip title={autoRefresh ? '暂停自动刷新' : '开启自动刷新'}>
                <Button
                  icon={autoRefresh ? <PauseOutlined /> : <PlayCircleOutlined />}
                  onClick={() => setAutoRefresh(!autoRefresh)}
                  type={autoRefresh ? 'primary' : 'default'}
                />
              </Tooltip>
              <Tooltip title="刷新">
                <Button icon={<ReloadOutlined />} onClick={handleRefresh} />
              </Tooltip>
              <Tooltip title="导出">
                <Button icon={<DownloadOutlined />} onClick={handleExport} />
              </Tooltip>
              <Tooltip title={isFullscreen ? '退出全屏' : '全屏'}>
                <Button icon={<FullscreenOutlined />} onClick={toggleFullscreen} />
              </Tooltip>
            </Space>
          </Col>
          <Col flex="none">
            <Space>
              <Text type="secondary">
                {graphData.nodes.length} 个节点 | {graphData.edges.length} 条边
              </Text>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 图形区域 */}
      <Card className={styles.graphCard}>
        <Spin spinning={loading}>
          <div className={styles.graphArea}>
            {renderSimpleGraph()}
          </div>
        </Spin>
      </Card>

      {/* 节点详情抽屉 */}
      <Drawer
        title="节点详情"
        placement="right"
        width={400}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
      >
        {selectedNode && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="名称">{selectedNode.name}</Descriptions.Item>
            <Descriptions.Item label="类型">
              <Tag color={getNodeColor(selectedNode.type)}>{selectedNode.type}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="所属域">{selectedNode.domainName || '-'}</Descriptions.Item>
            <Descriptions.Item label="Owner">{selectedNode.ownerName || '-'}</Descriptions.Item>
            <Descriptions.Item label="描述">{selectedNode.description || '-'}</Descriptions.Item>
          </Descriptions>
        )}
        
        <div style={{ marginTop: 24 }}>
          <Button
            type="primary"
            block
            onClick={() => {
              if (selectedNode?.assetId) {
                window.open(`/assets/detail/${selectedNode.assetId}`, '_blank');
              }
            }}
          >
            查看资产详情
          </Button>
          <Button
            block
            style={{ marginTop: 8 }}
            onClick={() => {
              setDrawerVisible(false);
              setDirection('BOTH');
              fetchLineage(selectedNode?.assetId);
            }}
          >
            追踪完整血缘
          </Button>
        </div>
      </Drawer>
    </div>
  );
};

export default LineageGraph;
