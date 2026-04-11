/**
 * 水印管理页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Button,
  Space,
  Tag,
  Input,
  Modal,
  Form,
  message,
  Popconfirm,
  Slider,
  Select,
  Radio,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  ExperimentOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Search } = Input;

interface WatermarkPolicy {
  id: string;
  name: string;
  type: 'VISIBLE' | 'INVISIBLE';
  content: string;
  application: 'TEXT' | 'LOGO' | 'QRCODE';
  scope: 'GLOBAL' | 'SPECIFIC';
  opacity: number;
  position: string;
  status: 'ENABLED' | 'DISABLED';
  createdTime: string;
  assetCount?: number;
}

const WatermarkManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<WatermarkPolicy[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [testVisible, setTestVisible] = useState(false);
  const [editingPolicy, setEditingPolicy] = useState<WatermarkPolicy | null>(null);
  const [form] = Form.useForm();

  // 模拟数据
  const mockPolicies: WatermarkPolicy[] = [
    { id: '1', name: '企业版权水印', type: 'VISIBLE', content: '© Enterprise Data System', application: 'TEXT', scope: 'GLOBAL', opacity: 30, position: '右下角', status: 'ENABLED', createdTime: '2026-03-01', assetCount: 156 },
    { id: '2', name: '机密标识水印', type: 'VISIBLE', content: 'CONFIDENTIAL', application: 'TEXT', scope: 'SPECIFIC', opacity: 25, position: '全屏', status: 'ENABLED', createdTime: '2026-03-05', assetCount: 23 },
    { id: '3', name: '用户追踪隐水印', type: 'INVISIBLE', content: 'user_trace_2026', application: 'TEXT', scope: 'GLOBAL', opacity: 10, position: '随机', status: 'ENABLED', createdTime: '2026-03-10', assetCount: 89 },
    { id: '4', name: '官方Logo水印', type: 'VISIBLE', content: 'official_logo.png', application: 'LOGO', scope: 'GLOBAL', opacity: 40, position: '右下角', status: 'ENABLED', createdTime: '2026-03-15', assetCount: 45 },
    { id: '5', name: '导出追溯二维码', type: 'VISIBLE', content: 'trace_qr.png', application: 'QRCODE', scope: 'SPECIFIC', opacity: 35, position: '右下角', status: 'DISABLED', createdTime: '2026-03-20', assetCount: 12 },
    { id: '6', name: '数据血缘隐水印', type: 'INVISIBLE', content: 'lineage_hash_v2', application: 'TEXT', scope: 'GLOBAL', opacity: 5, position: '随机', status: 'ENABLED', createdTime: '2026-04-01', assetCount: 67 },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockPolicies);
      setLoading(false);
    }, 500);
  }, []);

  // 搜索
  const handleSearch = (value: string) => {
    if (!value) {
      setDataSource(mockPolicies);
      return;
    }
    setDataSource(mockPolicies.filter(p => p.name.includes(value)));
  };

  // 新增策略
  const handleAdd = () => {
    setEditingPolicy(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑策略
  const handleEdit = (record: WatermarkPolicy) => {
    setEditingPolicy(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 删除策略
  const handleDelete = async (id: string) => {
    message.success('删除成功');
    setDataSource(dataSource.filter(p => p.id !== id));
  };

  // 启用/禁用策略
  const handleToggleStatus = (record: WatermarkPolicy) => {
    const newStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    setDataSource(dataSource.map(p =>
      p.id === record.id ? { ...p, status: newStatus } : p
    ));
    message.success(`策略已${newStatus === 'ENABLED' ? '启用' : '禁用'}`);
  };

  // 测试水印
  const handleTest = (record: WatermarkPolicy) => {
    message.info(`正在为「${record.name}」生成测试水印...`);
    setTimeout(() => {
      message.success('测试水印已生成，请查看预览');
      setTestVisible(true);
    }, 500);
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingPolicy) {
        setDataSource(dataSource.map(p =>
          p.id === editingPolicy.id ? { ...p, ...values } : p
        ));
        message.success('策略更新成功');
      } else {
        const newPolicy: WatermarkPolicy = {
          id: `policy-${Date.now()}`,
          ...values,
          status: 'ENABLED',
          createdTime: new Date().toLocaleString(),
          assetCount: 0,
        };
        setDataSource([newPolicy, ...dataSource]);
        message.success('策略创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取策略类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      VISIBLE: { color: 'blue', label: '可见水印' },
      INVISIBLE: { color: 'purple', label: '隐水印' },
    };
    return config[type] || { color: 'default', label: type };
  };

  // 获取应用场景标签
  const getApplicationTag = (app: string) => {
    const config: Record<string, { color: string; label: string }> = {
      TEXT: { color: 'green', label: '水印文字' },
      LOGO: { color: 'cyan', label: 'Logo' },
      QRCODE: { color: 'orange', label: '二维码' },
    };
    return config[app] || { color: 'default', label: app };
  };

  // 表格列配置
  const columns: ColumnsType<WatermarkPolicy> = [
    {
      title: '策略名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          {name}
          <Tag color={record.status === 'ENABLED' ? 'green' : 'gray'}>
            {record.status === 'ENABLED' ? '启用' : '停用'}
          </Tag>
        </Space>
      ),
    },
    {
      title: '策略类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: string) => {
        const config = getTypeTag(type);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '应用场景',
      dataIndex: 'application',
      key: 'application',
      width: 100,
      render: (app: string) => {
        const config = getApplicationTag(app);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '适用范围',
      dataIndex: 'scope',
      key: 'scope',
      width: 100,
      render: (scope: string) => scope === 'GLOBAL' ? '全局' : '指定资产',
    },
    {
      title: '透明度',
      dataIndex: 'opacity',
      key: 'opacity',
      width: 100,
      render: (opacity: number) => `${opacity}%`,
    },
    {
      title: '已保护资产',
      dataIndex: 'assetCount',
      key: 'assetCount',
      width: 100,
      render: (count: number) => count || 0,
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<ExperimentOutlined />} onClick={() => handleTest(record)}>
            测试
          </Button>
          <Button type="text" size="small" icon={record.status === 'ENABLED' ? <PauseCircleOutlined /> : <PlayCircleOutlined />} onClick={() => handleToggleStatus(record)} />
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm title="确认删除该策略？" onConfirm={() => handleDelete(record.id)} okText="确认" cancelText="取消">
            <Button type="text" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      {/* 统计卡片 */}
      <Row gutter={16} className={styles.statsRow}>
        <Col span={8}>
          <Card><Statistic title="总水印策略数" value={mockPolicies.length} /></Card>
        </Col>
        <Col span={8}>
          <Card><Statistic title="已保护资产数" value={mockPolicies.reduce((sum, p) => sum + (p.assetCount || 0), 0)} valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
        <Col span={8}>
          <Card><Statistic title="今日溯源查询数" value={128} valueStyle={{ color: '#1890ff' }} /></Card>
        </Col>
      </Row>

      {/* 主表格 */}
      <Card
        title="水印策略列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增策略
          </Button>
        }
        className={styles.tableCard}
      >
        <div className={styles.searchBar}>
          <Search placeholder="搜索策略名称" onSearch={handleSearch} style={{ width: 300 }} allowClear />
        </div>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条策略` }}
        />
      </Card>

      {/* 新增/编辑策略弹窗 */}
      <Modal title={editingPolicy ? '编辑策略' : '新增策略'} open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)} okText="确认" cancelText="取消" width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="策略名称" rules={[{ required: true, message: '请输入策略名称' }]}>
            <Input placeholder="请输入策略名称" />
          </Form.Item>
          <Form.Item name="type" label="策略类型" rules={[{ required: true, message: '请选择策略类型' }]}>
            <Radio.Group>
              <Radio value="VISIBLE">可见水印</Radio>
              <Radio value="INVISIBLE">隐水印</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="application" label="应用场景" rules={[{ required: true, message: '请选择应用场景' }]}>
            <Select placeholder="请选择应用场景">
              <Select.Option value="TEXT">水印文字</Select.Option>
              <Select.Option value="LOGO">Logo</Select.Option>
              <Select.Option value="QRCODE">二维码</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="content" label="水印内容" rules={[{ required: true, message: '请输入水印内容' }]}>
            <Input placeholder="请输入水印内容" />
          </Form.Item>
          <Form.Item name="scope" label="适用范围" rules={[{ required: true, message: '请选择适用范围' }]}>
            <Radio.Group>
              <Radio value="GLOBAL">全局</Radio>
              <Radio value="SPECIFIC">指定资产</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="opacity" label="透明度" rules={[{ required: true, message: '请设置透明度' }]}>
            <Slider min={5} max={80} marks={{ 5: '5%', 25: '25%', 50: '50%', 80: '80%' }} />
          </Form.Item>
          <Form.Item name="position" label="水印位置">
            <Select placeholder="请选择水印位置">
              <Select.Option value="右下角">右下角</Select.Option>
              <Select.Option value="左下角">左下角</Select.Option>
              <Select.Option value="右上角">右上角</Select.Option>
              <Select.Option value="左上角">左上角</Select.Option>
              <Select.Option value="居中">居中</Select.Option>
              <Select.Option value="全屏">全屏</Select.Option>
              <Select.Option value="随机">随机</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 测试水印弹窗 */}
      <Modal title="水印测试预览" open={testVisible} onCancel={() => setTestVisible(false)} footer={null} width={800}>
        <div style={{ padding: '40px', background: '#f5f5f5', position: 'relative', minHeight: '300px' }}>
          <p style={{ color: '#666' }}>这是水印预览区域，模拟数据导出时的水印效果...</p>
          <div style={{ position: 'absolute', bottom: '20px', right: '20px', opacity: 0.3, fontSize: '14px', color: '#333' }}>
            © Enterprise Data System
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default WatermarkManagement;
