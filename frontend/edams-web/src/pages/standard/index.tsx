/**
 * 数据标准管理页面
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
  Input,
  Tree,
  message,
  Popconfirm,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  FolderOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import styles from './index.less';

interface StandardRecord {
  id: string;
  code: string;
  name: string;
  category: string;
  version: string;
  status: 'DRAFT' | 'PUBLISHED' | 'DEPRECATED';
  creator: string;
  createTime: string;
  description?: string;
}

interface CategoryNode {
  title: string;
  key: string;
  children?: CategoryNode[];
}

const StandardManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<StandardRecord[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState<StandardRecord | null>(null);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [form] = Form.useForm();

  // 分类树数据
  const categoryTreeData: DataNode[] = [
    {
      title: '全部标准',
      key: 'all',
      icon: <FolderOutlined />,
    },
    {
      title: '基础数据标准',
      key: 'basic',
      icon: <FolderOutlined />,
      children: [
        { title: '客户数据', key: 'customer', icon: <FileTextOutlined /> },
        { title: '产品数据', key: 'product', icon: <FileTextOutlined /> },
        { title: '订单数据', key: 'order', icon: <FileTextOutlined /> },
      ],
    },
    {
      title: '业务数据标准',
      key: 'business',
      icon: <FolderOutlined />,
      children: [
        { title: '销售数据', key: 'sales', icon: <FileTextOutlined /> },
        { title: '财务数据', key: 'finance', icon: <FileTextOutlined /> },
        { title: '库存数据', key: 'inventory', icon: <FileTextOutlined /> },
      ],
    },
    {
      title: '技术数据标准',
      key: 'technical',
      icon: <FolderOutlined />,
      children: [
        { title: '接口规范', key: 'api', icon: <FileTextOutlined /> },
        { title: '编码规范', key: 'coding', icon: <FileTextOutlined /> },
      ],
    },
  ];

  // Mock数据
  const mockData: StandardRecord[] = [
    {
      id: '1',
      code: 'STD-CUS-001',
      name: '客户编号标准',
      category: '客户数据',
      version: 'v1.0',
      status: 'PUBLISHED',
      creator: '张三',
      createTime: '2026-04-01',
      description: '定义客户唯一标识编码规则',
    },
    {
      id: '2',
      code: 'STD-PRO-001',
      name: '产品分类标准',
      category: '产品数据',
      version: 'v2.1',
      status: 'PUBLISHED',
      creator: '李四',
      createTime: '2026-03-15',
      description: '产品分类层级定义',
    },
    {
      id: '3',
      code: 'STD-SAL-001',
      name: '销售数据字典',
      category: '销售数据',
      version: 'v1.0',
      status: 'DRAFT',
      creator: '王五',
      createTime: '2026-04-10',
      description: '销售相关数据项定义',
    },
    {
      id: '4',
      code: 'STD-API-001',
      name: 'REST接口规范',
      category: '接口规范',
      version: 'v1.5',
      status: 'PUBLISHED',
      creator: '赵六',
      createTime: '2026-02-20',
      description: 'API设计规范与约束',
    },
    {
      id: '5',
      code: 'STD-FIN-001',
      name: '财务报表标准',
      category: '财务数据',
      version: 'v1.0',
      status: 'DEPRECATED',
      creator: '钱七',
      createTime: '2025-12-01',
      description: '财务报表数据规范',
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
    const config: Record<string, { color: string; label: string }> = {
      DRAFT: { color: 'blue', label: '草稿' },
      PUBLISHED: { color: 'green', label: '已发布' },
      DEPRECATED: { color: 'gray', label: '已废弃' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 树节点选择
  const handleTreeSelect = (selectedKeys: React.Key[]) => {
    const key = selectedKeys[0] as string;
    setSelectedCategory(key);
    
    if (key === 'all') {
      setDataSource(mockData);
    } else {
      // 根据分类筛选
      const categoryMap: Record<string, string[]> = {
        basic: ['客户数据', '产品数据', '订单数据'],
        business: ['销售数据', '财务数据', '库存数据'],
        technical: ['接口规范', '编码规范'],
      };
      
      let categories: string[] = [];
      if (categoryMap[key]) {
        categories = categoryMap[key];
      } else {
        // 直接匹配子分类
        const allCategories = ['客户数据', '产品数据', '订单数据', '销售数据', '财务数据', '库存数据', '接口规范', '编码规范'];
        const catName = categoryTreeData
          .flatMap((node) => node.children || [])
          .find((node) => node.key === key)?.title as string;
        if (catName) categories = [catName];
      }
      
      setDataSource(mockData.filter((item) => categories.includes(item.category)));
    }
  };

  // 新增标准
  const handleAdd = () => {
    setEditingRecord(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑标准
  const handleEdit = (record: StandardRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 删除标准
  const handleDelete = (id: string) => {
    message.success('删除成功');
    setDataSource(dataSource.filter((item) => item.id !== id));
  };

  // 发布标准
  const handlePublish = (record: StandardRecord) => {
    setDataSource(
      dataSource.map((item) =>
        item.id === record.id ? { ...item, status: 'PUBLISHED' } : item
      )
    );
    message.success(`标准 "${record.name}" 已发布`);
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord) {
        setDataSource(
          dataSource.map((item) =>
            item.id === editingRecord.id ? { ...item, ...values } : item
          )
        );
        message.success('标准更新成功');
      } else {
        const newRecord: StandardRecord = {
          id: `std-${Date.now()}`,
          ...values,
          status: 'DRAFT',
          creator: '当前用户',
          createTime: new Date().toISOString().split('T')[0],
        };
        setDataSource([newRecord, ...dataSource]);
        message.success('标准创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取分类选项
  const getCategoryOptions = () => {
    return [
      { label: '客户数据', value: '客户数据' },
      { label: '产品数据', value: '产品数据' },
      { label: '订单数据', value: '订单数据' },
      { label: '销售数据', value: '销售数据' },
      { label: '财务数据', value: '财务数据' },
      { label: '库存数据', value: '库存数据' },
      { label: '接口规范', value: '接口规范' },
      { label: '编码规范', value: '编码规范' },
    ];
  };

  // 表格列配置
  const columns: ColumnsType<StandardRecord> = [
    {
      title: '标准编码',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: '标准名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 120,
      render: (category: string) => <Tag>{category}</Tag>,
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 100,
      render: (version: string) => <Tag color="purple">{version}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = getStatusConfig(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '创建人',
      dataIndex: 'creator',
      key: 'creator',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          {record.status === 'DRAFT' && (
            <Button
              type="text"
              size="small"
              icon={<PlayCircleOutlined />}
              onClick={() => handlePublish(record)}
            >
              发布
            </Button>
          )}
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除该标准？"
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
        <div className={styles.layout}>
          {/* 左侧分类树 */}
          <div className={styles.leftPanel}>
            <Card title="标准分类" size="small" className={styles.treeCard}>
              <Tree
                treeData={categoryTreeData}
                defaultSelectedKeys={['all']}
                onSelect={handleTreeSelect}
                blockNode
              />
            </Card>
          </div>

          {/* 右侧表格 */}
          <div className={styles.rightPanel}>
            <div className={styles.toolbar}>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                新增标准
              </Button>
            </div>
            <Table
              columns={columns}
              dataSource={dataSource}
              rowKey="id"
              loading={loading}
              pagination={{
                pageSize: 10,
                showTotal: (total) => `共 ${total} 条标准`,
              }}
            />
          </div>
        </div>
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingRecord ? '编辑标准' : '新增标准'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="code"
            label="标准编码"
            rules={[{ required: true, message: '请输入标准编码' }]}
          >
            <Input placeholder="请输入标准编码，如 STD-XXX-001" />
          </Form.Item>
          <Form.Item
            name="name"
            label="标准名称"
            rules={[{ required: true, message: '请输入标准名称' }]}
          >
            <Input placeholder="请输入标准名称" />
          </Form.Item>
          <Form.Item
            name="category"
            label="分类"
            rules={[{ required: true, message: '请选择分类' }]}
          >
            <Select
              placeholder="请选择分类"
              options={getCategoryOptions()}
            />
          </Form.Item>
          <Form.Item
            name="version"
            label="版本"
            rules={[{ required: true, message: '请输入版本号' }]}
          >
            <Input placeholder="请输入版本号，如 v1.0" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入标准描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default StandardManagement;
