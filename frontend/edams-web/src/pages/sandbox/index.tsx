/**
 * 数据沙箱页面
 */

import React, { useState, useEffect, useCallback } from 'react';
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
  ReloadOutlined,
} from '@ant-design/icons';
import styles from './index.less';
import { sandbox } from '../../services';

interface SandboxRecord {
  id: string;
  name: string;
  status: 'RUNNING' | 'STOPPED' | 'CREATING' | 'FAILED';
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

  // 从API加载沙箱列表
  const loadSandboxes = useCallback(async () => {
    setLoading(true);
    try {
      const response = await sandbox.getSandboxList();
      if (response.success && response.data) {
        // 转换API数据格式为页面需要的格式
        const mappedData: SandboxRecord[] = (response.data.records || response.data).map((item: any) => ({
          id: String(item.id),
          name: item.name,
          status: item.status,
          resourceSpec: item.spec ? `${item.spec.cpu}核${item.spec.memory}G` : '4核8G',
          expireTime: item.expireTime,
          description: item.description,
          cpu: item.spec?.cpu || 4,
          memory: item.spec?.memory || 8,
          storage: item.spec?.disk || 100,
        }));
        setDataSource(mappedData);
      }
    } catch (error) {
      message.error('加载沙箱列表失败');
      console.error('Load sandboxes error:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSandboxes();
  }, [loadSandboxes]);

  // 获取状态标签配置
  const getStatusConfig = (status: string) => {
    const config: Record<string, { color: string; label: string; icon: React.ReactNode }> = {
      RUNNING: { color: 'green', label: '运行中', icon: <PlayCircleOutlined /> },
      STOPPED: { color: 'gray', label: '已停止', icon: <PauseCircleOutlined /> },
      CREATING: { color: 'blue', label: '创建中', icon: <ClockCircleOutlined /> },
      FAILED: { color: 'red', label: '失败', icon: <PauseCircleOutlined /> },
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
  const handleToggleStatus = async (record: SandboxRecord) => {
    try {
      if (record.status === 'RUNNING') {
        await sandbox.stopSandbox(record.id);
        message.success(`沙箱 "${record.name}" 已停止`);
      } else {
        await sandbox.startSandbox(record.id);
        message.success(`沙箱 "${record.name}" 已启动`);
      }
      // 刷新列表
      loadSandboxes();
    } catch (error) {
      message.error('操作失败');
      console.error('Toggle status error:', error);
    }
  };

  // 删除沙箱
  const handleDelete = async (id: string) => {
    try {
      await sandbox.deleteSandbox(id);
      message.success('删除成功');
      setDataSource(dataSource.filter((item) => item.id !== id));
    } catch (error) {
      message.error('删除失败');
      console.error('Delete error:', error);
    }
  };

  // 提交创建
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const spec = resourceSpecs.find((s) => s.value === values.resourceSpec);
      
      await sandbox.createSandbox({
        name: values.name,
        description: values.description,
        spec: {
          cpu: `${spec?.cpu || 4}`,
          memory: `${spec?.memory || 8}`,
          disk: `${spec?.storage || 100}`,
        },
      });
      
      message.success('沙箱创建中，请稍候...');
      setModalVisible(false);
      // 刷新列表
      loadSandboxes();
    } catch (error) {
      message.error('创建沙箱失败');
      console.error('Create sandbox error:', error);
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
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              创建沙箱
            </Button>
            <Button icon={<ReloadOutlined />} onClick={loadSandboxes}>
              刷新
            </Button>
          </Space>
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
