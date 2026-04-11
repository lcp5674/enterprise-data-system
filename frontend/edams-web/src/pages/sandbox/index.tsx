/**
 * 数据沙箱页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Select,
  Input,
  Row,
  Col,
  Statistic,
  message,
  Popconfirm,
  Tooltip,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  DeleteOutlined,
  DatabaseOutlined,
  ClockCircleOutlined,
  DesktopOutlined,
} from '@ant-design/icons';
import styles from './index.less';

interface SandboxRecord {
  id: string;
  name: string;
  status: 'RUNNING' | 'STOPPED' | 'CREATING';
  resourceSpec: string;
  expireTime: string;
  description?: string;
  cpu: number;
  memory: number;
  storage: number;
}

const SandboxManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<SandboxRecord[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 资源规格选项
  const resourceSpecs = [
    { label: '小型 (2核4G)', value: 'small', cpu: 2, memory: 4, storage: 50 },
    { label: '中型 (4核8G)', value: 'medium', cpu: 4, memory: 8, storage: 100 },
    { label: '大型 (8核16G)', value: 'large', cpu: 8, memory: 16, storage: 200 },
    { label: '超大型 (16核32G)', value: 'xlarge', cpu: 16, memory: 32, storage: 500 },
  ];

  // Mock数据
  const mockData: SandboxRecord[] = [
    {
      id: '1',
      name: '订单分析沙箱',
      status: 'RUNNING',
      resourceSpec: '4核8G',
      expireTime: '2026-05-11',
      description: '用于订单数据分析测试',
      cpu: 4,
      memory: 8,
      storage: 100,
    },
    {
      id: '2',
      name: '用户画像沙箱',
      status: 'RUNNING',
      resourceSpec: '8核16G',
      expireTime: '2026-05-15',
      description: '用户画像模型训练环境',
      cpu: 8,
      memory: 16,
      storage: 200,
    },
    {
      id: '3',
      name: '报表测试沙箱',
      status: 'STOPPED',
      resourceSpec: '2核4G',
      expireTime: '2026-04-20',
      description: '财务报表功能测试',
      cpu: 2,
      memory: 4,
      storage: 50,
    },
    {
      id: '4',
      name: '数据脱敏沙箱',
      status: 'CREATING',
      resourceSpec: '4核8G',
      expireTime: '2026-05-01',
      description: '敏感数据脱敏处理环境',
      cpu: 4,
      memory: 8,
      storage: 100,
    },
    {
      id: '5',
      name: 'ETL测试沙箱',
      status: 'RUNNING',
      resourceSpec: '16核32G',
      expireTime: '2026-06-01',
      description: '大规模ETL作业测试',
      cpu: 16,
      memory: 32,
      storage: 500,
    },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockData);
      setLoading(false);
    }, 500);
  }, []);

  // 获取状态标签配置
  const getStatusConfig = (status: string) => {
    const config: Record<string, { color: string; label: string; icon: React.ReactNode }> = {
      RUNNING: { color: 'green', label: '运行中', icon: <PlayCircleOutlined /> },
      STOPPED: { color: 'gray', label: '已停止', icon: <PauseCircleOutlined /> },
      CREATING: { color: 'blue', label: '创建中', icon: <ClockCircleOutlined /> },
    };
    return config[status] || { color: 'default', label: status, icon: null };
  };

  // 获取资源规格显示
  const getResourceSpecDisplay = (spec: string) => {
    return <Tag icon={<DesktopOutlined />}>{spec}</Tag>;
  };

  // 统计数据
  const stats = {
    total: dataSource.length,
    running: dataSource.filter((item) => item.status === 'RUNNING').length,
    stopped: dataSource.filter((item) => item.status === 'STOPPED').length,
    creating: dataSource.filter((item) => item.status === 'CREATING').length,
  };

  // 创建沙箱
  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  // 启动/停止沙箱
  const handleToggleStatus = (record: SandboxRecord) => {
    const newStatus = record.status === 'RUNNING' ? 'STOPPED' : 'RUNNING';
    setDataSource(
      dataSource.map((item) =>
        item.id === record.id ? { ...item, status: newStatus } : item
      )
    );
    message.success(`沙箱 "${record.name}" 已${newStatus === 'RUNNING' ? '启动' : '停止'}`);
  };

  // 删除沙箱
  const handleDelete = (id: string) => {
    message.success('删除成功');
    setDataSource(dataSource.filter((item) => item.id !== id));
  };

  // 提交创建
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const spec = resourceSpecs.find((s) => s.value === values.resourceSpec);
      
      const newRecord: SandboxRecord = {
        id: `sandbox-${Date.now()}`,
        name: values.name,
        status: 'CREATING',
        resourceSpec: spec ? `${spec.cpu}核${spec.memory}G` : '4核8G',
        expireTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        description: values.description,
        cpu: spec?.cpu || 4,
        memory: spec?.memory || 8,
        storage: spec?.storage || 100,
      };
      
      setDataSource([newRecord, ...dataSource]);
      message.success('沙箱创建中，请稍候...');
      setModalVisible(false);
      
      // 模拟创建完成
      setTimeout(() => {
        setDataSource((prev) =>
          prev.map((item) =>
            item.id === newRecord.id ? { ...item, status: 'RUNNING' } : item
          )
        );
        message.success(`沙箱 "${newRecord.name}" 创建完成`);
      }, 3000);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 表格列配置
  const columns: ColumnsType<SandboxRecord> = [
    {
      title: '沙箱名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <DatabaseOutlined />
          {name}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => {
        const config = getStatusConfig(status);
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.label}
          </Tag>
        );
      },
    },
    {
      title: '资源规格',
      dataIndex: 'resourceSpec',
      key: 'resourceSpec',
      width: 120,
      render: (spec: string) => getResourceSpecDisplay(spec),
    },
    {
      title: '到期时间',
      dataIndex: 'expireTime',
      key: 'expireTime',
      width: 150,
      render: (time: string) => (
        <Space>
          <ClockCircleOutlined />
          {time}
        </Space>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          {record.status !== 'CREATING' && (
            <Tooltip title={record.status === 'RUNNING' ? '停止' : '启动'}>
              <Button
                type="text"
                size="small"
                icon={
                  record.status === 'RUNNING' ? (
                    <PauseCircleOutlined />
                  ) : (
                    <PlayCircleOutlined />
                  )
                }
                onClick={() => handleToggleStatus(record)}
              >
                {record.status === 'RUNNING' ? '停止' : '启动'}
              </Button>
            </Tooltip>
          )}
          <Popconfirm
            title="确认删除该沙箱？"
            description="删除后数据将无法恢复"
            onConfirm={() => handleDelete(record.id)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="text" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card className={styles.mainCard}>
        {/* 工具栏 */}
        <div className={styles.toolbar}>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            创建沙箱
          </Button>
        </div>

        {/* 主表格 */}
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 个沙箱`,
          }}
        />

        {/* 底部统计 */}
        <div className={styles.statsSection}>
          <Row gutter={16}>
            <Col span={12}>
              <Card className={styles.statCard} size="small">
                <Statistic
                  title="我的沙箱总数"
                  value={stats.total}
                  prefix={<DatabaseOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>
            <Col span={12}>
              <Card className={styles.statCard} size="small">
                <Statistic
                  title="运行中数量"
                  value={stats.running}
                  prefix={<PlayCircleOutlined />}
                  valueStyle={{ color: '#52c41a' }}
                />
              </Card>
            </Col>
          </Row>
        </div>
      </Card>

      {/* 创建沙箱弹窗 */}
      <Modal
        title="创建沙箱"
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="创建"
        cancelText="取消"
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="沙箱名称"
            rules={[{ required: true, message: '请输入沙箱名称' }]}
          >
            <Input placeholder="请输入沙箱名称" />
          </Form.Item>
          <Form.Item
            name="resourceSpec"
            label="资源规格"
            rules={[{ required: true, message: '请选择资源规格' }]}
            initialValue="medium"
          >
            <Select
              placeholder="请选择资源规格"
              options={resourceSpecs.map((spec) => ({
                label: spec.label,
                value: spec.value,
              }))}
            />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入沙箱描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SandboxManagement;
