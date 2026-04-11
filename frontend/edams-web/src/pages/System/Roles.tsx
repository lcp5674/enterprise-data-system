/**
 * 角色权限页面
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
  LockOutlined,
} from '@ant-design/icons';
import styles from './index.less';

interface Role {
  id: string;
  name: string;
  code: string;
  description: string;
  userCount: number;
  permissions: string[];
}

const SystemRoles: React.FC = () => {
  const [loading] = useState(false);
  const [dataSource, setDataSource] = useState<Role[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [permissionVisible, setPermissionVisible] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [form] = Form.useForm();
  const [permissionForm] = Form.useForm();

  // 模拟角色数据
  const mockRoles: Role[] = [
    { id: '1', name: '系统管理员', code: 'SYS_ADMIN', description: '系统全部权限', userCount: 2, permissions: ['*'] },
    { id: '2', name: '数据管理员', code: 'DATA_ADMIN', description: '数据资产全部管理权限', userCount: 5, permissions: ['asset:*', 'catalog:*', 'quality:*'] },
    { id: '3', name: '数据分析师', code: 'DATA_ANALYST', description: '数据查询和分析权限', userCount: 15, permissions: ['asset:read', 'catalog:read', 'lineage:read', 'quality:read'] },
    { id: '4', name: '业务用户', code: 'BUSINESS_USER', description: '基础数据访问权限', userCount: 50, permissions: ['asset:read', 'catalog:read'] },
  ];

  const [roles] = useState<Role[]>(mockRoles);

  // 权限树数据
  const permissionTreeData: DataNode[] = [
    {
      title: '资产管理',
      key: 'asset',
      children: [
        { title: '查看', key: 'asset:read' },
        { title: '创建', key: 'asset:create' },
        { title: '编辑', key: 'asset:update' },
        { title: '删除', key: 'asset:delete' },
      ],
    },
    {
      title: '目录管理',
      key: 'catalog',
      children: [
        { title: '查看', key: 'catalog:read' },
        { title: '编辑', key: 'catalog:update' },
      ],
    },
    {
      title: '血缘分析',
      key: 'lineage',
      children: [
        { title: '查看', key: 'lineage:read' },
        { title: '分析', key: 'lineage:analyze' },
      ],
    },
    {
      title: '质量管理',
      key: 'quality',
      children: [
        { title: '查看', key: 'quality:read' },
        { title: '规则管理', key: 'quality:rule' },
        { title: '问题处理', key: 'quality:issue' },
      ],
    },
    {
      title: '系统管理',
      key: 'system',
      children: [
        { title: '用户管理', key: 'system:user' },
        { title: '角色管理', key: 'system:role' },
        { title: '数据源配置', key: 'system:datasource' },
      ],
    },
  ];

  // 新增角色
  const handleAdd = () => {
    setEditingRole(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑角色
  const handleEdit = (record: Role) => {
    setEditingRole(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 配置权限
  const handlePermission = (record: Role) => {
    setSelectedRole(record);
    permissionForm.setFieldsValue({ permissions: record.permissions === ['*'] ? ['*'] : record.permissions });
    setPermissionVisible(true);
  };

  // 删除角色
  const handleDelete = (id: string) => {
    setDataSource(dataSource.filter(role => role.id !== id));
    message.success('删除成功');
  };

  // 提交角色表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRole) {
        setDataSource(dataSource.map(role =>
          role.id === editingRole.id ? { ...role, ...values } : role
        ));
        message.success('角色更新成功');
      } else {
        const newRole: Role = {
          id: `role-${Date.now()}`,
          ...values,
          userCount: 0,
          permissions: [],
        };
        setDataSource([...dataSource, newRole]);
        message.success('角色创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 提交权限配置
  const handlePermissionSubmit = async () => {
    try {
      const values = await permissionForm.validateFields();
      setDataSource(dataSource.map(role =>
        role.id === selectedRole?.id ? { ...role, permissions: values.permissions || [] } : role
      ));
      message.success('权限配置成功');
      setPermissionVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 表格列配置
  const columns: ColumnsType<Role> = [
    {
      title: '角色名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <LockOutlined style={{ color: '#1890ff' }} />
          <span>{name}</span>
          <Tag>{record.code}</Tag>
        </Space>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '用户数',
      dataIndex: 'userCount',
      key: 'userCount',
      width: 100,
      align: 'center',
      render: (count: number) => <Tag color="blue">{count}</Tag>,
    },
    {
      title: '权限',
      key: 'permissions',
      render: (_, record) => (
        <span>
          {record.permissions.length === 1 && record.permissions[0] === '*'
            ? '全部权限'
            : `${record.permissions.length} 个权限`}
        </span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<LockOutlined />} onClick={() => handlePermission(record)}>
            权限
          </Button>
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该角色？"
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
        title="角色权限"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增角色
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

      {/* 新增/编辑角色弹窗 */}
      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item name="code" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input placeholder="请输入角色编码，如：ADMIN" disabled={!!editingRole} />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入角色描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 权限配置弹窗 */}
      <Modal
        title={`配置权限 - ${selectedRole?.name}`}
        open={permissionVisible}
        onOk={handlePermissionSubmit}
        onCancel={() => setPermissionVisible(false)}
        okText="确认"
        cancelText="取消"
        width={600}
      >
        <Form form={permissionForm} layout="vertical">
          <Form.Item name="permissions" label="选择权限">
            <Tree
              checkable
              defaultExpandAll
              treeData={permissionTreeData}
              height={400}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SystemRoles;
