/**
 * 数据源配置页面
 */

import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  Row,
  Col,
  Drawer,
  Descriptions,
  Divider,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  DatabaseOutlined,
  ReloadOutlined,
  PlayCircleOutlined,
  StopOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Password } = Input;

interface Datasource {
  id: string;
  name: string;
  type: string;
  host: string;
  port: number;
  database: string;
  username: string;
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR';
  assetCount: number;
  description: string;
}

const SystemDatasources: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<Datasource[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [editingDatasource, setEditingDatasource] = useState<Datasource | null>(null);
  const [selectedDatasource, setSelectedDatasource] = useState<Datasource | null>(null);
  const [form] = Form.useForm();

  // 模拟数据源数据
  const mockDatasources: Datasource[] = [
    { id: '1', name: 'MySQL生产库', type: 'MYSQL', host: '192.168.1.100', port: 3306, database: 'edams_prod', username: 'edams_ro', status: 'CONNECTED', assetCount: 256, description: '生产环境MySQL数据库' },
    { id: '2', name: 'PostgreSQL数据仓库', type: 'POSTGRESQL', host: '192.168.1.101', port: 5432, database: 'warehouse', username: 'warehouse', status: 'CONNECTED', assetCount: 128, description: '数据仓库PostgreSQL' },
    { id: '3', name: 'Hive数据湖', type: 'HIVE', host: 'hadoop-master', port: 10000, database: 'default', username: 'hive', status: 'CONNECTED', assetCount: 512, description: '大数据Hive数据湖' },
    { id: '4', name: 'Kafka消息队列', type: 'KAFKA', host: '192.168.1.102', port: 9092, database: '-', username: '-', status: 'DISCONNECTED', assetCount: 45, description: 'Kafka消息队列' },
  ];

  const [datasources] = useState<Datasource[]>(mockDatasources);

  // 测试连接
  const handleTest = (record: Datasource) => {
    message.loading('正在测试连接...', 1.5).then(() => {
      message.success('连接成功');
    });
  };

  // 启用/禁用
  const handleToggle = (record: Datasource) => {
    setDataSource(dataSource.map(ds =>
      ds.id === record.id
        ? { ...ds, status: ds.status === 'CONNECTED' ? 'DISCONNECTED' : 'CONNECTED' }
        : ds
    ));
    message.success(`数据源已${record.status === 'CONNECTED' ? '停用' : '启用'}`);
  };

  // 新增数据源
  const handleAdd = () => {
    setEditingDatasource(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑数据源
  const handleEdit = (record: Datasource) => {
    setEditingDatasource(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 查看详情
  const handleView = (record: Datasource) => {
    setSelectedDatasource(record);
    setDrawerVisible(true);
  };

  // 删除数据源
  const handleDelete = (id: string) => {
    setDataSource(dataSource.filter(ds => ds.id !== id));
    message.success('删除成功');
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingDatasource) {
        setDataSource(dataSource.map(ds =>
          ds.id === editingDatasource.id ? { ...ds, ...values } : ds
        ));
        message.success('数据源更新成功');
      } else {
        const newDatasource: Datasource = {
          id: `ds-${Date.now()}`,
          ...values,
          status: 'DISCONNECTED',
          assetCount: 0,
        };
        setDataSource([...dataSource, newDatasource]);
        message.success('数据源创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      CONNECTED: { color: 'green', label: '已连接' },
      DISCONNECTED: { color: 'default', label: '未连接' },
      ERROR: { color: 'red', label: '连接错误' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 获取类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      MYSQL: { color: 'blue', label: 'MySQL' },
      POSTGRESQL: { color: 'green', label: 'PostgreSQL' },
      HIVE: { color: 'orange', label: 'Hive' },
      KAFKA: { color: 'purple', label: 'Kafka' },
      CLICKHOUSE: { color: 'cyan', label: 'ClickHouse' },
      ELASTICSEARCH: { color: 'magenta', label: 'Elasticsearch' },
    };
    return config[type] || { color: 'default', label: type };
  };

  // 表格列配置
  const columns: ColumnsType<Datasource> = [
    {
      title: '数据源名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <DatabaseOutlined style={{ color: '#1890ff' }} />
          <a onClick={() => handleView(record)}>{name}</a>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const config = getTypeTag(type);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '连接信息',
      key: 'connection',
      render: (_, record) => (
        <span style={{ fontSize: 12, color: '#8c8c8c' }}>
          {record.host}:{record.port}/{record.database}
        </span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = getStatusTag(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '资产数',
      dataIndex: 'assetCount',
      key: 'assetCount',
      width: 80,
      align: 'center',
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<PlayCircleOutlined />} onClick={() => handleTest(record)}>
            测试
          </Button>
          <Button
            type="text"
            size="small"
            icon={record.status === 'CONNECTED' ? <StopOutlined /> : <PlayCircleOutlined />}
            onClick={() => handleToggle(record)}
          >
            {record.status === 'CONNECTED' ? '停用' : '启用'}
          </Button>
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm
            title="确认删除该数据源？"
            onConfirm={() => handleDelete(record.id)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="text" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card
        title="数据源配置"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增数据源
          </Button>
        }
        className={styles.tableCard}
      >
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      {/* 新增/编辑数据源弹窗 */}
      <Modal
        title={editingDatasource ? '编辑数据源' : '新增数据源'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="数据源名称" rules={[{ required: true, message: '请输入数据源名称' }]}>
            <Input placeholder="请输入数据源名称" />
          </Form.Item>
          <Form.Item name="type" label="数据源类型" rules={[{ required: true, message: '请选择数据源类型' }]}>
            <Select placeholder="请选择数据源类型">
              <Select.Option value="MYSQL">MySQL</Select.Option>
              <Select.Option value="POSTGRESQL">PostgreSQL</Select.Option>
              <Select.Option value="HIVE">Hive</Select.Option>
              <Select.Option value="KAFKA">Kafka</Select.Option>
              <Select.Option value="CLICKHOUSE">ClickHouse</Select.Option>
              <Select.Option value="ELASTICSEARCH">Elasticsearch</Select.Option>
            </Select>
          </Form.Item>
          <Row gutter={16}>
            <Col span={16}>
              <Form.Item name="host" label="主机地址" rules={[{ required: true, message: '请输入主机地址' }]}>
                <Input placeholder="请输入主机地址" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="port" label="端口" rules={[{ required: true, message: '请输入端口' }]}>
                <Input type="number" placeholder="端口" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="database" label="数据库/实例">
            <Input placeholder="请输入数据库或实例名" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="username" label="用户名">
                <Input placeholder="请输入用户名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="password" label="密码">
                <Input.Password placeholder="请输入密码" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="请输入描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 数据源详情抽屉 */}
      <Drawer
        title="数据源详情"
        placement="right"
        width={450}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
      >
        {selectedDatasource && (
          <>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="数据源名称">{selectedDatasource.name}</Descriptions.Item>
              <Descriptions.Item label="类型">
                {(() => {
                  const config = getTypeTag(selectedDatasource.type);
                  return <Tag color={config.color}>{config.label}</Tag>;
                })()}
              </Descriptions.Item>
              <Descriptions.Item label="状态">
                {(() => {
                  const config = getStatusTag(selectedDatasource.status);
                  return <Tag color={config.color}>{config.label}</Tag>;
                })()}
              </Descriptions.Item>
              <Descriptions.Item label="主机地址">{selectedDatasource.host}</Descriptions.Item>
              <Descriptions.Item label="端口">{selectedDatasource.port}</Descriptions.Item>
              <Descriptions.Item label="数据库">{selectedDatasource.database}</Descriptions.Item>
              <Descriptions.Item label="用户名">{selectedDatasource.username}</Descriptions.Item>
              <Descriptions.Item label="资产数">{selectedDatasource.assetCount}</Descriptions.Item>
              <Descriptions.Item label="描述">{selectedDatasource.description}</Descriptions.Item>
            </Descriptions>

            <Divider />

            <Space style={{ width: '100%' }}>
              <Button icon={<PlayCircleOutlined />} onClick={() => handleTest(selectedDatasource)}>
                测试连接
              </Button>
              <Button icon={<EditOutlined />} onClick={() => {
                setDrawerVisible(false);
                handleEdit(selectedDatasource);
              }}>
                编辑
              </Button>
            </Space>
          </>
        )}
      </Drawer>
    </div>
  );
};

export default SystemDatasources;
