/**
 * 知识图谱可视化页面
 */

import React, { useState, useEffect } from 'react';
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
} from 'antd';
import {
  SearchOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  ReloadOutlined,
  ExportOutlined,
  NodeIndexOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Search } = Input;

interface GraphNode {
  id: string;
  name: string;
  type: 'TABLE' | 'FIELD' | 'METRIC' | 'API';
  description?: string;
  x?: number;
  y?: number;
}

interface GraphEdge {
  source: string;
  target: string;
  label?: string;
}

interface NodeDetail {
  node: GraphNode;
  relations: GraphNode[];
}

const KnowledgeGraph: React.FC = () => {
  const [searchValue, setSearchValue] = useState('');
  const [filterType, setFilterType] = useState<string | undefined>(undefined);
  const [selectedNode, setSelectedNode] = useState<NodeDetail | null>(null);
  const [graphScale, setGraphScale] = useState(1);

  // 模拟图谱数据
  const mockNodes: GraphNode[] = [
    { id: '1', name: '客户信息表 (dwd_customer)', type: 'TABLE', description: '客户主数据表，包含客户基本信息' },
    { id: '2', name: 'customer_id', type: 'FIELD', description: '客户唯一标识' },
    { id: '3', name: 'customer_name', type: 'FIELD', description: '客户名称' },
    { id: '4', name: '订单事实表 (dwd_order)', type: 'TABLE', description: '订单明细数据' },
    { id: '5', name: 'order_id', type: 'FIELD', description: '订单唯一标识' },
    { id: '6', name: 'customer_id_fk', type: 'FIELD', description: '客户外键' },
    { id: '7', name: '客户订单统计指标', type: 'METRIC', description: '统计每个客户的订单数量和金额' },
    { id: '8', name: '客户分析API', type: 'API', description: '提供客户分析和查询接口' },
    { id: '9', name: '商品信息表 (dwd_product)', type: 'TABLE', description: '商品主数据' },
    { id: '10', name: 'product_id', type: 'FIELD', description: '商品唯一标识' },
  ];

  const mockEdges: GraphEdge[] = [
    { source: '1', target: '2', label: '包含' },
    { source: '1', target: '3', label: '包含' },
    { source: '4', target: '5', label: '包含' },
    { source: '4', target: '6', label: '引用' },
    { source: '1', target: '4', label: '关联' },
    { source: '7', target: '1', label: '依赖' },
    { source: '7', target: '4', label: '依赖' },
    { source: '8', target: '7', label: '调用' },
    { source: '9', target: '10', label: '包含' },
    { source: '4', target: '9', label: '关联' },
  ];

  // 图谱统计
  const graphStats = {
    totalNodes: mockNodes.length,
    totalEdges: mockEdges.length,
    entityDistribution: {
      TABLE: mockNodes.filter(n => n.type === 'TABLE').length,
      FIELD: mockNodes.filter(n => n.type === 'FIELD').length,
      METRIC: mockNodes.filter(n => n.type === 'METRIC').length,
      API: mockNodes.filter(n => n.type === 'API').length,
    },
  };

  // 筛选后的节点
  const filteredNodes = mockNodes.filter(node => {
    const matchSearch = !searchValue || node.name.toLowerCase().includes(searchValue.toLowerCase());
    const matchType = !filterType || node.type === filterType;
    return matchSearch && matchType;
  });

  // 筛选后的边（只显示两端节点都在筛选结果中的边）
  const filteredEdges = mockEdges.filter(edge =>
    filteredNodes.some(n => n.id === edge.source) && filteredNodes.some(n => n.id === edge.target)
  );

  // 获取节点类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      TABLE: { color: 'blue', label: '表' },
      FIELD: { color: 'green', label: '字段' },
      METRIC: { color: 'purple', label: '指标' },
      API: { color: 'orange', label: 'API' },
    };
    return config[type] || { color: 'default', label: type };
  };

  // 点击节点
  const handleNodeClick = (node: GraphNode) => {
    // 找到关联节点
    const relatedIds = mockEdges
      .filter(e => e.source === node.id || e.target === node.id)
      .flatMap(e => [e.source, e.target])
      .filter(id => id !== node.id);
    const relatedNodes = mockNodes.filter(n => relatedIds.includes(n.id));

    setSelectedNode({ node, relations: relatedNodes });
  };

  // 放大
  const handleZoomIn = () => setGraphScale(prev => Math.min(prev + 0.2, 2));

  // 缩小
  const handleZoomOut = () => setGraphScale(prev => Math.max(prev - 0.2, 0.4));

  // 重置视图
  const handleReset = () => setGraphScale(1);

  // 导出图谱
  const handleExport = () => {
    console.log('导出图谱...');
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
          </Select>
          <Space>
            <Button icon={<ZoomInOutlined />} onClick={handleZoomIn}>放大</Button>
            <Button icon={<ZoomOutOutlined />} onClick={handleZoomOut}>缩小</Button>
            <Button icon={<ReloadOutlined />} onClick={handleReset}>重置</Button>
            <Button icon={<ExportOutlined />} onClick={handleExport}>导出</Button>
          </Space>
        </Space>
      </Card>

      {/* 主区域 */}
      <Row gutter={16}>
        {/* 左侧图谱区域 */}
        <Col span={selectedNode ? 16 : 24}>
          <Card title="知识图谱" className={styles.graphCard}>
            <div
              className={styles.graphContainer}
              style={{ transform: `scale(${graphScale})`, transformOrigin: 'top left' }}
            >
              {/* 简化的图谱可视化 */}
              <div className={styles.graphArea}>
                {filteredNodes.map((node, index) => {
                  // 简单的圆形布局
                  const angle = (index / filteredNodes.length) * 2 * Math.PI;
                  const radius = 180;
                  const x = 250 + radius * Math.cos(angle);
                  const y = 200 + radius * Math.sin(angle);

                  return (
                    <div
                      key={node.id}
                      className={styles.graphNode}
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

                {/* 绘制连线（简化的SVG连线） */}
                <svg className={styles.edgeSvg}>
                  {filteredNodes.map((sourceNode, index) => {
                    const angle = (index / filteredNodes.length) * 2 * Math.PI;
                    const radius = 180;
                    const x1 = 250 + radius * Math.cos(angle);
                    const y1 = 200 + radius * Math.sin(angle);

                    return filteredEdges
                      .filter(e => e.source === sourceNode.id)
                      .map((edge, edgeIndex) => {
                        const targetIndex = filteredNodes.findIndex(n => n.id === edge.target);
                        if (targetIndex === -1) return null;
                        const angle2 = (targetIndex / filteredNodes.length) * 2 * Math.PI;
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
                  <p>暂无数据，请调整筛选条件</p>
                </div>
              )}
            </div>
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
              </Descriptions>

              <Divider>关联节点</Divider>

              <div className={styles.relationList}>
                {selectedNode.relations.map(relation => (
                  <div
                    key={relation.id}
                    className={styles.relationItem}
                    onClick={() => handleNodeClick(relation)}
                  >
                    <Tag color={getTypeTag(relation.type).color}>{getTypeTag(relation.type).label}</Tag>
                    <span>{relation.name}</span>
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
            <Statistic title="节点总数" value={graphStats.totalNodes} />
          </Col>
          <Col span={6}>
            <Statistic title="关系总数" value={graphStats.totalEdges} />
          </Col>
          <Col span={12}>
            <div className={styles.distribution}>
              <span style={{ marginRight: 16 }}>实体类型分布：</span>
              <Tag color="blue">表 {graphStats.entityDistribution.TABLE}</Tag>
              <Tag color="green">字段 {graphStats.entityDistribution.FIELD}</Tag>
              <Tag color="purple">指标 {graphStats.entityDistribution.METRIC}</Tag>
              <Tag color="orange">API {graphStats.entityDistribution.API}</Tag>
            </div>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default KnowledgeGraph;
