/**
 * 数据血缘图页面
 */
import React, { useState, useCallback } from 'react';
import {
  Card,
  Row,
  Col,
  Form,
  Select,
  Input,
  Button,
  Space,
  Drawer,
  Descriptions,
  Tag,
  Table,
  Modal,
  message,
} from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import LineageGraph, { LineageNode, LineageEdge } from '@/components/LineageGraph';
import type { ColumnsType } from 'antd/es/table';
import styles from './Graph.less';

const { Option } = Select;

interface SearchForm {
  assetName?: string;
  assetType?: string;
  database?: string;
}

const LineageGraphPage: React.FC = () => {
  const [form] = Form.useForm<SearchForm>();
  const [loading, setLoading] = useState(false);
  const [selectedNode, setSelectedNode] = useState<LineageNode | null>(null);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [nodes, setNodes] = useState<LineageNode[]>([]);
  const [edges, setEdges] = useState<LineageEdge[]>([]);

  // 模拟数据
  const mockNodes: LineageNode[] = [
    {
      id: 'node1',
      name: 'customer_source',
      type: 'TABLE',
      tableType: 'SOURCE',
      database: 'oltp_db',
      schema: 'public',
      qualityScore: 98,
      owner: '张三',
      fields: [
        { name: 'customer_id', type: 'BIGINT' },
        { name: 'name', type: 'VARCHAR(100)' },
        { name: 'email', type: 'VARCHAR(200)' },
        { name: 'phone', type: 'VARCHAR(20)' },
      ],
    },
    {
      id: 'node2',
      name: 'customer_staging',
      type: 'TABLE',
      tableType: 'STAGING',
      database: 'dw_db',
      schema: 'staging',
      qualityScore: 95,
      owner: '李四',
      fields: [
        { name: 'customer_id', type: 'BIGINT' },
        { name: 'name', type: 'VARCHAR(100)' },
        { name: 'email', type: 'VARCHAR(200)' },
        { name: 'phone', type: 'VARCHAR(20)' },
        { name: 'load_time', type: 'TIMESTAMP' },
      ],
    },
    {
      id: 'node3',
      name: 'customer_dimension',
      type: 'TABLE',
      tableType: 'WAREHOUSE',
      database: 'dw_db',
      schema: 'warehouse',
      qualityScore: 92,
      owner: '王五',
      fields: [
        { name: 'customer_key', type: 'BIGINT' },
        { name: 'customer_id', type: 'BIGINT' },
        { name: 'name', type: 'VARCHAR(100)' },
        { name: 'email_hash', type: 'VARCHAR(64)' },
      ],
    },
    {
      id: 'node4',
      name: 'customer_mart',
      type: 'TABLE',
      tableType: 'DATAMART',
      database: 'dm_db',
      schema: 'sales',
      qualityScore: 88,
      owner: '赵六',
      fields: [
        { name: 'customer_key', type: 'BIGINT' },
        { name: 'order_count', type: 'INT' },
        { name: 'total_amount', type: 'DECIMAL(18,2)' },
      ],
    },
    {
      id: 'node5',
      name: 'CustomerAPI',
      type: 'API',
      qualityScore: 90,
      owner: '孙七',
    },
    {
      id: 'node6',
      name: 'customer_report',
      type: 'FILE',
      qualityScore: 85,
      owner: '周八',
    },
  ];

  const mockEdges: LineageEdge[] = [
    {
      id: 'edge1',
      source: 'node1',
      target: 'node2',
      type: 'LOAD',
      transformation: 'CDC同步',
      schedule: '*/5 * * * *',
    },
    {
      id: 'edge2',
      source: 'node2',
      target: 'node3',
      type: 'TRANSFORM',
      transformation: '数据清洗、脱敏',
      schedule: '0 2 * * *',
    },
    {
      id: 'edge3',
      source: 'node3',
      target: 'node4',
      type: 'DERIVE',
      transformation: '聚合计算',
      schedule: '0 6 * * *',
    },
    {
      id: 'edge4',
      source: 'node4',
      target: 'node5',
      type: 'REFERENCE',
    },
    {
      id: 'edge5',
      source: 'node4',
      target: 'node6',
      type: 'REFERENCE',
    },
  ];

  // 搜索血缘
  const handleSearch = useCallback(async (values: SearchForm) => {
    setLoading(true);
    
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 800));
    
    // 模拟返回数据
    setNodes(mockNodes);
    setEdges(mockEdges);
    
    setLoading(false);
    message.success('血缘关系加载成功');
  }, []);

  // 重置搜索
  const handleReset = useCallback(() => {
    form.resetFields();
    setNodes([]);
    setEdges([]);
  }, [form]);

  // 点击节点
  const handleNodeClick = useCallback((node: LineageNode) => {
    setSelectedNode(node);
    setDrawerVisible(true);
  }, []);

  // 双击展开节点
  const handleExpandNode = useCallback((node: LineageNode) => {
    message.info(`正在获取 ${node.name} 的更多血缘关系...`);
    // 这里可以调用API获取更多上下游节点
  }, []);

  // 导出图片
  const handleExport = useCallback(() => {
    message.success('血缘图已导出');
  }, []);

  const nodeColumns: ColumnsType<{ name: string; type: string }> = [
    { title: '字段名', dataIndex: 'name', key: 'name' },
    { title: '类型', dataIndex: 'type', key: 'type' },
  ];

  return (
    <div className={styles.lineageGraphPage}>
      {/* 搜索表单 */}
      <Card className={styles.searchCard}>
        <Form form={form} layout="inline" onFinish={handleSearch}>
          <Form.Item name="assetName" label="资产名称">
            <Input placeholder="请输入资产名称" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item name="assetType" label="资产类型">
            <Select placeholder="请选择" style={{ width: 120 }} allowClear>
              <Option value="TABLE">表</Option>
              <Option value="API">API</Option>
              <Option value="FILE">文件</Option>
            </Select>
          </Form.Item>
          <Form.Item name="database" label="数据库">
            <Select placeholder="请选择" style={{ width: 150 }} allowClear>
              <Option value="oltp_db">oltp_db</Option>
              <Option value="dw_db">dw_db</Option>
              <Option value="dm_db">dm_db</Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                查询
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      {/* 血缘图 */}
      <Card className={styles.graphCard}>
        <LineageGraph
          nodes={nodes}
          edges={edges}
          height={550}
          loading={loading}
          onNodeClick={handleNodeClick}
          onExpandNode={handleExpandNode}
          onExportImage={handleExport}
        />
      </Card>

      {/* 节点详情抽屉 */}
      <Drawer
        title="节点详情"
        placement="right"
        width={500}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
      >
        {selectedNode && (
          <>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="名称" span={2}>
                <strong>{selectedNode.name}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="类型">
                <Tag color="blue">{selectedNode.type}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="表类型">
                {selectedNode.tableType && <Tag>{selectedNode.tableType}</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="数据库">
                {selectedNode.database || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Schema">
                {selectedNode.schema || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="负责人">
                {selectedNode.owner || '-'}
              </Descriptions.Item>
              <Descriptions.Item label="质量评分">
                <Tag color={selectedNode.qualityScore && selectedNode.qualityScore >= 80 ? 'green' : 'orange'}>
                  {selectedNode.qualityScore ? `${selectedNode.qualityScore}%` : '-'}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            {selectedNode.fields && selectedNode.fields.length > 0 && (
              <>
                <h4 style={{ marginTop: 16 }}>字段列表</h4>
                <Table
                  columns={nodeColumns}
                  dataSource={selectedNode.fields}
                  rowKey="name"
                  size="small"
                  pagination={false}
                />
              </>
            )}

            <Space style={{ marginTop: 16 }}>
              <Button type="primary">查看详情</Button>
              <Button>血缘分析</Button>
              <Button>质量报告</Button>
            </Space>
          </>
        )}
      </Drawer>
    </div>
  );
};

export default LineageGraphPage;
