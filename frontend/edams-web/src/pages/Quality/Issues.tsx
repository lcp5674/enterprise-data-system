/**
 * 问题追踪页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
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
  Row,
  Col,
  Drawer,
  Descriptions,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EditOutlined,
} from '@ant-design/icons';
import * as qualityService from '../../services/quality';
import type { QualityIssue } from '../../types';
import styles from './index.less';

const { Search } = Input;

const QualityIssues: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<QualityIssue[]>([]);
  const [selectedIssue, setSelectedIssue] = useState<QualityIssue | null>(null);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 模拟数据
  const mockIssues: QualityIssue[] = [
    { id: '1', assetName: 'dim_users', ruleName: '空值检测', severity: 'HIGH', status: 'OPEN', description: 'phone字段存在空值', createTime: '2026-04-10 09:00:00' },
    { id: '2', assetName: 'fact_orders', ruleName: '唯一性检测', severity: 'HIGH', status: 'PROCESSING', description: 'order_id存在重复', createTime: '2026-04-09 14:00:00', assignee: '张三' },
    { id: '3', assetName: 'dim_products', ruleName: '格式校验', severity: 'MEDIUM', status: 'RESOLVED', description: 'sku_code格式不规范', createTime: '2026-04-08 11:00:00', assignee: '李四' },
    { id: '4', assetName: 'rpt_sales', ruleName: '值域校验', severity: 'LOW', status: 'OPEN', description: 'amount字段存在负值', createTime: '2026-04-07 16:00:00' },
    { id: '5', assetName: 'api_user_info', ruleName: '一致性检测', severity: 'MEDIUM', status: 'OPEN', description: '与用户中心数据不一致', createTime: '2026-04-06 10:00:00' },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockIssues);
      setLoading(false);
    }, 500);
  }, []);

  // 搜索
  const handleSearch = (value: string) => {
    if (!value) {
      setDataSource(mockIssues);
      return;
    }
    setDataSource(mockIssues.filter(issue => issue.assetName.includes(value)));
  };

  // 查看详情
  const handleView = (record: QualityIssue) => {
    setSelectedIssue(record);
    setDrawerVisible(true);
  };

  // 认领问题
  const handleAssign = async (record: QualityIssue) => {
    setSelectedIssue(record);
    form.setFieldsValue({ issueId: record.id });
    setModalVisible(true);
  };

  // 解决/关闭问题
  const handleResolve = (record: QualityIssue) => {
    setDataSource(dataSource.map(issue =>
      issue.id === record.id ? { ...issue, status: 'RESOLVED' } : issue
    ));
    message.success('问题已标记为已解决');
  };

  // 提交处理
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setDataSource(dataSource.map(issue =>
        issue.id === values.issueId ? { ...issue, status: 'PROCESSING', assignee: values.assignee } : issue
      ));
      message.success('问题已分配');
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取严重程度标签
  const getSeverityTag = (severity: string) => {
    const config: Record<string, { color: string; label: string }> = {
      HIGH: { color: 'red', label: '高' },
      MEDIUM: { color: 'orange', label: '中' },
      LOW: { color: 'green', label: '低' },
    };
    return config[severity] || { color: 'default', label: severity };
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      OPEN: { color: 'red', label: '待处理' },
      PROCESSING: { color: 'blue', label: '处理中' },
      RESOLVED: { color: 'green', label: '已解决' },
      CLOSED: { color: 'default', label: '已关闭' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 表格列配置
  const columns: ColumnsType<QualityIssue> = [
    {
      title: '资产名称',
      dataIndex: 'assetName',
      key: 'assetName',
      render: (name: string) => <a>{name}</a>,
    },
    {
      title: '规则名称',
      dataIndex: 'ruleName',
      key: 'ruleName',
    },
    {
      title: '严重程度',
      dataIndex: 'severity',
      key: 'severity',
      width: 100,
      render: (severity: string) => {
        const config = getSeverityTag(severity);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
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
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '负责人',
      dataIndex: 'assignee',
      key: 'assignee',
      width: 100,
      render: (assignee: string) => assignee || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleView(record)}>
            详情
          </Button>
          {record.status === 'OPEN' && (
            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleAssign(record)}>
              认领
            </Button>
          )}
          {record.status !== 'RESOLVED' && (
            <Popconfirm
              title="确认已解决该问题？"
              onConfirm={() => handleResolve(record)}
              okText="确认"
              cancelText="取消"
            >
              <Button type="text" size="small" icon={<CheckCircleOutlined />}>
                解决
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card
        title="问题追踪"
        extra={
          <Space>
            <Select placeholder="状态筛选" style={{ width: 120 }} allowClear>
              <Select.Option value="OPEN">待处理</Select.Option>
              <Select.Option value="PROCESSING">处理中</Select.Option>
              <Select.Option value="RESOLVED">已解决</Select.Option>
            </Select>
            <Select placeholder="严重程度" style={{ width: 120 }} allowClear>
              <Select.Option value="HIGH">高</Select.Option>
              <Select.Option value="MEDIUM">中</Select.Option>
              <Select.Option value="LOW">低</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={() => setDataSource(mockIssues)}>
              刷新
            </Button>
          </Space>
        }
        className={styles.tableCard}
      >
        <div className={styles.searchBar}>
          <Search placeholder="搜索资产名称" onSearch={handleSearch} style={{ width: 300 }} allowClear />
        </div>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 个问题`,
          }}
        />
      </Card>

      {/* 问题详情抽屉 */}
      <Drawer
        title="问题详情"
        placement="right"
        width={400}
        open={drawerVisible}
        onClose={() => setDrawerVisible(false)}
      >
        {selectedIssue && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="资产名称">{selectedIssue.assetName}</Descriptions.Item>
            <Descriptions.Item label="规则名称">{selectedIssue.ruleName}</Descriptions.Item>
            <Descriptions.Item label="严重程度">
              {(() => {
                const config = getSeverityTag(selectedIssue.severity);
                return <Tag color={config.color}>{config.label}</Tag>;
              })()}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              {(() => {
                const config = getStatusTag(selectedIssue.status);
                return <Tag color={config.color}>{config.label}</Tag>;
              })()}
            </Descriptions.Item>
            <Descriptions.Item label="描述">{selectedIssue.description}</Descriptions.Item>
            <Descriptions.Item label="负责人">{selectedIssue.assignee || '-'}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{selectedIssue.createTime}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>

      {/* 认领问题弹窗 */}
      <Modal
        title="认领问题"
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="issueId" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="assignee" label="负责人" rules={[{ required: true, message: '请选择负责人' }]}>
            <Select placeholder="请选择负责人">
              <Select.Option value="张三">张三</Select.Option>
              <Select.Option value="李四">李四</Select.Option>
              <Select.Option value="王五">王五</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default QualityIssues;
