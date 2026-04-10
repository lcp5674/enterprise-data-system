/**
 * 影响分析页面
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
  Table,
  Tag,
  Tree,
  Modal,
  message,
  Alert,
} from 'antd';
import {
  SearchOutlined,
  ArrowRightOutlined,
  WarningOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import LineageGraph from '@/components/LineageGraph';
import type { ColumnsType } from 'antd/es/table';
import type { DataAsset } from '@/types';
import styles from './Impact.less';

const { Option } = Select;
const { TextArea } = Input;

interface ImpactForm {
  assetName?: string;
  assetType?: string;
  impactLevel?: string;
}

interface AffectedAsset {
  key: string;
  name: string;
  type: string;
  level: number;
  status: 'pending' | 'warning' | 'critical';
  owner: string;
  lastUpdated: string;
}

const ImpactAnalysisPage: React.FC = () => {
  const [form] = Form.useForm<ImpactForm>();
  const [loading, setLoading] = useState(false);
  const [analyzeLoading, setAnalyzeLoading] = useState(false);
  const [showImpactGraph, setShowImpactGraph] = useState(false);
  const [impactNodes, setImpactNodes] = useState<any[]>([]);
  const [impactEdges, setImpactEdges] = useState<any[]>([]);
  const [affectedAssets, setAffectedAssets] = useState<AffectedAsset[]>([]);

  // 模拟影响分析数据
  const mockImpactData = {
    nodes: [
      { id: 'center', name: 'customer_dimension', type: 'TABLE', tableType: 'WAREHOUSE', qualityScore: 92 },
      { id: 'down1', name: 'sales_report', type: 'TABLE', tableType: 'DATAMART' },
      { id: 'down2', name: 'CustomerAPI', type: 'API' },
      { id: 'down3', name: 'customer_mart', type: 'TABLE', tableType: 'DATAMART' },
      { id: 'down4', name: 'revenue_dashboard', type: 'FILE' },
      { id: 'down5', name: 'ExecutiveReport', type: 'API' },
    ],
    edges: [
      { source: 'center', target: 'down1', type: 'REFERENCE' },
      { source: 'center', target: 'down2', type: 'REFERENCE' },
      { source: 'center', target: 'down3', type: 'REFERENCE' },
      { source: 'down3', target: 'down4', type: 'REFERENCE' },
      { source: 'down3', target: 'down5', type: 'REFERENCE' },
    ],
  };

  const mockAffectedAssets: AffectedAsset[] = [
    { key: '1', name: 'sales_report', type: 'TABLE', level: 1, status: 'critical', owner: '张三', lastUpdated: '2024-01-15' },
    { key: '2', name: 'CustomerAPI', type: 'API', level: 1, status: 'warning', owner: '李四', lastUpdated: '2024-01-14' },
    { key: '3', name: 'customer_mart', type: 'TABLE', level: 2, status: 'warning', owner: '王五', lastUpdated: '2024-01-13' },
    { key: '4', name: 'revenue_dashboard', type: 'FILE', level: 3, status: 'pending', owner: '赵六', lastUpdated: '2024-01-12' },
    { key: '5', name: 'ExecutiveReport', type: 'API', level: 3, status: 'pending', owner: '孙七', lastUpdated: '2024-01-11' },
  ];

  // 执行影响分析
  const handleAnalyze = useCallback(async (values: ImpactForm) => {
    setAnalyzeLoading(true);
    setLoading(true);
    
    await new Promise((resolve) => setTimeout(resolve, 1000));
    
    setImpactNodes(mockImpactData.nodes);
    setImpactEdges(mockImpactData.edges);
    setAffectedAssets(mockAffectedAssets);
    setShowImpactGraph(true);
    
    setAnalyzeLoading(false);
    setLoading(false);
    
    message.success('影响分析完成');
  }, []);

  // 重置
  const handleReset = useCallback(() => {
    form.resetFields();
    setShowImpactGraph(false);
    setImpactNodes([]);
    setImpactEdges([]);
    setAffectedAssets([]);
  }, [form]);

  const affectedColumns: ColumnsType<AffectedAsset> = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
      render: (name, record) => (
        <Space>
          {Array(record.level).fill(0).map((_, i) => (
            <span key={i} style={{ color: '#d9d9d9' }}>─</span>
          ))}
          <ArrowRightOutlined style={{ color: '#d9d9d9' }} />
          <span>{name}</span>
        </Space>
      ),
    },
    { title: '类型', dataIndex: 'type', key: 'type', render: (type) => <Tag>{type}</Tag> },
    {
      title: '影响状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const config: Record<string, { color: string; icon: React.ReactNode; text: string }> = {
          critical: { color: 'red', icon: <WarningOutlined />, text: '严重' },
          warning: { color: 'orange', icon: <WarningOutlined />, text: '警告' },
          pending: { color: 'blue', icon: <CheckCircleOutlined />, text: '待评估' },
        };
        const c = config[status];
        return <Tag color={c.color} icon={c.icon}>{c.text}</Tag>;
      },
    },
    { title: '负责人', dataIndex: 'owner', key: 'owner' },
    { title: '更新时间', dataIndex: 'lastUpdated', key: 'lastUpdated' },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Button type="link" size="small">
          评估影响
        </Button>
      ),
    },
  ];

  const treeData = [
    {
      title: 'customer_dimension (当前)',
      key: 'center',
      icon: <WarningOutlined style={{ color: '#fa8c16' }} />,
      children: [
        {
          title: 'sales_report',
          key: 'down1',
          icon: <WarningOutlined style={{ color: '#ff4d4f' }} />,
        },
        {
          title: 'CustomerAPI',
          key: 'down2',
          icon: <WarningOutlined style={{ color: '#faad14' }} />,
        },
        {
          title: 'customer_mart',
          key: 'down3',
          icon: <WarningOutlined style={{ color: '#faad14' }} />,
          children: [
            { title: 'revenue_dashboard', key: 'down4' },
            { title: 'ExecutiveReport', key: 'down5' },
          ],
        },
      ],
    },
  ];

  return (
    <div className={styles.impactAnalysisPage}>
      <Row gutter={16}>
        {/* 左侧分析表单 */}
        <Col span={8}>
          <Card title="影响分析" className={styles.analysisCard}>
            <Form form={form} layout="vertical" onFinish={handleAnalyze}>
              <Form.Item name="assetName" label="资产名称" rules={[{ required: true }]}>
                <Input placeholder="请输入要分析的资产名称" />
              </Form.Item>
              <Form.Item name="assetType" label="资产类型">
                <Select placeholder="请选择" allowClear>
                  <Option value="TABLE">表</Option>
                  <Option value="API">API</Option>
                  <Option value="FILE">文件</Option>
                </Select>
              </Form.Item>
              <Form.Item name="impactLevel" label="影响范围">
                <Select placeholder="请选择" allowClear>
                  <Option value="direct">直接影响</Option>
                  <Option value="all">全部影响</Option>
                </Select>
              </Form.Item>
              
              <Alert
                type="info"
                showIcon
                icon={<WarningOutlined />}
                message="提示"
                description="影响分析将展示指定资产变更后可能影响的下游资产列表，帮助评估变更风险。"
                style={{ marginBottom: 16 }}
              />
              
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" htmlType="submit" block loading={analyzeLoading}>
                  执行影响分析
                </Button>
                <Button onClick={handleReset} block>
                  重置
                </Button>
              </Space>
            </Form>
          </Card>

          {/* 影响资产树 */}
          {showImpactGraph && (
            <Card title="影响路径" className={styles.treeCard}>
              <Tree
                showIcon
                defaultExpandAll
                treeData={treeData}
                titleRender={(node) => <span>{String(node.title)}</span>}
              />
            </Card>
          )}
        </Col>

        {/* 右侧影响图 */}
        <Col span={16}>
          {showImpactGraph ? (
            <>
              <Card
                title="影响传播图"
                className={styles.graphCard}
                extra={
                  <Space>
                    <Button size="small">导出报告</Button>
                    <Button size="small">发送通知</Button>
                  </Space>
                }
              >
                <LineageGraph
                  nodes={impactNodes}
                  edges={impactEdges}
                  height={400}
                  loading={loading}
                  layout="TB"
                />
              </Card>

              <Card title="受影响的资产" className={styles.affectedCard}>
                <Table
                  columns={affectedColumns}
                  dataSource={affectedAssets}
                  rowKey="key"
                  pagination={{ pageSize: 5 }}
                />
              </Card>
            </>
          ) : (
            <Card className={styles.emptyCard}>
              <div className={styles.emptyState}>
                <WarningOutlined style={{ fontSize: 48, color: '#d9d9d9' }} />
                <p>请先选择要分析的资产</p>
                <p className={styles.emptyHint}>
                  输入资产名称后点击"执行影响分析"
                </p>
              </div>
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ImpactAnalysisPage;
