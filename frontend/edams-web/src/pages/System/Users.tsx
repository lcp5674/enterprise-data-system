/**
 * 用户管理页面
 */

import React, { useState } from 'react';
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
  Avatar,
  Popconfirm,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  UserOutlined,
  LockOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Search } = Input;

interface User {
  id: string;
  username: string;
  name: string;
  email: string;
  mobile: string;
  department: string;
  roles: string[];
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  lastLoginTime: string;
}

const SystemUsers: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<User[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [form] = Form.useForm();

  // 模拟用户数据
  const mockUsers: User[] = [
    { id: '1', username: 'admin', name: '管理员', email: 'admin@enterprise.com', mobile: '13800138000', department: 'IT部', roles: ['系统管理员'], status: 'ACTIVE', lastLoginTime: '2026-04-10 09:00:00' },
    { id: '2', username: 'zhangsan', name: '张三', email: 'zhangsan@enterprise.com', mobile: '13800138001', department: '数据部', roles: ['数据管理员'], status: 'ACTIVE', lastLoginTime: '2026-04-10 08:30:00' },
    { id: '3', username: 'lisi', name: '李四', email: 'lisi@enterprise.com', mobile: '13800138002', department: '业务部', roles: ['业务用户'], status: 'ACTIVE', lastLoginTime: '2026-04-09 17:00:00' },
    { id: '4', username: 'wangwu', name: '王五', email: 'wangwu@enterprise.com', mobile: '13800138003', department: '数据部', roles: ['数据分析师'], status: 'INACTIVE', lastLoginTime: '2026-04-01 10:00:00' },
  ];

  const [users] = useState<User[]>(mockUsers);

  // 搜索
  const handleSearch = (value: string) => {
    if (!value) {
      setDataSource(mockUsers);
      return;
    }
    setDataSource(mockUsers.filter(user =>
      user.name.includes(value) || user.username.includes(value) || user.email.includes(value)
    ));
  };

  // 新增用户
  const handleAdd = () => {
    setEditingUser(null);
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑用户
  const handleEdit = (record: User) => {
    setEditingUser(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 删除用户
  const handleDelete = (id: string) => {
    setDataSource(dataSource.filter(user => user.id !== id));
    message.success('删除成功');
  };

  // 重置密码
  const handleResetPassword = (record: User) => {
    Modal.confirm({
      title: '确认重置密码',
      content: `确定要重置用户 "${record.name}" 的密码吗？`,
      onOk: () => {
        message.success('密码已重置');
      },
    });
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingUser) {
        setDataSource(dataSource.map(user =>
          user.id === editingUser.id ? { ...user, ...values } : user
        ));
        message.success('用户更新成功');
      } else {
        const newUser: User = {
          id: `user-${Date.now()}`,
          ...values,
          status: 'ACTIVE',
          lastLoginTime: '-',
        };
        setDataSource([...dataSource, newUser]);
        message.success('用户创建成功');
      }
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      ACTIVE: { color: 'green', label: '正常' },
      INACTIVE: { color: 'default', label: '停用' },
      LOCKED: { color: 'red', label: '锁定' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 表格列配置
  const columns: ColumnsType<User> = [
    {
      title: '用户',
      key: 'user',
      render: (_, record) => (
        <Space>
          <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }} />
          <div>
            <div>{record.name}</div>
            <div style={{ fontSize: 12, color: '#8c8c8c' }}>@{record.username}</div>
          </div>
        </Space>
      ),
    },
    {
      title: '部门',
      dataIndex: 'department',
      key: 'department',
      width: 120,
    },
    {
      title: '角色',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles: string[]) => (
        <Space wrap>
          {roles.map(role => <Tag key={role}>{role}</Tag>)}
        </Space>
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
      title: '最后登录',
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button type="text" size="small" icon={<LockOutlined />} onClick={() => handleResetPassword(record)}>
            重置密码
          </Button>
          <Popconfirm
            title="确认删除该用户？"
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
        title="用户管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增用户
          </Button>
        }
        className={styles.tableCard}
      >
        <div className={styles.toolbar}>
          <Search placeholder="搜索用户" onSearch={handleSearch} style={{ width: 300 }} allowClear />
        </div>

        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 个用户`,
          }}
        />
      </Card>

      {/* 新增/编辑用户弹窗 */}
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
        width={500}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input placeholder="请输入用户名" disabled={!!editingUser} />
          </Form.Item>
          {!editingUser && (
            <Form.Item name="password" label="初始密码" rules={[{ required: true, message: '请输入密码' }]}>
              <Input.Password placeholder="请输入初始密码" />
            </Form.Item>
          )}
          <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item name="email" label="邮箱" rules={[{ required: true, type: 'email', message: '请输入正确的邮箱' }]}>
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item name="mobile" label="手机号">
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item name="department" label="部门" rules={[{ required: true, message: '请选择部门' }]}>
            <Select placeholder="请选择部门">
              <Select.Option value="IT部">IT部</Select.Option>
              <Select.Option value="数据部">数据部</Select.Option>
              <Select.Option value="业务部">业务部</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="roles" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Select mode="multiple" placeholder="请选择角色">
              <Select.Option value="系统管理员">系统管理员</Select.Option>
              <Select.Option value="数据管理员">数据管理员</Select.Option>
              <Select.Option value="数据分析师">数据分析师</Select.Option>
              <Select.Option value="业务用户">业务用户</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SystemUsers;
