/**
 * 质量规则页面
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
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
} from '@ant-design/icons';
import * as qualityService from '../../services/quality';
import type { QualityRule } from '../../types';
import styles from './index.less';

const { Search } = Input;

const QualityRules: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<QualityRule[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRule, setEditingRule] = useState<QualityRule | null>(null);
  const [form] = Form.useForm();

  // 模拟数据
  const mockRules: QualityRule[] = [
    { id: '1', name: '空值检测', type: 'NULL_CHECK', description: '检测字段是否为空', status: 'ENABLED', severity: 'HIGH', lastCheckTime: '2026-04-10 10:00:00' },
    { id: '2', name: '唯一性检测', type: 'UNIQUE_CHECK', description: '检测字段值是否唯一', status: 'ENABLED', severity: 'MEDIUM', lastCheckTime: '2026-04-10 10:00:00' },
    { id: '3', name: '格式校验', type: 'FORMAT_CHECK', description: '检测字段格式是否正确', status: 'ENABLED', severity: 'LOW', lastCheckTime: '2026-04-10 10:00:00' },
    { id: '4', name: '值域校验', type: 'RANGE_CHECK', description: '检测字段值是否在合理范围内', status: 'DISABLED', severity: 'MEDIUM', lastCheckTime: '2026-04-09 10:00:00' },
    { id: '5', name: '一致性检测', type: 'CONSISTENCY_CHECK', description: '检测跨表数据一致性', status: 'ENABLED', severity: 'HIGH', lastCheckTime: '2026-04-10 10:00:00' },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockRules);
      setLoading(false);
    }, 500);
  }, []);

  // 搜索
  const handleSearch = (value: string) => {
    if (!value) {
      setDataSource(mockRules);
      return;
    }
    setDataSource(mockRules.filter(rule => rule.name.includes(value)));
  };

  // 新增规则
  const handleAdd = () => {
    setEditingRule(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑规则
  const handleEdit = (record: QualityRule) => {
    setEditingRule(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 删除规则
  const handleDelete = async (id: string) => {
    message.success('删除成功');
    setDataSource(dataSource.filter(rule => rule.id !== id));
  };

  // 启用/禁用规则
  const handleToggleStatus = (record: QualityRule) => {
    const newStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    setDataSource(dataSource.map(rule =>
      rule.id === record.id ? { ...rule, status: newStatus } : rule
    ));
    message.success(`规则已${newStatus === 'ENABLED' ? '启用' : '禁用'}`);
  };

  // 手动执行规则
  const handleExecute = async (record: QualityRule) => {
    try {
      await qualityService.triggerCheck({ ruleId: record.id });
      message.success('规则执行已触发');
    } catch (error) {
      message.error('执行失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRule) {
        setDataSource(dataSource.map(rule =>
          rule.id === editingRule.id ? { ...rule, ...values } : rule
        ));
        message.success('规则更新成功');
      } else {
        const newRule: QualityRule = {
          id: `rule-${Date.now()}`,
          ...values,
          status: 'ENABLED',
          lastCheckTime: '-',
        };
        setDataSource([newRule, ...dataSource]);
        message.success('规则创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取规则类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      NULL_CHECK: { color: 'blue', label: '空值检测' },
      UNIQUE_CHECK: { color: 'green', label: '唯一性' },
      FORMAT_CHECK: { color: 'purple', label: '格式校验' },
      RANGE_CHECK: { color: 'orange', label: '值域校验' },
      CONSISTENCY_CHECK: { color: 'cyan', label: '一致性' },
    };
    return config[type] || { color: 'default', label: type };
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

  // 表格列配置
  const columns: ColumnsType<QualityRule> = [
    {
      title: '规则名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          {name}
          <Tag color={record.status === 'ENABLED' ? 'green' : 'default'}>
            {record.status === 'ENABLED' ? '已启用' : '已禁用'}
          </Tag>
        </Space>
      ),
    },
    {
      title: '规则类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const config = getTypeTag(type);
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
      title: '最近检查',
      dataIndex: 'lastCheckTime',
      key: 'lastCheckTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<PlayCircleOutlined />}
            onClick={() => handleExecute(record)}
          >
            执行
          </Button>
          <Button
            type="text"
            size="small"
            icon={record.status === 'ENABLED' ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
            onClick={() => handleToggleStatus(record)}
          />
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="确认删除该规则？"
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
        title="质量规则"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增规则
          </Button>
        }
        className={styles.tableCard}
      >
        <div className={styles.searchBar}>
          <Search
            placeholder="搜索规则名称"
            onSearch={handleSearch}
            style={{ width: 300 }}
            allowClear
          />
        </div>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 条规则`,
          }}
        />
      </Card>

      {/* 新增/编辑规则弹窗 */}
      <Modal
        title={editingRule ? '编辑规则' : '新增规则'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="规则名称"
            rules={[{ required: true, message: '请输入规则名称' }]}
          >
            <Input placeholder="请输入规则名称" />
          </Form.Item>
          <Form.Item
            name="type"
            label="规则类型"
            rules={[{ required: true, message: '请选择规则类型' }]}
          >
            <Select placeholder="请选择规则类型">
              <Select.Option value="NULL_CHECK">空值检测</Select.Option>
              <Select.Option value="UNIQUE_CHECK">唯一性检测</Select.Option>
              <Select.Option value="FORMAT_CHECK">格式校验</Select.Option>
              <Select.Option value="RANGE_CHECK">值域校验</Select.Option>
              <Select.Option value="CONSISTENCY_CHECK">一致性检测</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="severity"
            label="严重程度"
            rules={[{ required: true, message: '请选择严重程度' }]}
          >
            <Select placeholder="请选择严重程度">
              <Select.Option value="HIGH">高</Select.Option>
              <Select.Option value="MEDIUM">中</Select.Option>
              <Select.Option value="LOW">低</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入规则描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default QualityRules;
