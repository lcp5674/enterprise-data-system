/**
 * SLA监控页面
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
  Select,
  Input,
  Row,
  Col,
  Statistic,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  CheckCircleOutlined,
  WarningOutlined,
  CloseCircleOutlined,
  FileTextOutlined,
  EditOutlined,
  ExportOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import styles from './index.less';

interface SlaRecord {
  id: string;
  name: string;
  targetService: string;
  sloTarget: string;
  currentStatus: 'COMPLIANT' | 'WARNING' | 'BREACH';
  lastCheckTime: string;
  currentValue: string;
  description?: string;
}

const SlaMonitor: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<SlaRecord[]>([]);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<SlaRecord | null>(null);
  const [editingRecord, setEditingRecord] = useState<SlaRecord | null>(null);
  const [form] = Form.useForm();

  // Mock数据
  const mockData: SlaRecord[] = [
    {
      id: '1',
      name: 'API响应时间SLA',
      targetService: '订单服务',
      sloTarget: '99.9%',
      currentStatus: 'COMPLIANT',
      lastCheckTime: '2026-04-11 09:30:00',
      currentValue: '99.95%',
      description: 'API平均响应时间不超过200ms',
    },
    {
      id: '2',
      name: '数据同步SLA',
      targetService: '库存服务',
      sloTarget: '99.5%',
      currentStatus: 'WARNING',
      lastCheckTime: '2026-04-11 09:30:00',
      currentValue: '99.3%',
      description: '数据同步延迟不超过5分钟',
    },
    {
      id: '3',
      name: '系统可用性SLA',
      targetService: '支付服务',
      sloTarget: '99.99%',
      currentStatus: 'COMPLIANT',
      lastCheckTime: '2026-04-11 09:30:00',
      currentValue: '99.995%',
      description: '系统月度可用性',
    },
    {
      id: '4',
      name: '批处理SLA',
      targetService: '报表服务',
      sloTarget: '99.0%',
      currentStatus: 'BREACH',
      lastCheckTime: '2026-04-11 09:30:00',
      currentValue: '98.5%',
      description: '批处理任务按时完成率',
    },
    {
      id: '5',
      name: '查询性能SLA',
      targetService: '分析服务',
      sloTarget: '99.9%',
      currentStatus: 'COMPLIANT',
      lastCheckTime: '2026-04-11 09:30:00',
      currentValue: '99.92%',
      description: '复杂查询响应时间',
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
    const config: Record<string, { color: string; label: string; icon: React.ReactNode }> = {
      COMPLIANT: { color: 'green', label: '达标', icon: <CheckCircleOutlined /> },
      WARNING: { color: 'orange', label: '告警', icon: <WarningOutlined /> },
      BREACH: { color: 'red', label: '违约', icon: <CloseCircleOutlined /> },
    };
    return config[status] || { color: 'default', label: status, icon: null };
  };

  // 计算统计数据
  const stats = {
    complianceRate: Math.round(
      (dataSource.filter((item) => item.currentStatus === 'COMPLIANT').length / dataSource.length) * 100
    ) || 0,
    avgResponseTime: '245ms',
    warningCount: dataSource.filter((item) => item.currentStatus === 'WARNING').length,
    breachCount: dataSource.filter((item) => item.currentStatus === 'BREACH').length,
    totalCount: dataSource.length,
  };

  // 查看详情
  const handleViewDetail = (record: SlaRecord) => {
    setSelectedRecord(record);
    setDetailModalVisible(true);
  };

  // 编辑目标
  const handleEdit = (record: SlaRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({
      sloTarget: record.sloTarget,
      description: record.description,
    });
    setEditModalVisible(true);
  };

  // 导出报告
  const handleExport = (record: SlaRecord) => {
    console.log('导出SLA报告:', record);
    message.success(`已开始导出 ${record.name} 的SLA报告`);
  };

  // 提交编辑
  const handleSubmitEdit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord) {
        setDataSource(
          dataSource.map((item) =>
            item.id === editingRecord.id ? { ...item, ...values } : item
          )
        );
        message.success('SLA目标更新成功');
      }
      setEditModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 表格列配置
  const columns: ColumnsType<SlaRecord> = [
    {
      title: '协议名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          {name}
          <Tag color="blue">{record.currentValue}</Tag>
        </Space>
      ),
    },
    {
      title: '目标服务',
      dataIndex: 'targetService',
      key: 'targetService',
    },
    {
      title: 'SLO指标',
      dataIndex: 'sloTarget',
      key: 'sloTarget',
      width: 120,
      render: (sloTarget: string) => <Tag>{sloTarget}</Tag>,
    },
    {
      title: '当前状态',
      dataIndex: 'currentStatus',
      key: 'currentStatus',
      width: 120,
      render: (status: string) => {
        const config = getStatusConfig(status);
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.label}
          </Tag>
        );
      },
    },
    {
      title: '最近检查时间',
      dataIndex: 'lastCheckTime',
      key: 'lastCheckTime',
      width: 180,
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
            icon={<EyeOutlined />}
            onClick={() => handleViewDetail(record)}
          >
            详情
          </Button>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="text"
            size="small"
            icon={<ExportOutlined />}
            onClick={() => handleExport(record)}
          >
            导出
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      {/* 统计卡片 */}
      <Row gutter={16} className={styles.statsRow}>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="SLA达标率"
              value={stats.complianceRate}
              suffix="%"
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="平均响应时间"
              value={stats.avgResponseTime}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="违规告警数"
              value={stats.warningCount + stats.breachCount}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="总协议数"
              value={stats.totalCount}
              valueStyle={{ color: '#722ed1' }}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 主表格 */}
      <Card title="SLA协议列表" className={styles.tableCard}>
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 条协议`,
          }}
        />
      </Card>

      {/* 详情弹窗 */}
      <Modal
        title="SLA协议详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={600}
      >
        {selectedRecord && (
          <div className={styles.detailContent}>
            <Row gutter={16}>
              <Col span={12}>
                <div className={styles.detailItem}>
                  <label>协议名称：</label>
                  <span>{selectedRecord.name}</span>
                </div>
              </Col>
              <Col span={12}>
                <div className={styles.detailItem}>
                  <label>目标服务：</label>
                  <span>{selectedRecord.targetService}</span>
                </div>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <div className={styles.detailItem}>
                  <label>SLO指标：</label>
                  <span>{selectedRecord.sloTarget}</span>
                </div>
              </Col>
              <Col span={12}>
                <div className={styles.detailItem}>
                  <label>当前值：</label>
                  <span>{selectedRecord.currentValue}</span>
                </div>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <div className={styles.detailItem}>
                  <label>当前状态：</label>
                  <Tag color={getStatusConfig(selectedRecord.currentStatus).color}>
                    {getStatusConfig(selectedRecord.currentStatus).label}
                  </Tag>
                </div>
              </Col>
              <Col span={12}>
                <div className={styles.detailItem}>
                  <label>检查时间：</label>
                  <span>{selectedRecord.lastCheckTime}</span>
                </div>
              </Col>
            </Row>
            <div className={styles.detailItem}>
              <label>描述：</label>
              <p>{selectedRecord.description || '暂无描述'}</p>
            </div>
          </div>
        )}
      </Modal>

      {/* 编辑弹窗 */}
      <Modal
        title="编辑SLA目标"
        open={editModalVisible}
        onOk={handleSubmitEdit}
        onCancel={() => setEditModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="sloTarget"
            label="SLO目标值"
            rules={[{ required: true, message: '请输入SLO目标值' }]}
          >
            <Select placeholder="请选择SLO目标值">
              <Select.Option value="99.99%">99.99%</Select.Option>
              <Select.Option value="99.9%">99.9%</Select.Option>
              <Select.Option value="99.5%">99.5%</Select.Option>
              <Select.Option value="99.0%">99.0%</Select.Option>
              <Select.Option value="95.0%">95.0%</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入SLA描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SlaMonitor;
