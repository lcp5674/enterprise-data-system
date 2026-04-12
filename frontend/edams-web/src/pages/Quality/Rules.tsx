/**
 * 质量规则页面
 */

import React, { useState, useEffect, useCallback } from 'react';
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
  InputNumber,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { http } from '@/services/request';
import { API_PATHS } from '@/constants';
import styles from './index.less';

const { Search } = Input;
const { TextArea } = Input;

// 规则类型定义
interface QualityRule {
  id: number;
  name: string;
  code: string;
  type: string;
  typeText: string;
  severity: string;
  severityText: string;
  description?: string;
  status: number;
  statusText: string;
  checkExpression?: string;
  tableName?: string;
  columnName?: string;
  lastCheckTime?: string;
  passRate?: number;
  createdTime?: string;
  updatedTime?: string;
}

const QualityRules: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<QualityRule[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRule, setEditingRule] = useState<QualityRule | null>(null);
  const [form] = Form.useForm();
  const [searchKeyword, setSearchKeyword] = useState('');
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

  // 加载规则列表
  const loadRules = useCallback(async (page = 1, pageSize = 10, keyword = '') => {
    setLoading(true);
    try {
      const result = await http.get<any>(API_PATHS.QUALITY.RULES.LIST, {
        pageNum: page,
        pageSize: pageSize,
        keyword: keyword || undefined,
      });
      const records = Array.isArray(result) ? result : (result?.records || []);
      setDataSource(records.map((item: any) => ({
        ...item,
        typeText: getTypeText(item.type),
        severityText: getSeverityText(item.severity),
        statusText: item.status === 1 ? '已启用' : '已禁用',
      })));
      setPagination({
        current: page,
        pageSize: pageSize,
        total: result?.total || records.length,
      });
    } catch (error) {
      message.error('加载规则列表失败');
      console.error('加载规则列表失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRules(pagination.current, pagination.pageSize, searchKeyword);
  }, []);

  // 获取规则类型文本
  const getTypeText = (type: string): string => {
    const typeMap: Record<string, string> = {
      NULL_CHECK: '空值检测',
      UNIQUE_CHECK: '唯一性检测',
      FORMAT_CHECK: '格式校验',
      RANGE_CHECK: '值域校验',
      CONSISTENCY_CHECK: '一致性检测',
      REGEX_CHECK: '正则校验',
      LENGTH_CHECK: '长度校验',
    };
    return typeMap[type] || type;
  };

  // 获取严重程度文本
  const getSeverityText = (severity: string): string => {
    const severityMap: Record<string, string> = {
      HIGH: '高',
      MEDIUM: '中',
      LOW: '低',
    };
    return severityMap[severity] || severity;
  };

  // 搜索
  const handleSearch = (value: string) => {
    setSearchKeyword(value);
    loadRules(1, pagination.pageSize, value);
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
  const handleDelete = async (id: number) => {
    try {
      await http.delete(`${API_PATHS.QUALITY.RULES.DETAIL(String(id))}`);
      message.success('删除成功');
      loadRules(pagination.current, pagination.pageSize, searchKeyword);
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 启用规则
  const handleEnable = async (id: number) => {
    try {
      await http.put(`${API_PATHS.QUALITY.RULES.ENABLE(String(id))}`);
      message.success('规则已启用');
      loadRules(pagination.current, pagination.pageSize, searchKeyword);
    } catch (error) {
      message.error('启用失败');
    }
  };

  // 禁用规则
  const handleDisable = async (id: number) => {
    try {
      await http.put(`${API_PATHS.QUALITY.RULES.DISABLE(String(id))}`);
      message.success('规则已禁用');
      loadRules(pagination.current, pagination.pageSize, searchKeyword);
    } catch (error) {
      message.error('禁用失败');
    }
  };

  // 手动执行规则
  const handleExecute = async (record: QualityRule) => {
    try {
      await http.post(API_PATHS.QUALITY.CHECK.TRIGGER, { ruleId: record.id });
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
        // 更新
        await http.put(API_PATHS.QUALITY.RULES.UPDATE(String(editingRule.id)), values);
        message.success('规则更新成功');
      } else {
        // 创建
        await http.post(API_PATHS.QUALITY.RULES.CREATE, values);
        message.success('规则创建成功');
      }
      setModalVisible(false);
      loadRules(pagination.current, pagination.pageSize, searchKeyword);
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 分页变化
  const handlePageChange = (page: number, pageSize: number) => {
    setPagination({ ...pagination, current: page, pageSize });
    loadRules(page, pageSize, searchKeyword);
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
          <Tag color={record.status === 1 ? 'green' : 'default'}>
            {record.statusText}
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
      title: '通过率',
      dataIndex: 'passRate',
      key: 'passRate',
      width: 100,
      render: (rate: number) => rate ? `${rate}%` : '-',
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
          <Button type="text" size="small" icon={<PlayCircleOutlined />} onClick={() => handleExecute(record)}>
            执行
          </Button>
          {record.status === 1 ? (
            <Button type="text" size="small" icon={<PauseCircleOutlined />} onClick={() => handleDisable(record.id)} />
          ) : (
            <Button type="text" size="small" icon={<PlayCircleOutlined />} onClick={() => handleEnable(record.id)} />
          )}
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          <Popconfirm title="确认删除该规则？" onConfirm={() => handleDelete(record.id)} okText="确认" cancelText="取消">
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
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => loadRules(pagination.current, pagination.pageSize, searchKeyword)}>
              刷新
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
              新增规则
            </Button>
          </Space>
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
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: handlePageChange,
            showSizeChanger: true,
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
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="规则名称" rules={[{ required: true, message: '请输入规则名称' }]}>
            <Input placeholder="请输入规则名称" />
          </Form.Item>
          <Form.Item name="code" label="规则编码" rules={[{ required: true, message: '请输入规则编码' }]}>
            <Input placeholder="请输入规则编码，如：NULL_CHECK" disabled={!!editingRule} />
          </Form.Item>
          <Form.Item name="type" label="规则类型" rules={[{ required: true, message: '请选择规则类型' }]}>
            <Select placeholder="请选择规则类型">
              <Select.Option value="NULL_CHECK">空值检测</Select.Option>
              <Select.Option value="UNIQUE_CHECK">唯一性检测</Select.Option>
              <Select.Option value="FORMAT_CHECK">格式校验</Select.Option>
              <Select.Option value="RANGE_CHECK">值域校验</Select.Option>
              <Select.Option value="CONSISTENCY_CHECK">一致性检测</Select.Option>
              <Select.Option value="REGEX_CHECK">正则校验</Select.Option>
              <Select.Option value="LENGTH_CHECK">长度校验</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="severity" label="严重程度" rules={[{ required: true, message: '请选择严重程度' }]}>
            <Select placeholder="请选择严重程度">
              <Select.Option value="HIGH">高</Select.Option>
              <Select.Option value="MEDIUM">中</Select.Option>
              <Select.Option value="LOW">低</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="tableName" label="检查表名">
            <Input placeholder="请输入要检查的表名" />
          </Form.Item>
          <Form.Item name="columnName" label="检查列名">
            <Input placeholder="请输入要检查的列名" />
          </Form.Item>
          <Form.Item name="checkExpression" label="检查表达式">
            <TextArea rows={3} placeholder="请输入SQL检查表达式，如：SELECT COUNT(*) FROM table WHERE column IS NULL" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <TextArea rows={2} placeholder="请输入规则描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default QualityRules;
