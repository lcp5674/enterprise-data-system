/**
 * 指标管理页面
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
  Select,
  Modal,
  Form,
  message,
  Popconfirm,
  Drawer,
  Descriptions,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Search } = Input;

interface Metric {
  id: string;
  name: string;
  code: string;
  type: 'BASIC' | 'DERIVED' | 'REAL_TIME';
  datasource: string;
  updateFrequency: string;
  status: 'ENABLED' | 'DISABLED';
  description?: string;
  owner?: string;
  lastUpdateTime?: string;
}

const MetricManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Metric[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editingMetric, setEditingMetric] = useState<Metric | null>(null);
  const [selectedMetric, setSelectedMetric] = useState<Metric | null>(null);
  const [form] = Form.useForm();

  // 模拟数据
  const mockMetrics: Metric[] = [
    { id: '1', name: '日活跃用户数', code: 'DAU', type: 'REAL_TIME', datasource: 'Hive-ODS层', updateFrequency: '实时', status: 'ENABLED', description: '统计每日活跃用户数量', owner: '数据平台组', lastUpdateTime: '2026-04-11 10:00:00' },
    { id: '2', name: '订单转化率', code: 'ORDER_CONV_RATE', type: 'DERIVED', datasource: 'ClickHouse-DWD层', updateFrequency: '每小时', status: 'ENABLED', description: '订单数/访问数', owner: '运营分析组', lastUpdateTime: '2026-04-11 09:00:00' },
    { id: '3', name: '平均订单金额', code: 'AVG_ORDER_AMT', type: 'BASIC', datasource: 'MySQL-订单库', updateFrequency: '每日', status: 'ENABLED', description: '统计平均每笔订单金额', owner: '财务组', lastUpdateTime: '2026-04-10 23:00:00' },
    { id: '4', name: '库存周转天数', code: 'INVENTORY_TURNOVER', type: 'DERIVED', datasource: 'Oracle-库存系统', updateFrequency: '每日', status: 'DISABLED', description: '平均库存量/日均销量', owner: '供应链组', lastUpdateTime: '2026-04-10 08:00:00' },
    { id: '5', name: 'API调用成功率', code: 'API_SUCCESS_RATE', type: 'REAL_TIME', datasource: 'Kafka-网关日志', updateFrequency: '实时', status: 'ENABLED', description: '成功调用次数/总调用次数', owner: '基础架构组', lastUpdateTime: '2026-04-11 10:30:00' },
    { id: '6', name: '用户留存率', code: 'USER_RETENTION_RATE', type: 'DERIVED', datasource: 'Hive-ODS层', updateFrequency: '每日', status: 'ENABLED', description: '次日/7日/30日留存率', owner: '产品组', lastUpdateTime: '2026-04-10 00:00:00' },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockMetrics);
      setLoading(false);
    }, 500);
  }, []);

  // 搜索
  const handleSearch = (value: string) => {
    if (!value) {
      setDataSource(mockMetrics);
      return;
    }
    setDataSource(mockMetrics.filter(m => m.name.includes(value) || m.code.includes(value)));
  };

  // 新增指标
  const handleAdd = () => {
    setEditingMetric(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑指标
  const handleEdit = (record: Metric) => {
    setEditingMetric(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 查看详情
  const handleViewDetail = (record: Metric) => {
    setSelectedMetric(record);
    setDetailVisible(true);
  };

  // 删除指标
  const handleDelete = async (id: string) => {
    message.success('删除成功');
    setDataSource(dataSource.filter(m => m.id !== id));
  };

  // 启用/禁用指标
  const handleToggleStatus = (record: Metric) => {
    const newStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    setDataSource(dataSource.map(m =>
      m.id === record.id ? { ...m, status: newStatus } : m
    ));
    message.success(`指标已${newStatus === 'ENABLED' ? '启用' : '禁用'}`);
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingMetric) {
        setDataSource(dataSource.map(m =>
          m.id === editingMetric.id ? { ...m, ...values } : m
        ));
        message.success('指标更新成功');
      } else {
        const newMetric: Metric = {
          id: `metric-${Date.now()}`,
          ...values,
          status: 'ENABLED',
          lastUpdateTime: '-',
        };
        setDataSource([newMetric, ...dataSource]);
        message.success('指标创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取指标类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      BASIC: { color: 'blue', label: '基础指标' },
      DERIVED: { color: 'purple', label: '衍生指标' },
      REAL_TIME: { color: 'red', label: '实时指标' },
    };
    return config[type] || { color: 'default', label: type };
  };

  // 表格列配置
  const columns: ColumnsType<Metric> = [
    {
      title: '指标名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          {name}
          <Tag color={record.status === 'ENABLED' ? 'green' : 'default'}>
            {record.status === 'ENABLED' ? '启用' : '停用'}
          </Tag>
        </Space>
      ),
    },
    {
      title: '指标编码',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: '指标类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const config = getTypeTag(type);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '数据源',
      dataIndex: 'datasource',
      key: 'datasource',
      width: 180,
      ellipsis: true,
    },
    {
      title: '更新频率',
      dataIndex: 'updateFrequency',
      key: 'updateFrequency',
      width: 100,
    },
    {
      title: '负责人',
      dataIndex: 'owner',
      key: 'owner',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          <Button type="text" size="small" icon={record.status === 'ENABLED' ? <PauseCircleOutlined /> : <PlayCircleOutlined />} onClick={() => handleToggleStatus(record)} />
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm title="确认删除该指标？" onConfirm={() => handleDelete(record.id)} okText="确认" cancelText="取消">
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
        <Col span={6}>
          <Card><Statistic title="总指标数" value={mockMetrics.length} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="活跃指标" value={mockMetrics.filter(m => m.status === 'ENABLED').length} valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="监控告警" value={2} valueStyle={{ color: '#faad14' }} suffix="个" /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="关联资产数" value={28} suffix="个" /></Card>
        </Col>
      </Row>

      {/* 主表格 */}
      <Card
        title="指标列表"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增指标
          </Button>
        }
        className={styles.tableCard}
      >
        <div className={styles.searchBar}>
          <Search placeholder="搜索指标名称或编码" onSearch={handleSearch} style={{ width: 300 }} allowClear />
        </div>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条指标` }}
        />
      </Card>

      {/* 新增/编辑指标弹窗 */}
      <Modal title={editingMetric ? '编辑指标' : '新增指标'} open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)} okText="确认" cancelText="取消">
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="指标名称" rules={[{ required: true, message: '请输入指标名称' }]}>
            <Input placeholder="请输入指标名称" />
          </Form.Item>
          <Form.Item name="code" label="指标编码" rules={[{ required: true, message: '请输入指标编码' }]}>
            <Input placeholder="请输入指标编码，如：DAU" />
          </Form.Item>
          <Form.Item name="type" label="指标类型" rules={[{ required: true, message: '请选择指标类型' }]}>
            <Select placeholder="请选择指标类型">
              <Select.Option value="BASIC">基础指标</Select.Option>
              <Select.Option value="DERIVED">衍生指标</Select.Option>
              <Select.Option value="REAL_TIME">实时指标</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="datasource" label="数据源" rules={[{ required: true, message: '请输入数据源' }]}>
            <Input placeholder="请输入数据源，如：Hive-ODS层" />
          </Form.Item>
          <Form.Item name="updateFrequency" label="更新频率">
            <Select placeholder="请选择更新频率">
              <Select.Option value="实时">实时</Select.Option>
              <Select.Option value="每小时">每小时</Select.Option>
              <Select.Option value="每日">每日</Select.Option>
              <Select.Option value="每周">每周</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="owner" label="负责人">
            <Input placeholder="请输入负责人" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入指标描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 详情抽屉 */}
      <Drawer title="指标详情" open={detailVisible} onClose={() => setDetailVisible(false)} width={500}>
        {selectedMetric && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="指标名称">{selectedMetric.name}</Descriptions.Item>
            <Descriptions.Item label="指标编码">{selectedMetric.code}</Descriptions.Item>
            <Descriptions.Item label="指标类型">{getTypeTag(selectedMetric.type).label}</Descriptions.Item>
            <Descriptions.Item label="数据源">{selectedMetric.datasource}</Descriptions.Item>
            <Descriptions.Item label="更新频率">{selectedMetric.updateFrequency}</Descriptions.Item>
            <Descriptions.Item label="负责人">{selectedMetric.owner || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{selectedMetric.status === 'ENABLED' ? '启用' : '停用'}</Descriptions.Item>
            <Descriptions.Item label="最近更新时间">{selectedMetric.lastUpdateTime}</Descriptions.Item>
            <Descriptions.Item label="描述">{selectedMetric.description || '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default MetricManagement;
