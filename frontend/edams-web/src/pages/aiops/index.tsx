/**
 * AI智能运维页面
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tag,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  message,
  Popconfirm,
  Badge,
  Tabs,
  Descriptions,
  Timeline,
  Tooltip,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  AlertOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  WarningOutlined,
  ExclamationCircleOutlined,
  ReloadOutlined,
  PlusOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
  ThunderboltOutlined,
  SafetyOutlined,
} from '@ant-design/icons';
import styles from './index.less';
import { aiops } from '../../services';

const { TextArea } = Input;
const { TabPane } = Tabs;

interface Alert {
  id: number;
  alertLevel: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  alertStatus: 'PENDING' | 'ACKNOWLEDGED' | 'RESOLVED' | 'CLOSED';
  alertTitle: string;
  alertContent: string;
  targetId?: string;
  targetName?: string;
  alertTime: string;
  ackTime?: string;
  ackBy?: string;
  resolveTime?: string;
  resolveBy?: string;
  solution?: string;
}

interface AlertRule {
  id: number;
  name: string;
  targetId: string;
  metricName: string;
  condition: string;
  threshold: number;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  enabled: boolean;
  alertTemplate?: string;
}

const AIOps: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [rules, setRules] = useState<AlertRule[]>([]);
  const [stats, setStats] = useState({ critical: 0, high: 0, medium: 0, low: 0, pending: 0 });
  const [ruleModalVisible, setRuleModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedAlert, setSelectedAlert] = useState<Alert | null>(null);
  const [form] = Form.useForm();

  // 加载告警列表
  const loadAlerts = useCallback(async () => {
    setLoading(true);
    try {
      const response = await aiops.getAlertPage({ pageNum: 1, pageSize: 20 });
      if (response.success && response.data) {
        setAlerts(response.data.records || []);
      }
    } catch (error) {
      console.error('加载告警列表失败:', error);
      message.error('加载告警列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  // 加载告警规则
  const loadRules = useCallback(async () => {
    try {
      const response = await aiops.getAlertRules();
      if (response.success && response.data) {
        setRules(response.data);
      }
    } catch (error) {
      console.error('加载告警规则失败:', error);
    }
  }, []);

  // 加载统计数据
  const loadStats = useCallback(async () => {
    try {
      const response = await aiops.countAlertsByLevel();
      if (response.success && response.data) {
        const newStats = { critical: 0, high: 0, medium: 0, low: 0, pending: 0 };
        (response.data || []).forEach((item: any) => {
          const level = item.level?.toLowerCase() || item.alertLevel?.toLowerCase();
          if (level === 'critical') newStats.critical = item.count;
          else if (level === 'high') newStats.high = item.count;
          else if (level === 'medium') newStats.medium = item.count;
          else if (level === 'low') newStats.low = item.count;
        });
        // 获取待处理告警数
        const pendingResponse = await aiops.getPendingAlerts();
        if (pendingResponse.success && pendingResponse.data) {
          newStats.pending = pendingResponse.data.length;
        }
        setStats(newStats);
      }
    } catch (error) {
      console.error('加载统计数据失败:', error);
    }
  }, []);

  // 初始加载
  useEffect(() => {
    loadAlerts();
    loadRules();
    loadStats();
  }, [loadAlerts, loadRules, loadStats]);

  // 获取告警级别配置
  const getLevelConfig = (level: string) => {
    const config: Record<string, { color: string; label: string; icon: React.ReactNode }> = {
      CRITICAL: { color: 'red', label: '严重', icon: <CloseCircleOutlined /> },
      HIGH: { color: 'orange', label: '高', icon: <ExclamationCircleOutlined /> },
      MEDIUM: { color: 'gold', label: '中', icon: <WarningOutlined /> },
      LOW: { color: 'blue', label: '低', icon: <ClockCircleOutlined /> },
    };
    return config[level] || { color: 'default', label: level, icon: null };
  };

  // 获取告警状态配置
  const getStatusConfig = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      PENDING: { color: 'processing', label: '待处理' },
      ACKNOWLEDGED: { color: 'warning', label: '已确认' },
      RESOLVED: { color: 'success', label: '已解决' },
      CLOSED: { color: 'default', label: '已关闭' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 确认告警
  const handleAcknowledge = async (alert: Alert) => {
    try {
      await aiops.acknowledgeAlert(alert.id, 'admin');
      message.success('告警已确认');
      loadAlerts();
      loadStats();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 解决告警
  const handleResolve = async (alert: Alert) => {
    try {
      await aiops.resolveAlert(alert.id, 'admin', '问题已处理');
      message.success('告警已解决');
      loadAlerts();
      loadStats();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 关闭告警
  const handleClose = async (alert: Alert) => {
    try {
      await aiops.closeAlert(alert.id, 'admin');
      message.success('告警已关闭');
      loadAlerts();
      loadStats();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 查看详情
  const handleViewDetail = (alert: Alert) => {
    setSelectedAlert(alert);
    setDetailModalVisible(true);
  };

  // 切换规则状态
  const handleToggleRule = async (rule: AlertRule) => {
    try {
      await aiops.toggleAlertRule(rule.id, !rule.enabled);
      message.success(`规则已${rule.enabled ? '禁用' : '启用'}`);
      loadRules();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 删除规则
  const handleDeleteRule = async (rule: AlertRule) => {
    try {
      await aiops.deleteAlertRule(rule.id);
      message.success('规则已删除');
      loadRules();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 提交规则
  const handleSubmitRule = async () => {
    try {
      const values = await form.validateFields();
      if (values.id) {
        await aiops.updateAlertRule(values.id, values);
        message.success('规则已更新');
      } else {
        await aiops.createAlertRule(values);
        message.success('规则已创建');
      }
      setRuleModalVisible(false);
      form.resetFields();
      loadRules();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 打开规则编辑
  const handleEditRule = (rule?: AlertRule) => {
    if (rule) {
      form.setFieldsValue(rule);
    } else {
      form.resetFields();
    }
    setRuleModalVisible(true);
  };

  // 告警表格列
  const alertColumns: ColumnsType<Alert> = [
    {
      title: '告警级别',
      dataIndex: 'alertLevel',
      key: 'alertLevel',
      width: 100,
      render: (level: string) => {
        const config = getLevelConfig(level);
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.label}
          </Tag>
        );
      },
    },
    {
      title: '告警标题',
      dataIndex: 'alertTitle',
      key: 'alertTitle',
      ellipsis: true,
    },
    {
      title: '目标',
      dataIndex: 'targetName',
      key: 'targetName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'alertStatus',
      key: 'alertStatus',
      width: 100,
      render: (status: string) => {
        const config = getStatusConfig(status);
        return <Badge status={config.color as any} text={config.label} />;
      },
    },
    {
      title: '告警时间',
      dataIndex: 'alertTime',
      key: 'alertTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)} />
          </Tooltip>
          {record.alertStatus === 'PENDING' && (
            <Tooltip title="确认">
              <Button type="text" size="small" icon={<CheckCircleOutlined />} onClick={() => handleAcknowledge(record)} />
            </Tooltip>
          )}
          {record.alertStatus === 'ACKNOWLEDGED' && (
            <Tooltip title="解决">
              <Button type="text" size="small" icon={<CheckCircleOutlined />} onClick={() => handleResolve(record)} />
            </Tooltip>
          )}
          {record.alertStatus === 'RESOLVED' && (
            <Tooltip title="关闭">
              <Button type="text" size="small" icon={<CloseCircleOutlined />} onClick={() => handleClose(record)} />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  // 规则表格列
  const ruleColumns: ColumnsType<AlertRule> = [
    {
      title: '规则名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: '监控指标',
      dataIndex: 'metricName',
      key: 'metricName',
      width: 150,
    },
    {
      title: '条件',
      key: 'condition',
      width: 150,
      render: (_, record) => (
        <span>
          {record.condition} {record.threshold}
        </span>
      ),
    },
    {
      title: '严重级别',
      dataIndex: 'severity',
      key: 'severity',
      width: 100,
      render: (severity: string) => {
        const config = getLevelConfig(severity);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      render: (enabled: boolean) => (
        <Switch checked={enabled} onChange={() => {}} size="small" />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="编辑">
            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEditRule(record)} />
          </Tooltip>
          <Popconfirm
            title="确认删除该规则？"
            onConfirm={() => handleDeleteRule(record)}
            okText="确认"
            cancelText="取消"
          >
            <Tooltip title="删除">
              <Button type="text" size="small" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card className={styles.mainCard}>
        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={4}>
            <Card size="small" className={styles.statCard}>
              <Statistic
                title="严重告警"
                value={stats.critical}
                valueStyle={{ color: '#ff4d4f' }}
                prefix={<CloseCircleOutlined />}
                suffix={<span style={{ fontSize: 14 }}>个</span>}
              />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small" className={styles.statCard}>
              <Statistic
                title="高风险告警"
                value={stats.high}
                valueStyle={{ color: '#fa8c16' }}
                prefix={<ExclamationCircleOutlined />}
                suffix={<span style={{ fontSize: 14 }}>个</span>}
              />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small" className={styles.statCard}>
              <Statistic
                title="中风险告警"
                value={stats.medium}
                valueStyle={{ color: '#faad14' }}
                prefix={<WarningOutlined />}
                suffix={<span style={{ fontSize: 14 }}>个</span>}
              />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small" className={styles.statCard}>
              <Statistic
                title="低风险告警"
                value={stats.low}
                valueStyle={{ color: '#1890ff' }}
                prefix={<ClockCircleOutlined />}
                suffix={<span style={{ fontSize: 14 }}>个</span>}
              />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small" className={styles.statCard}>
              <Statistic
                title="待处理"
                value={stats.pending}
                valueStyle={{ color: '#722ed1' }}
                prefix={<AlertOutlined />}
                suffix={<span style={{ fontSize: 14 }}>个</span>}
              />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small" className={styles.statCard}>
              <Button type="primary" icon={<ReloadOutlined />} onClick={() => { loadAlerts(); loadStats(); }}>
                刷新数据
              </Button>
            </Card>
          </Col>
        </Row>

        {/* 标签页 */}
        <Tabs defaultActiveKey="alerts">
          <TabPane
            tab={
              <span>
                <AlertOutlined />
                告警列表
              </span>
            }
            key="alerts"
          >
            <Table
              columns={alertColumns}
              dataSource={alerts}
              rowKey="id"
              loading={loading}
              pagination={{ pageSize: 10, showTotal: (total) => `共 ${total} 条` }}
            />
          </TabPane>
          <TabPane
            tab={
              <span>
                <SafetyOutlined />
                告警规则
              </span>
            }
            key="rules"
          >
            <div style={{ marginBottom: 16 }}>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => handleEditRule()}>
                新建规则
              </Button>
            </div>
            <Table
              columns={ruleColumns}
              dataSource={rules}
              rowKey="id"
              loading={loading}
              pagination={false}
            />
          </TabPane>
          <TabPane
            tab={
              <span>
                <ThunderboltOutlined />
                异常检测
              </span>
            }
            key="anomalies"
          >
            <Empty description="异常检测功能开发中" />
          </TabPane>
        </Tabs>
      </Card>

      {/* 告警详情弹窗 */}
      <Modal
        title="告警详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={700}
      >
        {selectedAlert && (
          <Descriptions bordered column={2}>
            <Descriptions.Item label="告警级别" span={2}>
              {(() => {
                const config = getLevelConfig(selectedAlert.alertLevel);
                return <Tag color={config.color}>{config.label}</Tag>;
              })()}
            </Descriptions.Item>
            <Descriptions.Item label="告警标题" span={2}>
              {selectedAlert.alertTitle}
            </Descriptions.Item>
            <Descriptions.Item label="告警内容" span={2}>
              {selectedAlert.alertContent}
            </Descriptions.Item>
            <Descriptions.Item label="目标">
              {selectedAlert.targetName || selectedAlert.targetId || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              {(() => {
                const config = getStatusConfig(selectedAlert.alertStatus);
                return <Badge status={config.color as any} text={config.label} />;
              })()}
            </Descriptions.Item>
            <Descriptions.Item label="告警时间">
              {selectedAlert.alertTime}
            </Descriptions.Item>
            <Descriptions.Item label="确认时间">
              {selectedAlert.ackTime || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="确认人">
              {selectedAlert.ackBy || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="解决时间">
              {selectedAlert.resolveTime || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="解决人">
              {selectedAlert.resolveBy || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="解决方案" span={2}>
              {selectedAlert.solution || '-'}
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      {/* 规则编辑弹窗 */}
      <Modal
        title={form.getFieldValue('id') ? '编辑规则' : '新建规则'}
        open={ruleModalVisible}
        onOk={handleSubmitRule}
        onCancel={() => {
          setRuleModalVisible(false);
          form.resetFields();
        }}
        okText="保存"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden>
            <Input />
          </Form.Item>
          <Form.Item
            name="name"
            label="规则名称"
            rules={[{ required: true, message: '请输入规则名称' }]}
          >
            <Input placeholder="请输入规则名称" />
          </Form.Item>
          <Form.Item
            name="metricName"
            label="监控指标"
            rules={[{ required: true, message: '请选择监控指标' }]}
          >
            <Select placeholder="请选择监控指标">
              <Select.Option value="cpu_usage">CPU使用率</Select.Option>
              <Select.Option value="memory_usage">内存使用率</Select.Option>
              <Select.Option value="disk_usage">磁盘使用率</Select.Option>
              <Select.Option value="response_time">响应时间</Select.Option>
              <Select.Option value="error_rate">错误率</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="condition"
            label="条件"
            rules={[{ required: true, message: '请选择条件' }]}
          >
            <Select placeholder="请选择条件">
              <Select.Option value=">">&gt; 大于</Select.Option>
              <Select.Option value=">=">&gt;= 大于等于</Select.Option>
              <Select.Option value="<">&lt; 小于</Select.Option>
              <Select.Option value="<=">&lt;= 小于等于</Select.Option>
              <Select.Option value="=">= 等于</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="threshold"
            label="阈值"
            rules={[{ required: true, message: '请输入阈值' }]}
          >
            <Input type="number" placeholder="请输入阈值" />
          </Form.Item>
          <Form.Item
            name="severity"
            label="严重级别"
            rules={[{ required: true, message: '请选择严重级别' }]}
          >
            <Select placeholder="请选择严重级别">
              <Select.Option value="CRITICAL">严重</Select.Option>
              <Select.Option value="HIGH">高</Select.Option>
              <Select.Option value="MEDIUM">中</Select.Option>
              <Select.Option value="LOW">低</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="enabled" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="alertTemplate" label="告警模板">
            <TextArea rows={3} placeholder="请输入告警模板，支持变量：${metric}, ${value}, ${threshold}" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AIOps;
