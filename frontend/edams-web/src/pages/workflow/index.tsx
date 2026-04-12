/**
 * 工作流管理配置页面
 */

import React, { useState, useEffect, useCallback } from 'react';
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
  Divider,
  InputNumber,
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
  ReloadOutlined,
} from '@ant-design/icons';
import { http } from '@/services/request';
import { API_PATHS } from '@/constants';
import styles from './index.less';

const { TextArea } = Input;
const { TabPane } = Tabs;
const { Option } = Select;

// 流程定义类型
interface ProcessDefinition {
  id: number;
  name: string;
  code: string;
  version: number;
  status: number;
  statusText: string;
  type: number;
  typeText: string;
  description?: string;
  bpmnContent?: string;
  createdTime?: string;
  updatedTime?: string;
}

// 运行实例类型
interface ProcessInstance {
  id: number;
  definitionId: number;
  processName: string;
  processDefKey: string;
  initiatorId: number;
  initiatorName: string;
  businessTitle: string;
  businessType: number;
  startTime: string;
  endTime?: string;
  status: number;
  statusText: string;
  currentNodeName?: string;
  priority: number;
}

// 历史记录类型
interface ProcessHistory {
  id: number;
  taskName: string;
  assigneeName?: string;
  action: string;
  comment?: string;
  time: string;
  duration?: number;
}

const WorkflowManagement: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('definition');
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [form] = Form.useForm();
  const [editingRecord, setEditingRecord] = useState<ProcessDefinition | null>(null);

  // 流程定义数据
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([]);

  // 运行实例数据
  const [instances, setInstances] = useState<ProcessInstance[]>([]);

  // 历史记录数据
  const [history, setHistory] = useState<ProcessHistory[]>([]);

  // 选中实例详情
  const [selectedInstance, setSelectedInstance] = useState<ProcessInstance | null>(null);

  // 分页状态
  const [definitionPagination, setDefinitionPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [instancePagination, setInstancePagination] = useState({ current: 1, pageSize: 10, total: 0 });

  // 加载流程定义列表
  const loadDefinitions = useCallback(async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const result = await http.get<any>(API_PATHS.WORKFLOW.DETAIL('').replace('/{instanceId}', ''), {
        pageNum: page,
        pageSize: pageSize,
      });
      // 处理返回数据
      const records = Array.isArray(result) ? result : (result?.records || []);
      setDefinitions(records.map((item: any) => ({
        ...item,
        statusText: item.status === 0 ? '草稿' : item.status === 1 ? '已发布' : '已禁用',
        typeText: getTypeText(item.type),
      })));
      setDefinitionPagination({
        current: page,
        pageSize: pageSize,
        total: result?.total || records.length,
      });
    } catch (error) {
      message.error('加载流程定义失败');
      console.error('加载流程定义失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  // 加载运行实例列表
  const loadInstances = useCallback(async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      // 调用待我审批列表API
      const result = await http.get<any>(API_PATHS.WORKFLOW.APPROVALS, {
        pageNum: page,
        pageSize: pageSize,
      });
      const records = Array.isArray(result) ? result : (result?.records || []);
      setInstances(records.map((item: any) => ({
        ...item,
        statusText: getStatusText(item.status),
      })));
      setInstancePagination({
        current: page,
        pageSize: pageSize,
        total: result?.total || records.length,
      });
    } catch (error) {
      message.error('加载运行实例失败');
      console.error('加载运行实例失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  // 加载流程实例详情
  const loadInstanceDetail = useCallback(async (instanceId: number) => {
    try {
      const result = await http.get<any>(API_PATHS.WORKFLOW.APPROVAL_DETAIL(String(instanceId)));
      setSelectedInstance(result);
    } catch (error) {
      message.error('加载实例详情失败');
      console.error('加载实例详情失败:', error);
    }
  }, []);

  // 加载流程历史
  const loadProcessHistory = useCallback(async (instanceId: number) => {
    try {
      // 历史记录暂未实现，返回空数组
      setHistory([]);
    } catch (error) {
      message.error('加载流程历史失败');
      console.error('加载流程历史失败:', error);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'definition') {
      loadDefinitions(definitionPagination.current, definitionPagination.pageSize);
    } else if (activeTab === 'instance') {
      loadInstances(instancePagination.current, instancePagination.pageSize);
    }
  }, [activeTab, loadDefinitions, loadInstances, definitionPagination.current, definitionPagination.pageSize, instancePagination.current, instancePagination.pageSize]);

  // 获取类型文本
  const getTypeText = (type: number): string => {
    const typeMap: Record<number, string> = {
      1: '资产审批',
      2: '变更申请',
      3: '质量告警',
      4: '权限申请',
      5: '数据导出',
    };
    return typeMap[type] || '其他';
  };

  // 获取状态文本
  const getStatusText = (status: number): string => {
    const statusMap: Record<number, string> = {
      0: '运行中',
      1: '已完成',
      2: '已撤销',
      3: '已驳回',
    };
    return statusMap[status] || '未知';
  };

  // 新增流程
  const handleAdd = () => {
    form.resetFields();
    setEditingRecord(null);
    setModalVisible(true);
  };

  // 编辑流程
  const handleEdit = (record: ProcessDefinition) => {
    form.setFieldsValue(record);
    setEditingRecord(record);
    setModalVisible(true);
  };

  // 删除流程
  const handleDelete = async (id: number) => {
    try {
      await http.delete(`/api/v1/workflows/${id}`);
      message.success('删除成功');
      loadDefinitions();
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 发布流程
  const handleDeploy = async (id: number) => {
    try {
      await http.post(`/api/v1/workflows/${id}/deploy`);
      message.success('发布成功');
      loadDefinitions();
    } catch (error) {
      message.error('发布失败');
    }
  };

  // 禁用流程
  const handleDisable = async (id: number) => {
    try {
      await http.put(`/api/v1/workflows/${id}/status?status=2`);
      message.success('禁用成功');
      loadDefinitions();
    } catch (error) {
      message.error('禁用失败');
    }
  };

  // 启用流程
  const handleEnable = async (id: number) => {
    try {
      await http.put(`/api/v1/workflows/${id}/status?status=1`);
      message.success('启用成功');
      loadDefinitions();
    } catch (error) {
      message.error('启用失败');
    }
  };

  // 新增版本
  const handleNewVersion = (record: ProcessDefinition) => {
    Modal.info({
      title: '新增版本',
      content: `正在为「${record.name}」创建新版本...`,
    });
  };

  // 查看实例详情
  const handleViewInstance = async (record: ProcessInstance) => {
    setSelectedInstance(record);
    setDetailVisible(true);
    await loadProcessHistory(record.id);
  };

  // 挂起实例
  const handleSuspend = (record: ProcessInstance) => {
    setInstances(instances.map(i =>
      i.id === record.id ? { ...i, status: 2, statusText: '已挂起' } : i
    ));
    message.success('实例已挂起');
  };

  // 取消实例
  const handleCancel = async (record: ProcessInstance) => {
    try {
      await http.post(`/api/v1/workflows/${record.id}/cancel`);
      message.success('实例已取消');
      loadInstances();
    } catch (error) {
      message.error('取消失败');
    }
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord) {
        // 更新
        await http.put(`/api/v1/workflows/${editingRecord.id}`, values);
        message.success('更新成功');
      } else {
        // 创建
        await http.post('/api/v1/workflows', values);
        message.success('创建成功');
      }
      setModalVisible(false);
      loadDefinitions();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 获取实例状态标签
  const getInstanceStatusTag = (status: number) => {
    const config: Record<number, { color: string; label: string }> = {
      0: { color: 'blue', label: '运行中' },
      1: { color: 'green', label: '已完成' },
      2: { color: 'gold', label: '已挂起' },
      3: { color: 'red', label: '已驳回' },
    };
    return config[status] || { color: 'default', label: '未知' };
  };

  // 流程定义列
  const definitionColumns: ColumnsType<ProcessDefinition> = [
    { title: '流程名称', dataIndex: 'name', key: 'name', render: (name: string, record) => (
      <Space>{name}<Tag color={record.status === 1 ? 'green' : record.status === 0 ? 'blue' : 'gray'}>
        {record.statusText}
      </Tag></Space>
    )},
    { title: '流程Key', dataIndex: 'code', key: 'code', width: 180 },
    { title: '类型', dataIndex: 'typeText', key: 'typeText', width: 100 },
    { title: '版本', dataIndex: 'version', key: 'version', width: 80, render: (v: number) => `V${v || 1}.0` },
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
          {record.status === 0 && (
            <Button type="text" size="small" icon={<PlayCircleOutlined />} onClick={() => handleDeploy(record.id)}>发布</Button>
          )}
          {record.status === 1 && (
            <Button type="text" size="small" icon={<PauseCircleOutlined />} onClick={() => handleDisable(record.id)}>禁用</Button>
          )}
          {record.status === 2 && (
            <Button type="text" size="small" icon={<PlayCircleOutlined />} onClick={() => handleEnable(record.id)}>启用</Button>
          )}
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          {record.status === 0 && (
            <Popconfirm title="确定删除？" onConfirm={() => handleDelete(record.id)}>
              <Button type="text" size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  // 运行实例列
  const instanceColumns: ColumnsType<ProcessInstance> = [
    { title: '实例ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '业务标题', dataIndex: 'businessTitle', key: 'businessTitle', ellipsis: true },
    { title: '流程名称', dataIndex: 'processName', key: 'processName' },
    { title: '发起人', dataIndex: 'initiatorName', key: 'initiatorName', width: 100 },
    { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 160 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => {
        const config = getInstanceStatusTag(status);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    { title: '当前节点', dataIndex: 'currentNodeName', key: 'currentNodeName', width: 150 },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleViewInstance(record)}>详情</Button>
          {record.status === 0 && <Button type="text" size="small" icon={<PauseCircleOutlined />} onClick={() => handleSuspend(record)}>挂起</Button>}
          {record.status === 0 && <Button type="text" size="small" danger icon={<StopOutlined />} onClick={() => handleCancel(record)}>取消</Button>}
        </Space>
      ),
    },
  ];

  // 历史记录列
  const historyColumns: ColumnsType<ProcessHistory> = [
    { title: '节点名称', dataIndex: 'taskName', key: 'taskName' },
    { title: '办理人', dataIndex: 'assigneeName', key: 'assigneeName', width: 100 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 100 },
    { title: '意见', dataIndex: 'comment', key: 'comment', ellipsis: true },
    { title: '时间', dataIndex: 'time', key: 'time', width: 160 },
    { title: '耗时', dataIndex: 'duration', key: 'duration', width: 100, render: (d: number) => d ? `${d}分钟` : '-' },
  ];

  // 流程定义表格分页变化
  const handleDefinitionPageChange = (page: number, pageSize: number) => {
    setDefinitionPagination({ ...definitionPagination, current: page, pageSize });
  };

  // 运行实例分页变化
  const handleInstancePageChange = (page: number, pageSize: number) => {
    setInstancePagination({ ...instancePagination, current: page, pageSize });
  };

  return (
    <div className={styles.container}>
      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          activeTab === 'definition' && <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>创建流程</Button>
        }>
          {/* 流程定义Tab */}
          <TabPane tab="流程定义" key="definition">
            <Space style={{ marginBottom: 16 }}>
              <Button icon={<ReloadOutlined />} onClick={() => loadDefinitions()}>刷新</Button>
            </Space>
            <Table
              columns={definitionColumns}
              dataSource={definitions}
              rowKey="id"
              loading={loading}
              pagination={{
                current: definitionPagination.current,
                pageSize: definitionPagination.pageSize,
                total: definitionPagination.total,
                onChange: handleDefinitionPageChange,
                showSizeChanger: true,
                showTotal: (total) => `共 ${total} 条流程`,
              }}
            />
          </TabPane>

          {/* 运行实例Tab */}
          <TabPane tab="运行实例" key="instance">
            <Space style={{ marginBottom: 16 }}>
              <Button icon={<ReloadOutlined />} onClick={() => loadInstances()}>刷新</Button>
            </Space>
            <Table
              columns={instanceColumns}
              dataSource={instances}
              rowKey="id"
              loading={loading}
              pagination={{
                current: instancePagination.current,
                pageSize: instancePagination.pageSize,
                total: instancePagination.total,
                onChange: handleInstancePageChange,
                showSizeChanger: true,
                showTotal: (total) => `共 ${total} 条实例`,
              }}
            />
          </TabPane>
        </Tabs>
      </Card>

      {/* 创建/编辑流程弹窗 */}
      <Modal
        title={editingRecord ? '编辑流程' : '创建流程'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="流程名称" rules={[{ required: true, message: '请输入流程名称' }]}>
            <Input placeholder="请输入流程名称" />
          </Form.Item>
          <Form.Item name="code" label="流程Key" rules={[{ required: true, message: '请输入流程Key' }]}>
            <Input placeholder="请输入流程Key，如：asset-approval" disabled={!!editingRecord} />
          </Form.Item>
          <Form.Item name="type" label="流程类型" rules={[{ required: true, message: '请选择流程类型' }]}>
            <Select placeholder="请选择流程类型">
              <Option value={1}>资产审批</Option>
              <Option value={2}>变更申请</Option>
              <Option value={3}>质量告警</Option>
              <Option value={4}>权限申请</Option>
              <Option value={5}>数据导出</Option>
            </Select>
          </Form.Item>
          <Form.Item name="description" label="流程描述">
            <TextArea rows={3} placeholder="请输入流程描述" />
          </Form.Item>
          <Form.Item name="timeoutHours" label="超时时间（小时）" initialValue={72}>
            <InputNumber min={1} max={720} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="BPMN内容" name="bpmnContent">
            <TextArea rows={8} placeholder={'请输入BPMN 2.0 XML内容'} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 实例详情抽屉 */}
      <Drawer title="实例详情" open={detailVisible} onClose={() => setDetailVisible(false)} width={700}>
        {selectedInstance && (
          <>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="实例ID">{selectedInstance.id}</Descriptions.Item>
              <Descriptions.Item label="业务标题">{selectedInstance.businessTitle}</Descriptions.Item>
              <Descriptions.Item label="流程名称">{selectedInstance.processName}</Descriptions.Item>
              <Descriptions.Item label="发起人">{selectedInstance.initiatorName}</Descriptions.Item>
              <Descriptions.Item label="开始时间">{selectedInstance.startTime}</Descriptions.Item>
              <Descriptions.Item label="当前状态">
                <Tag color={getInstanceStatusTag(selectedInstance.status).color}>
                  {getInstanceStatusTag(selectedInstance.status).label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="当前节点">{selectedInstance.currentNodeName || '-'}</Descriptions.Item>
              <Descriptions.Item label="优先级">
                {selectedInstance.priority === 1 ? '普通' : selectedInstance.priority === 2 ? '重要' : '紧急'}
              </Descriptions.Item>
            </Descriptions>

            <Divider>流程进度</Divider>

            <Steps direction="vertical" size="small" current={selectedInstance.status === 0 ? 1 : 2}>
              <Steps.Step title="流程启动" description={selectedInstance.startTime} status="finish" />
              <Steps.Step title={selectedInstance.currentNodeName || '审批中'} description="当前节点" status="process" />
              <Steps.Step
                title="流程结束"
                description={selectedInstance.endTime || '-'}
                status={selectedInstance.status === 1 ? 'finish' : selectedInstance.status === 3 ? 'error' : 'wait'}
              />
            </Steps>

            <Divider>审批历史</Divider>

            <Table
              columns={historyColumns}
              dataSource={history}
              rowKey="id"
              size="small"
              pagination={false}
            />
          </>
        )}
      </Drawer>
    </div>
  );
};

export default WorkflowManagement;
