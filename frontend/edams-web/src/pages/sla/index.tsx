/**
 * SLA监控页面
 */

import React, { useState, useEffect, useCallback } from 'react';
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
  InputNumber,
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
  ReloadOutlined,
} from '@ant-design/icons';
import { http } from '@/services/request';
import styles from './index.less';

const { TextArea } = Input;

// SLA协议类型
interface SlaAgreement {
  id: number;
  name: string;
  serviceName: string;
  serviceType: string;
  metricType: string;
  metricTypeText: string;
  targetValue: number;
  targetUnit: string;
  status: number;
  statusText: string;
  description?: string;
  ownerId?: number;
  ownerName?: string;
  createdTime?: string;
  updatedTime?: string;
}

const SlaMonitor: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<SlaAgreement[]>([]);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<SlaAgreement | null>(null);
  const [editingRecord, setEditingRecord] = useState<SlaAgreement | null>(null);
  const [form] = Form.useForm();
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [stats, setStats] = useState({
    complianceRate: 0,
    totalCount: 0,
    warningCount: 0,
    breachCount: 0,
  });

  // 加载SLA协议列表
  const loadAgreements = useCallback(async (page = 1, pageSize = 10) => {
    setLoading(true);
    try {
      const result = await http.get<any>('/api/sla/agreement/list', {
        page,
        size: pageSize,
      });
      const records = Array.isArray(result) ? result : (result?.records || []);
      setDataSource(records.map((item: any) => ({
        ...item,
        statusText: item.status === 1 ? '合规' : item.status === 2 ? '告警' : '违约',
        metricTypeText: getMetricTypeText(item.metricType),
      })));
      setPagination({
        current: page,
        pageSize: pageSize,
        total: result?.total || records.length,
      });

      // 计算统计数据
      const compliant = records.filter((item: any) => item.status === 1).length;
      const warning = records.filter((item: any) => item.status === 2).length;
      const breach = records.filter((item: any) => item.status === 3).length;
      const total = records.length;
      setStats({
        complianceRate: total > 0 ? Math.round((compliant / total) * 100) : 0,
        totalCount: total,
        warningCount: warning,
        breachCount: breach,
      });
    } catch (error) {
      message.error('加载SLA协议列表失败');
      console.error('加载SLA协议列表失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAgreements(pagination.current, pagination.pageSize);
  }, []);

  // 获取指标类型文本
  const getMetricTypeText = (type: string): string => {
    const typeMap: Record<string, string> = {
      AVAILABILITY: '可用性',
      RESPONSE_TIME: '响应时间',
      ERROR_RATE: '错误率',
      THROUGHPUT: '吞吐量',
    };
    return typeMap[type] || type;
  };

  // 获取状态标签配置
  const getStatusConfig = (status: number) => {
    const config: Record<number, { color: string; label: string; icon: React.ReactNode }> = {
      1: { color: 'green', label: '合规', icon: <CheckCircleOutlined /> },
      2: { color: 'orange', label: '告警', icon: <WarningOutlined /> },
      3: { color: 'red', label: '违约', icon: <CloseCircleOutlined /> },
    };
    return config[status] || { color: 'default', label: '未知', icon: null };
  };

  // 查看详情
  const handleViewDetail = async (record: SlaAgreement) => {
    try {
      const result = await http.get<any>(`/api/sla/agreement/${record.id}`);
      setSelectedRecord(result);
      setDetailModalVisible(true);
    } catch (error) {
      setSelectedRecord(record);
      setDetailModalVisible(true);
    }
  };

  // 编辑目标
  const handleEdit = (record: SlaAgreement) => {
    setEditingRecord(record);
    form.setFieldsValue({
      name: record.name,
      serviceName: record.serviceName,
      serviceType: record.serviceType,
      metricType: record.metricType,
      targetValue: record.targetValue,
      targetUnit: record.targetUnit,
      description: record.description,
    });
    setEditModalVisible(true);
  };

  // 导出报告
  const handleExport = async (record: SlaAgreement) => {
    try {
      await http.post(`/api/sla/agreement/${record.id}/report?userId=1`);
      message.success(`已开始导出 ${record.name} 的SLA报告`);
    } catch (error) {
      message.error('导出失败');
    }
  };

  // 提交编辑
  const handleSubmitEdit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRecord) {
        await http.put(`/api/sla/agreement/${editingRecord.id}`, values);
        message.success('SLA目标更新成功');
        setEditModalVisible(false);
        loadAgreements(pagination.current, pagination.pageSize);
      }
    } catch (error) {
      message.error('更新失败');
    }
  };

  // 分页变化
  const handlePageChange = (page: number, pageSize: number) => {
    setPagination({ ...pagination, current: page, pageSize });
    loadAgreements(page, pageSize);
  };

  // 表格列配置
  const columns: ColumnsType<SlaAgreement> = [
    {
      title: '协议名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          {name}
          <Tag color={getStatusConfig(record.status).color} icon={getStatusConfig(record.status).icon}>
            {getStatusConfig(record.status).label}
          </Tag>
        </Space>
      ),
    },
    {
      title: '服务名称',
      dataIndex: 'serviceName',
      key: 'serviceName',
    },
    {
      title: '指标类型',
      dataIndex: 'metricTypeText',
      key: 'metricTypeText',
      width: 120,
    },
    {
      title: '目标值',
      dataIndex: 'targetValue',
      key: 'targetValue',
      width: 120,
      render: (value: number, record) => `${value}${record.targetUnit || ''}`,
    },
    {
      title: '服务类型',
      dataIndex: 'serviceType',
      key: 'serviceType',
      width: 100,
    },
    {
      title: '负责人',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 100,
      render: (name: string) => name || '-',
    },
    {
      title: '更新时间',
      dataIndex: 'updatedTime',
      key: 'updatedTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button type="text" size="small" icon={<ExportOutlined />} onClick={() => handleExport(record)}>
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
              title="协议总数"
              value={stats.totalCount}
              valueStyle={{ color: '#1890ff' }}
              prefix={<FileTextOutlined />}
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
              title="违约数量"
              value={stats.breachCount}
              valueStyle={{ color: '#f5222d' }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 主表格 */}
      <Card title="SLA协议列表" className={styles.tableCard}
        extra={<Button icon={<ReloadOutlined />} onClick={() => loadAgreements(pagination.current, pagination.pageSize)}>刷新</Button>}>
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
          <Row gutter={16}>
            <Col span={12}>
              <div className={styles.detailItem}><label>协议名称：</label><span>{selectedRecord.name}</span></div>
            </Col>
            <Col span={12}>
              <div className={styles.detailItem}><label>服务名称：</label><span>{selectedRecord.serviceName}</span></div>
            </Col>
            <Col span={12}>
              <div className={styles.detailItem}><label>指标类型：</label><span>{selectedRecord.metricTypeText}</span></div>
            </Col>
            <Col span={12}>
              <div className={styles.detailItem}><label>目标值：</label><span>{selectedRecord.targetValue}{selectedRecord.targetUnit}</span></div>
            </Col>
            <Col span={12}>
              <div className={styles.detailItem}><label>当前状态：</label>
                <Tag color={getStatusConfig(selectedRecord.status).color}>
                  {selectedRecord.statusText}
                </Tag>
              </div>
            </Col>
            <Col span={12}>
              <div className={styles.detailItem}><label>负责人：</label><span>{selectedRecord.ownerName || '-'}</span></div>
            </Col>
            <Col span={24}>
              <div className={styles.detailItem}><label>描述：</label><p>{selectedRecord.description || '暂无描述'}</p></div>
            </Col>
          </Row>
        )}
      </Modal>

      {/* 编辑弹窗 */}
      <Modal
        title="编辑SLA协议"
        open={editModalVisible}
        onOk={handleSubmitEdit}
        onCancel={() => setEditModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="协议名称" rules={[{ required: true, message: '请输入协议名称' }]}>
            <Input placeholder="请输入协议名称" />
          </Form.Item>
          <Form.Item name="serviceName" label="服务名称" rules={[{ required: true, message: '请输入服务名称' }]}>
            <Input placeholder="请输入服务名称" />
          </Form.Item>
          <Form.Item name="metricType" label="指标类型" rules={[{ required: true, message: '请选择指标类型' }]}>
            <Select placeholder="请选择指标类型">
              <Select.Option value="AVAILABILITY">可用性</Select.Option>
              <Select.Option value="RESPONSE_TIME">响应时间</Select.Option>
              <Select.Option value="ERROR_RATE">错误率</Select.Option>
              <Select.Option value="THROUGHPUT">吞吐量</Select.Option>
            </Select>
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="targetValue" label="目标值" rules={[{ required: true, message: '请输入目标值' }]}>
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="targetUnit" label="单位">
                <Select placeholder="请选择单位">
                  <Select.Option value="%">%</Select.Option>
                  <Select.Option value="ms">毫秒</Select.Option>
                  <Select.Option value="s">秒</Select.Option>
                  <Select.Option value="tps">TPS</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="description" label="描述">
            <TextArea rows={3} placeholder="请输入SLA描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SlaMonitor;
