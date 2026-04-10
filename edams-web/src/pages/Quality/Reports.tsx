/**
 * 质量报告页面
 */

import React, { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Table,
  Button,
  Space,
  Tag,
  Typography,
  DatePicker,
  Select,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  DownloadOutlined,
  FileTextOutlined,
  EyeOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

interface QualityReport {
  id: string;
  name: string;
  type: string;
  period: string;
  status: string;
  createTime: string;
  createBy: string;
  score: number;
}

const QualityReports: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<QualityReport[]>([]);

  // 模拟报告数据
  const mockReports: QualityReport[] = [
    { id: '1', name: '2026年Q1数据质量综合报告', type: 'COMPREHENSIVE', period: '2026Q1', status: 'COMPLETED', createTime: '2026-04-01 10:00:00', createBy: '系统', score: 87 },
    { id: '2', name: '3月份数据质量月报', type: 'MONTHLY', period: '2026-03', status: 'COMPLETED', createTime: '2026-04-01 09:00:00', createBy: '李四', score: 85 },
    { id: '3', name: '交易域质量专项报告', type: 'DOMAIN', period: '2026-03', status: 'COMPLETED', createTime: '2026-03-28 14:00:00', createBy: '张三', score: 92 },
    { id: '4', name: '重要数据资产质量评估报告', type: 'ASSET', period: '2026-03', status: 'COMPLETED', createTime: '2026-03-25 11:00:00', createBy: '王五', score: 88 },
    { id: '5', name: '数据血缘准确性报告', type: 'LINEAGE', period: '2026-03', status: 'GENERATING', createTime: '2026-04-10 10:00:00', createBy: '系统', score: 0 },
  ];

  const [reports] = useState<QualityReport[]>(mockReports);

  // 刷新
  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockReports);
      setLoading(false);
      message.success('刷新成功');
    }, 500);
  };

  // 下载报告
  const handleDownload = (record: QualityReport) => {
    message.success(`正在生成报告: ${record.name}`);
  };

  // 查看报告
  const handleView = (record: QualityReport) => {
    message.info(`查看报告: ${record.name}`);
  };

  // 生成报告
  const handleGenerate = () => {
    message.success('报告生成任务已提交');
  };

  // 获取报告类型标签
  const getTypeTag = (type: string) => {
    const config: Record<string, { color: string; label: string }> = {
      COMPREHENSIVE: { color: 'blue', label: '综合报告' },
      MONTHLY: { color: 'green', label: '月度报告' },
      DOMAIN: { color: 'purple', label: '专项报告' },
      ASSET: { color: 'orange', label: '资产报告' },
      LINEAGE: { color: 'cyan', label: '血缘报告' },
    };
    return config[type] || { color: 'default', label: type };
  };

  // 获取状态标签
  const getStatusTag = (status: string) => {
    const config: Record<string, { color: string; label: string }> = {
      COMPLETED: { color: 'green', label: '已完成' },
      GENERATING: { color: 'blue', label: '生成中' },
      FAILED: { color: 'red', label: '失败' },
    };
    return config[status] || { color: 'default', label: status };
  };

  // 表格列配置
  const columns: ColumnsType<QualityReport> = [
    {
      title: '报告名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <FileTextOutlined style={{ color: '#1890ff' }} />
          <a onClick={() => handleView(record)}>{name}</a>
        </Space>
      ),
    },
    {
      title: '报告类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const config = getTypeTag(type);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '统计周期',
      dataIndex: 'period',
      key: 'period',
      width: 100,
    },
    {
      title: '质量评分',
      dataIndex: 'score',
      key: 'score',
      width: 100,
      render: (score: number) => score > 0 ? (
        <Text style={{ color: score >= 90 ? '#52c41a' : score >= 80 ? '#1890ff' : '#faad14' }}>
          {score}分
        </Text>
      ) : '-',
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
      title: '生成时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
    },
    {
      title: '创建人',
      dataIndex: 'createBy',
      key: 'createBy',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <Button
            type="text"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handleView(record)}
          >
            查看
          </Button>
          {record.status === 'COMPLETED' && (
            <Button
              type="text"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            >
              下载
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card
        title="质量报告"
        extra={
          <Button type="primary" icon={<FileTextOutlined />} onClick={handleGenerate}>
            生成报告
          </Button>
        }
        className={styles.tableCard}
      >
        <div className={styles.toolbar}>
          <Space>
            <RangePicker placeholder={['开始日期', '结束日期']} />
            <Select placeholder="报告类型" style={{ width: 120 }} allowClear>
              <Select.Option value="COMPREHENSIVE">综合报告</Select.Option>
              <Select.Option value="MONTHLY">月度报告</Select.Option>
              <Select.Option value="DOMAIN">专项报告</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
              刷新
            </Button>
          </Space>
        </div>

        <Table
          columns={columns}
          dataSource={reports}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 条报告`,
          }}
        />
      </Card>
    </div>
  );
};

export default QualityReports;
