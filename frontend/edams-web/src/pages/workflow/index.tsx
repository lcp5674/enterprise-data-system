/**
 * 工作流管理配置页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Tabs,
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
  Drawer,
  Timeline,
  Steps,
  Descriptions,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  EyeOutlined,
  BorderOutlined,
  SyncOutlined,
  StopOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { TextArea } = Input;
const { TabPane } = Tabs;

// 流程定义类型
interface ProcessDefinition {
  id: string;
  name: string;
  key: string;
  version: number;
  status: 'ENABLED' | 'DISABLED';
  createdTime: string;
  description?: string;
}

// 运行实例类型
interface ProcessInstance {
  id: string;
  processName: string;
  initiator: string;
  startTime: string;
  status: 'RUNNING' | 'COMPLETED' | 'SUSPENDED' | 'FAILED';
  currentNode?: string;
}

// 历史记录类型
interface ProcessHistory {
  id: string;
  processName: string;
  initiator: string;
  startTime: string;
  endTime?: string;
  status: 'COMPLETED' | 'FAILED' | 'CANCELED';
  duration?: string;
}

const WorkflowManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('definition');
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [form] = Form.useForm();

  // 流程定义数据
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([
    { id: '1', name: '资产审批流程', key: 'asset-approval', version: 3, status: 'ENABLED', createdTime: '2026-01-15', description: '数据资产发布审批流程' },
    { id: '2', name: '变更申请流程', key: 'change-request', version: 2, status: 'ENABLED', createdTime: '2026-02-01', description: '元数据变更申请流程' },
    { id: '3', name: '质量告警处理', key: 'quality-alert', version: 1, status: 'ENABLED', createdTime: '2026-02-20', description: '数据质量告警处理流程' },
    { id: '4', name: '数据导出申请', key: 'data-export', version: 1, status: 'DISABLED', createdTime: '2026-03-05', description: '敏感数据导出审批流程' },
    { id: '5', name: '权限申请流程', key: 'permission-request', version: 2, status: 'ENABLED', createdTime: '2026-03-10', description: '数据权限申请审批流程' },
  ]);

  // 运行实例数据
  const [instances, setInstances] = useState<ProcessInstance[]>([
    { id: '1001', processName: '资产审批流程', initiator: '张三', startTime: '2026-04-11 09:30:00', status: 'RUNNING', currentNode: '部门负责人审批' },
    { id: '1002', processName: '变更申请流程', initiator: '李四', startTime: '2026-04-11 10:00:00', status: 'RUNNING', currentNode: 'DBA审核' },
    { id: '1003', processName: '质量告警处理', initiator: '王五', startTime: '2026-04-10 14:00:00', status: 'COMPLETED', currentNode: '-' },
    { id: '1004', processName: '权限申请流程', initiator: '赵六', startTime: '2026-04-09 16:00:00', status: 'SUSPENDED', currentNode: '安全管理员审批' },
    { id: '1005', processName: '资产审批流程', initiator: '钱七', startTime: '2026-04-08 11:00:00', status: 'FAILED', currentNode: '技术负责人审批' },
  ]);

  // 历史记录数据
  const [history, setHistory] = useState<ProcessHistory[]>([
    { id: '2001', processName: '资产审批流程', initiator: '张三', startTime: '2026-04-01 09:00:00', endTime: '2026-04-03 16:00:00', status: 'COMPLETED', duration: '2天7小时' },
    { id: '2002', processName: '变更申请流程', initiator: '李四', startTime: '2026-04-05 10:00:00', endTime: '2026-04-06 14:00:00', status: 'COMPLETED', duration: '1天4小时' },
    { id: '2003', processName: '数据导出申请', initiator: '王五', startTime: '2026-04-08 15:00:00', endTime: '2026-04-08 15:30:00', status: 'CANCELED', duration: '30分钟' },
    { id: '2004', processName: '质量告警处理', initiator: '赵六', startTime: '2026-04-10 08:00:00', endTime: '2026-04-10 18:00:00', status: 'COMPLETED', duration: '10小时' },
    { id: '2005', processName: '权限申请流程', initiator: '钱八', startTime: '2026-04-07 09:00:00', endTime: '2026-04-07 09:05:00', status: 'FAILED', duration: '5分钟' },
  ]);

  const [selectedInstance, setSelectedInstance] = useState<ProcessInstance | null>(null);

  useEffect(() => {
    setLoading(true);
    setTimeout(() => setLoading(false), 500);
  }, [activeTab]);

  // 新增流程
  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  // 编辑流程
  const handleEdit = (record: ProcessDefinition) => {
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  // 删除流程
  const handleDelete = async (id: string) => {
    message.success('删除成功');
    setDefinitions(definitions.filter(d => d.id !== id));
  };

  // 启用/禁用流程
  const handleToggleStatus = (record: ProcessDefinition) => {
    const newStatus = record.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
    setDefinitions(definitions.map(d =>
      d.id === record.id ? { ...d, status: newStatus } : d
    ));
    message.success(`流程已${newStatus === 'ENABLED' ? '启用' : '禁用'}`);
  };

  // 新增版本
  const handleNewVersion = (record: ProcessDefinition) => {
    Modal.info({
      title: '新增版本',
      content: `正在为「${record.name}」创建新版本...`,
    });
  };

  // 查看实例详情
  const handleViewInstance = (record: ProcessInstance) => {
    setSelectedInstance(record);
    setDetailVisible(true);
  };

  // 挂起实例
  const handleSuspend = (record: ProcessInstance) => {
    setInstances(instances.map(i =>
      i.id === record.id ? { ...i, status: 'SUSPENDED' } : i
    ));
    message.success('实例已挂起');
  };

  // 取消实例
  const handleCancel = (record: ProcessInstance) => {
    setInstances(instances.filter(i => i.id !== record.id));
    message.success('实例已取消');
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const newDef: ProcessDefinition = {
        id: `def-${Date.now()}`,
        ...values,
        version: 1,
        status: 'ENABLED',
        createdTime: new Date().toLocaleString(),
      };
      setDefinitions([newDef, ...definitions]);
      message.success('流程创建成功');
      setModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 获取实例状态标签
  const getInstanceStatusTag = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      RUNNING: { color: 'blue', label: '运行中' },
      COMPLETED: { color: 'green', label: '已完成' },
      SUSPENDED: { color: 'gold', label: '已挂起' },
      FAILED: { color: 'red', label: '失败' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 获取历史状态标签
  const getHistoryStatusTag = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      COMPLETED: { color: 'green', label: '已完成' },
      FAILED: { color: 'red', label: '失败' },
      CANCELED: { color: 'default', label: '已取消' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 流程定义列
  const definitionColumns: ColumnsType<ProcessDefinition> = [
    { title: '流程名称', dataIndex: 'name', key: 'name', render: (name: string, record) => (
      <Space>{name}<Tag color={record.status === 'ENABLED' ? 'green' : 'gray'}>{record.status === 'ENABLED' ? '启用' : '停用'}</Tag></Space>
    )},
    { title: '流程Key', dataIndex: 'key', key: 'key', width: 180 },
    { title: '版本', dataIndex: 'version', key: 'version', width: 80, render: (v: number) => `V${v}.0` },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createdTime', key: 'createdTime', width: 160 },
    {
      title: '操作',
      key: 'action',
      width: 280,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<BorderOutlined />}>设计器</Button>
          <Button type="text" size="small" icon={<SyncOutlined />} onClick={() => handleNewVersion(record)}>新增版本</Button>
          <Button type="text" size="small" icon={record.status === 'ENABLED' ? <PauseCircleOutlined /> : <PlayCircleOutlined />} onClick={() => handleToggleStatus(record)} />
          <Button type="text" size="small" icon={<DeleteOutlined />} onClick={() => handleDelete(record.id)} />
        </Space>
      ),
    },
  ];

  // 运行实例列
  const instanceColumns: ColumnsType<ProcessInstance> = [
    { title: '实例ID', dataIndex: 'id', key: 'id', width: 100 },
    { title: '流程名称', dataIndex: 'processName', key: 'processName' },
    { title: '发起人', dataIndex: 'initiator', key: 'initiator', width: 100 },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 160 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = getInstanceStatusTag(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    { title: '当前节点', dataIndex: 'currentNode', key: 'currentNode', width: 150 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleViewInstance(record)}>详情</Button>
          {record.status === 'RUNNING' && <Button type="text" size="small" icon={<PauseCircleOutlined />} onClick={() => handleSuspend(record)}>挂起</Button>}
          <Button type="text" size="small" danger icon={<StopOutlined />} onClick={() => handleCancel(record)}>取消</Button>
        </Space>
      ),
    },
  ];

  // 历史记录列
  const historyColumns: ColumnsType<ProcessHistory> = [
    { title: '流程名称', dataIndex: 'processName', key: 'processName' },
    { title: '发起人', dataIndex: 'initiator', key: 'initiator', width: 100 },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 160 },
    { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 160 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = getHistoryStatusTag(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    { title: '耗时', dataIndex: 'duration', key: 'duration', width: 120 },
  ];

  return (
    <div className={styles.container}>
      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          activeTab === 'definition' && <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>创建流程</Button>
        }>
          {/* 流程定义Tab */}
          <TabPane tab="流程定义" key="definition">
            <Table
              columns={definitionColumns}
              dataSource={definitions}
              rowKey="id"
              loading={loading}
              pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条流程` }}
            />
          </TabPane>

          {/* 运行实例Tab */}
          <TabPane tab="运行实例" key="instance">
            <Table
              columns={instanceColumns}
              dataSource={instances}
              rowKey="id"
              loading={loading}
              pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条实例` }}
            />
          </TabPane>

          {/* 历史记录Tab */}
          <TabPane tab="历史记录" key="history">
            <Table
              columns={historyColumns}
              dataSource={history}
              rowKey="id"
              loading={loading}
              pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条记录` }}
            />
          </TabPane>
        </Tabs>
      </Card>

      {/* 创建流程弹窗 */}
      <Modal title="创建流程" open={modalVisible} onOk={handleSubmit} onCancel={() => setModalVisible(false)} okText="确认" cancelText="取消">
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="流程名称" rules={[{ required: true, message: '请输入流程名称' }]}>
            <Input placeholder="请输入流程名称" />
          </Form.Item>
          <Form.Item name="key" label="流程Key" rules={[{ required: true, message: '请输入流程Key' }]}>
            <Input placeholder="请输入流程Key，如：asset-approval" />
          </Form.Item>
          <Form.Item name="description" label="流程描述">
            <TextArea rows={3} placeholder="请输入流程描述" />
          </Form.Item>
          <Form.Item label="表单定义JSON" name="formDefinition">
            <TextArea rows={6} placeholder={'请输入表单定义JSON，如：\n{\n  "fields": [...]\n}'} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 实例详情抽屉 */}
      <Drawer title="实例详情" open={detailVisible} onClose={() => setDetailVisible(false)} width={600}>
        {selectedInstance && (
          <>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="实例ID">{selectedInstance.id}</Descriptions.Item>
              <Descriptions.Item label="流程名称">{selectedInstance.processName}</Descriptions.Item>
              <Descriptions.Item label="发起人">{selectedInstance.initiator}</Descriptions.Item>
              <Descriptions.Item label="开始时间">{selectedInstance.startTime}</Descriptions.Item>
              <Descriptions.Item label="当前状态">
                <Tag color={getInstanceStatusTag(selectedInstance.status).color}>
                  {getInstanceStatusTag(selectedInstance.status).label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="当前节点">{selectedInstance.currentNode}</Descriptions.Item>
            </Descriptions>

            <Divider>流程进度</Divider>

            <Steps direction="vertical" size="small" current={selectedInstance.status === 'RUNNING' ? 1 : 2}>
              <Steps.Step title="流程启动" description="2026-04-11 09:30:00" status="finish" />
              <Steps.Step title="提交申请" description="2026-04-11 09:30:05" status="finish" />
              <Steps.Step title={selectedInstance.currentNode || '部门负责人审批'} description="审批中" status="process" />
              <Steps.Step title="流程结束" description="-" status="wait" />
            </Steps>
          </>
        )}
      </Drawer>
    </div>
  );
};

export default WorkflowManagement;
