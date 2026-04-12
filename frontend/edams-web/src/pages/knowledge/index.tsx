/**
 * 知识图谱可视化页面
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Button,
  Space,
  Tag,
  Descriptions,
  Statistic,
  Divider,
  message,
} from 'antd';
import {
  SearchOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  ReloadOutlined,
  ExportOutlined,
  NodeIndexOutlined,
  LoadingOutlined,
} from '@ant-design/icons';
import { http } from '@/services/request';
import { API_PATHS } from '@/constants';
import styles from './index.less';

const { Search } = Input;

// 图谱节点
interface GraphNode {
  id: string;
  name: string;
  type: 'TABLE' | 'FIELD' | 'METRIC' | 'API' | 'DOMAIN' | 'PROCESS';
  typeText: string;
  description?: string;
  properties?: Record<string, any>;
}

// 图谱边
interface GraphEdge {
  source: string;
  target: string;
  label?: string;
  relationType?: string;
}

// 图谱统计
interface GraphStats {
  totalNodes: number;
  totalEdges: number;
  entityDistribution: Record<string, number>;
}

// 节点详情
interface NodeDetail {
  node: GraphNode;
  relations: GraphNode[];
  relationsInfo: { node: GraphNode; edge: GraphEdge }[];
}

// 图谱数据
interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

const KnowledgeGraph: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [filterType, setFilterType] = useState<string | undefined>(undefined);
  const [selectedNode, setSelectedNode] = useState<NodeDetail | null>(null);
  const [graphScale, setGraphScale] = useState(1);
  const [graphData, setGraphData] = useState<GraphData>({ nodes: [], edges: [] });
  const [stats, setStats] = useState<GraphStats>({
    totalNodes: 0,
    totalEdges: 0,
    entityDistribution: {},
  });

  // 加载图谱数据
  const loadGraphData = useCallback(async () => {
    setLoading(true);
    try {
      // 调用知识图谱API
      const result = await http.get<any>(API_PATHS.KNOWLEDGE.GRAPH, {
        keyword: searchValue || undefined,
        type: filterType || undefined,
      });

      const nodes: GraphNode[] = Array.isArray(result) ? result : (result?.nodes || []);
      const edges: GraphEdge[] = result?.edges || [];

      setGraphData({ nodes, edges });

      // 计算统计
      const distribution: Record<string, number> = {};
      nodes.forEach(node => {
        const type = node.type || 'UNKNOWN';
        distribution[type] = (distribution[type] || 0) + 1;
      });

      setStats({
        totalNodes: nodes.length,
        totalEdges: edges.length,
        entityDistribution: distribution,
      });
    } catch (error) {
      console.error('加载图谱数据失败:', error);
      message.error('加载图谱数据失败');
    } finally {
      setLoading(false);
    }
  }, [searchValue, filterType]);

  useEffect(() => {
    loadGraphData();
  }, [loadGraphData]);

  // 获取节点类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      TABLE: { color: 'blue', label: '表' },
      FIELD: { color: 'green', label: '字段' },
      METRIC: { color: 'purple', label: '指标' },
      API: { color: 'orange', label: 'API' },
      DOMAIN: { color: 'cyan', label: '业务域' },
      PROCESS: { color: 'magenta', label: '流程' },
    };
    return config[type] || { color: 'default', label: type };
  };

  // 筛选后的节点
  const filteredNodes = graphData.nodes.filter(node => {
    const matchSearch = !searchValue || node.name.toLowerCase().includes(searchValue.toLowerCase());
    const matchType = !filterType || node.type === filterType;
    return matchSearch && matchType;
  });

  // 筛选后的边
  const filteredEdges = graphData.edges.filter(edge =>
    filteredNodes.some(n => n.id === edge.source) && filteredNodes.some(n => n.id === edge.target)
  );

  // 点击节点
  const handleNodeClick = useCallback((node: GraphNode) => {
    // 找到关联边和节点
    const relatedEdges = graphData.edges.filter(e => e.source === node.id || e.target === node.id);
    const relatedInfo = relatedEdges.map(edge => {
      const otherId = edge.source === node.id ? edge.target : edge.source;
      const otherNode = graphData.nodes.find(n => n.id === otherId);
      return otherNode ? { node: otherNode, edge } : null;
    }).filter(Boolean) as { node: GraphNode; edge: GraphEdge }[];

    setSelectedNode({
      node,
      relations: relatedInfo.map(info => info.node),
      relationsInfo: relatedInfo,
    });
  }, [graphData]);

  // 放大
  const handleZoomIn = () => setGraphScale(prev => Math.min(prev + 0.2, 2));

  // 缩小
  const handleZoomOut = () => setGraphScale(prev => Math.max(prev - 0.2, 0.4));

  // 重置视图
  const handleReset = () => setGraphScale(1);

  // 导出图谱
  const handleExport = async () => {
    try {
      // 导出为JSON
      const exportData = JSON.stringify({ nodes: filteredNodes, edges: filteredEdges }, null, 2);
      const blob = new Blob([exportData], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `knowledge-graph-${Date.now()}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      message.success('图谱已导出');
    } catch (error) {
      message.error('导出失败');
    }
  };

  // 搜索
  const handleSearch = (value: string) => {
    setSearchValue(value);
  };

  return (
    <div className={styles.container}>
      {/* 顶部搜索和筛选 */}
      <Card className={styles.searchCard}>
        <Space size="large" wrap>
          <Search
            placeholder="搜索节点名称"
            value={searchValue}
            onChange={e => setSearchValue(e.target.value)}
            onSearch={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
            allowClear
          />
          <Select
            placeholder="筛选类型"
            value={filterType}
            onChange={setFilterType}
            style={{ width: 150 }}
            allowClear
          >
            <Select.Option value="TABLE">表</Select.Option>
            <Select.Option value="FIELD">字段</Select.Option>
            <Select.Option value="METRIC">指标</Select.Option>
            <Select.Option value="API">API</Select.Option>
            <Select.Option value="DOMAIN">业务域</Select.Option>
            <Select.Option value="PROCESS">流程</Select.Option>
          </Select>
          <Space>
            <Button icon={<ZoomInOutlined />} onClick={handleZoomIn}>放大</Button>
            <Button icon={<ZoomOutOutlined />} onClick={handleZoomOut}>缩小</Button>
            <Button icon={<ReloadOutlined />} onClick={loadGraphData}>刷新</Button>
            <Button icon={<ExportOutlined />} onClick={handleExport}>导出</Button>
          </Space>
        </Space>
      </Card>

      {/* 主区域 */}
      <Row gutter={16}>
        {/* 左侧图谱区域 */}
        <Col span={selectedNode ? 16 : 24}>
          <Card title="知识图谱" className={styles.graphCard}>
            {loading ? (
              <div className={styles.loadingState}>
                <LoadingOutlined style={{ fontSize: 48, color: '#1890ff' }} />
                <p>加载中...</p>
              </div>
            ) : (
              <div
                className={styles.graphContainer}
                style={{ transform: `scale(${graphScale})`, transformOrigin: 'top left' }}
              >
                <div className={styles.graphArea}>
                  {filteredNodes.map((node, index) => {
                    // 简单的圆形布局
                    const angle = (index / Math.max(filteredNodes.length, 1)) * 2 * Math.PI;
                    const radius = 180;
                    const x = 250 + radius * Math.cos(angle);
                    const y = 200 + radius * Math.sin(angle);

                    return (
                      <div
                        key={node.id}
                        className={`${styles.graphNode} ${selectedNode?.node.id === node.id ? styles.selected : ''}`}
                        style={{
                          left: x,
                          top: y,
                          borderColor: getTypeTag(node.type).color,
                        }}
                        onClick={() => handleNodeClick(node)}
                      >
                        <Tag color={getTypeTag(node.type).color} style={{ marginBottom: 4 }}>
                          {getTypeTag(node.type).label}
                        </Tag>
                        <div className={styles.nodeName} title={node.name}>
                          {node.name.length > 15 ? node.name.substring(0, 15) + '...' : node.name}
                        </div>
                      </div>
                    );
                  })}

                  {/* 绘制连线 */}
                  <svg className={styles.edgeSvg}>
                    {filteredNodes.map((sourceNode, index) => {
                      const angle = (index / Math.max(filteredNodes.length, 1)) * 2 * Math.PI;
                      const radius = 180;
                      const x1 = 250 + radius * Math.cos(angle);
                      const y1 = 200 + radius * Math.sin(angle);

                      return filteredEdges
                        .filter(e => e.source === sourceNode.id)
                        .map((edge) => {
                          const targetIndex = filteredNodes.findIndex(n => n.id === edge.target);
                          if (targetIndex === -1) return null;
                          const angle2 = (targetIndex / Math.max(filteredNodes.length, 1)) * 2 * Math.PI;
                          const x2 = 250 + radius * Math.cos(angle2);
                          const y2 = 200 + radius * Math.sin(angle2);

                          return (
                            <line
                              key={`${edge.source}-${edge.target}`}
                              x1={x1}
                              y1={y1}
                              x2={x2}
                              y2={y2}
                              stroke="#d9d9d9"
                              strokeWidth="1"
                              markerEnd="url(#arrow)"
                            />
                          );
                        });
                    })}
                    <defs>
                      <marker id="arrow" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
                        <path d="M0,0 L0,6 L9,3 z" fill="#d9d9d9" />
                      </marker>
                    </defs>
                  </svg>
                </div>

                {/* 空状态 */}
                {filteredNodes.length === 0 && (
                  <div className={styles.emptyState}>
                    <NodeIndexOutlined style={{ fontSize: 48, color: '#d9d9d9' }} />
                    <p>暂无数据，请调整筛选条件或导入数据</p>
                  </div>
                )}
              </div>
            )}
          </Card>
        </Col>

        {/* 右侧详情面板 */}
        {selectedNode && (
          <Col span={8}>
            <Card title="节点详情" className={styles.detailCard} extra={<Button size="small" onClick={() => setSelectedNode(null)}>关闭</Button>}>
              <Descriptions column={1} size="small">
                <Descriptions.Item label="名称">{selectedNode.node.name}</Descriptions.Item>
                <Descriptions.Item label="类型">
                  <Tag color={getTypeTag(selectedNode.node.type).color}>{getTypeTag(selectedNode.node.type).label}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="描述">{selectedNode.node.description || '-'}</Descriptions.Item>
                {selectedNode.node.properties && (
                  <Descriptions.Item label="属性">
                    {Object.entries(selectedNode.node.properties).slice(0, 3).map(([k, v]) => (
                      <div key={k}>{k}: {String(v)}</div>
                    ))}
                  </Descriptions.Item>
                )}
              </Descriptions>

              <Divider>关联节点 ({selectedNode.relations.length})</Divider>

              <div className={styles.relationList}>
                {selectedNode.relationsInfo.map((info, index) => (
                  <div
                    key={index}
                    className={styles.relationItem}
                    onClick={() => handleNodeClick(info.node)}
                  >
                    <Tag color={getTypeTag(info.node.type).color}>{getTypeTag(info.node.type).label}</Tag>
                    <span>{info.node.name}</span>
                    {info.edge.label && <Tag style={{ marginLeft: 4 }}>{info.edge.label}</Tag>}
                  </div>
                ))}
                {selectedNode.relations.length === 0 && <p style={{ color: '#999' }}>暂无关联节点</p>}
              </div>
            </Card>
          </Col>
        )}
      </Row>

      {/* 底部统计 */}
      <Card className={styles.statsCard}>
        <Row gutter={24}>
          <Col span={6}>
            <Statistic title="节点总数" value={stats.totalNodes} />
          </Col>
          <Col span={6}>
            <Statistic title="关系总数" value={stats.totalEdges} />
          </Col>
          <Col span={12}>
            <div className={styles.distribution}>
              <span style={{ marginRight: 16 }}>实体类型分布：</span>
              {Object.entries(stats.entityDistribution).map(([type, count]) => (
                <Tag key={type} color={getTypeTag(type).color}>
                  {getTypeTag(type).label} {count}
                </Tag>
              ))}
            </div>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default KnowledgeGraph;
