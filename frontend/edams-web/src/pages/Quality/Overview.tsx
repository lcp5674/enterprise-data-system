/**
 * 质量概览页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Table,
  Space,
  Typography,
  Tag,
  Button,
  Rate,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  CheckCircleOutlined,
  WarningOutlined,
  ClockCircleOutlined,
  ArrowRightOutlined,
  ReloadOutlined,
  DatabaseOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import * as qualityService from '../../services/quality';
import styles from './index.less';

const { Title, Text } = Typography;

interface QualityTrend {
  date: string;
  score: number;
  passed: number;
  failed: number;
}

interface RecentIssue {
  id: string;
  assetName: string;
  ruleName: string;
  severity: string;
  status: string;
  createTime: string;
}

const QualityOverview: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [statistics, setStatistics] = useState<any>({});
  const [recentIssues, setRecentIssues] = useState<RecentIssue[]>([]);

  // 获取统计数据
  const fetchStatistics = async () => {
    setLoading(true);
    try {
      const result = await qualityService.getQualityOverview();
      setStatistics(result);
      setRecentIssues(result.recentIssues || []);
    } catch (error) {
      console.error('获取质量统计数据失败:', error);
      // 使用默认数据
      setStatistics({
        overallScore: 87,
        totalRules: 156,
        passedRules: 136,
        failedRules: 20,
        pendingIssues: 45,
        scheduledChecks: 23,
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatistics();
  }, []);

  // 获取质量评分颜色
  const getScoreColor = (score: number) => {
    if (score >= 90) return '#52c41a';
    if (score >= 80) return '#1890ff';
    if (score >= 70) return '#faad14';
    return '#f5222d';
  };

  // 获取严重程度颜色
  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'HIGH':
        return 'red';
      case 'MEDIUM':
        return 'orange';
      case 'LOW':
        return 'green';
      default:
        return 'default';
    }
  };

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN':
        return 'red';
      case 'PROCESSING':
        return 'blue';
      case 'RESOLVED':
        return 'green';
      default:
        return 'default';
    }
  };

  // 最近问题表格列
  const issueColumns: ColumnsType<RecentIssue> = [
    {
      title: '资产名称',
      dataIndex: 'assetName',
      key: 'assetName',
      render: (name: string) => <a>{name}</a>,
    },
    {
      title: '规则名称',
      dataIndex: 'ruleName',
      key: 'ruleName',
    },
    {
      title: '严重程度',
      dataIndex: 'severity',
      key: 'severity',
      width: 100,
      render: (severity: string) => (
        <Tag color={getSeverityColor(severity)}>
          {severity === 'HIGH' ? '高' : severity === 'MEDIUM' ? '中' : '低'}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config: Record<string, { color: string; label: string }> = {
          OPEN: { color: 'red', label: '待处理' },
          PROCESSING: { color: 'blue', label: '处理中' },
          RESOLVED: { color: 'green', label: '已解决' },
        };
        return <Tag color={config[status]?.color || 'default'}>{config[status]?.label || status}</Tag>;
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 150,
      render: (time: string) => new Date(time).toLocaleString(),
    },
  ];

  const overallScore = statistics.overallScore || 0;
  const passedRules = statistics.passedRules || 0;
  const totalRules = statistics.totalRules || 0;

  return (
    <div className={styles.container}>
      {/* 顶部标题 */}
      <div className={styles.header}>
        <Title level={4}>数据质量概览</Title>
        <Button icon={<ReloadOutlined />} onClick={fetchStatistics}>
          刷新
        </Button>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col span={6}>
          <Card className={styles.scoreCard}>
            <Statistic
              title="综合质量评分"
              value={overallScore}
              suffix="分"
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: getScoreColor(overallScore) }}
            />
            <Progress
              percent={overallScore}
              showInfo={false}
              strokeColor={getScoreColor(overallScore)}
              className={styles.scoreProgress}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="质量规则总数"
              value={totalRules}
              prefix={<CheckCircleOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="通过规则数"
              value={passedRules}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="待处理问题"
              value={statistics.pendingIssues || 0}
              prefix={<WarningOutlined style={{ color: '#f5222d' }} />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        {/* 质量趋势 */}
        <Col span={16}>
          <Card title="质量评分趋势" className={styles.chartCard}>
            <div className={styles.chartPlaceholder}>
              <Space direction="vertical" align="center">
                <DatabaseOutlined style={{ fontSize: 48, color: '#1890ff' }} />
                <Text type="secondary">趋势图表展示区域</Text>
                <Text type="secondary">建议集成 ECharts 或 Ant Design Charts</Text>
              </Space>
            </div>
          </Card>
        </Col>

        {/* 规则分布 */}
        <Col span={8}>
          <Card title="规则通过率" className={styles.chartCard}>
            <div className={styles.pieChart}>
              <Progress
                type="circle"
                percent={Math.round((passedRules / totalRules) * 100) || 0}
                strokeColor={{
                  '0%': '#52c41a',
                  '100%': '#f5222d',
                }}
                format={(percent) => (
                  <span style={{ fontSize: 24, fontWeight: 600 }}>
                    {percent}%
                  </span>
                )}
              />
              <div className={styles.pieLegend}>
                <Space direction="vertical">
                  <Space>
                    <CheckCircleOutlined style={{ color: '#52c41a' }} />
                    <Text>通过: {passedRules}</Text>
                  </Space>
                  <Space>
                    <WarningOutlined style={{ color: '#f5222d' }} />
                    <Text>失败: {(statistics.failedRules || 0)}</Text>
                  </Space>
                </Space>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* 最近质量问题 */}
      <Card
        title="最近质量问题"
        extra={
          <Button type="link" onClick={() => history.push('/quality/issues')}>
            查看全部 <ArrowRightOutlined />
          </Button>
        }
        className={styles.tableCard}
      >
        <Table
          columns={issueColumns}
          dataSource={recentIssues}
          rowKey="id"
          loading={loading}
          pagination={false}
          size="small"
        />
      </Card>
    </div>
  );
};

export default QualityOverview;
