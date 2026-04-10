/**
 * 首页/工作台
 */

import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Table, Tag, Typography, Space, Button, Progress, List, Avatar } from 'antd';
import {
  DatabaseOutlined,
  FolderOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  RiseOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  StarOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons';
import { history, useNavigate } from '@umijs/max';
import type { ColumnsType } from 'antd/es/table';
import * as statisticsService from '../../services/statistics';
import * as assetService from '../../services/asset';
import type { DataAsset } from '../../types';
import styles from './index.less';

const { Title, Text } = Typography;

// 统计数据卡片配置
const statisticsCards = [
  { key: 'totalAssets', title: '数据资产总量', icon: <DatabaseOutlined />, color: '#1890ff' },
  { key: 'totalTables', title: '数据表数量', icon: <FolderOutlined />, color: '#52c41a' },
  { key: 'qualityScore', title: '数据质量评分', icon: <CheckCircleOutlined />, color: '#faad14', suffix: '%' },
  { key: 'qualityIssues', title: '待处理质量问题', icon: <WarningOutlined />, color: '#f5222d' },
];

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [statistics, setStatistics] = useState<any>({});
  const [recentAssets, setRecentAssets] = useState<DataAsset[]>([]);
  const [qualityOverview, setQualityOverview] = useState<any>({});

  // 获取统计数据
  const fetchStatistics = async () => {
    try {
      const [assetStats, qualityStats] = await Promise.all([
        statisticsService.getAssetStatistics(),
        statisticsService.getQualityStatistics(),
      ]);

      setStatistics({
        totalAssets: assetStats.totalAssets || 0,
        totalTables: assetStats.totalTables || 0,
        qualityScore: qualityStats.overallScore || 0,
        qualityIssues: qualityStats.pendingIssues || 0,
      });

      setQualityOverview({
        totalRules: qualityStats.totalRules || 0,
        passedRules: qualityStats.passedRules || 0,
        failedRules: qualityStats.failedRules || 0,
        scheduledChecks: qualityStats.scheduledChecks || 0,
      });
    } catch (error) {
      console.error('获取统计数据失败:', error);
    }
  };

  // 获取最近访问的资产
  const fetchRecentAssets = async () => {
    try {
      const result = await assetService.getRecentAssets({ pageSize: 5 });
      setRecentAssets(result.items || []);
    } catch (error) {
      console.error('获取最近资产失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatistics();
    fetchRecentAssets();
  }, []);

  // 跳转到资产详情
  const handleAssetClick = (id: string) => {
    navigate(`/assets/detail/${id}`);
  };

  // 资产表格列配置
  const recentAssetColumns: ColumnsType<DataAsset> = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <a onClick={() => handleAssetClick(record.id)}>{name}</a>
      ),
    },
    {
      title: '类型',
      dataIndex: 'assetType',
      key: 'assetType',
      width: 100,
      render: (type: string) => {
        const typeMap: Record<string, { color: string; label: string }> = {
          TABLE: { color: 'blue', label: '数据表' },
          VIEW: { color: 'green', label: '视图' },
          FILE: { color: 'orange', label: '文件' },
          API: { color: 'purple', label: 'API' },
          STREAM: { color: 'cyan', label: '流数据' },
        };
        const config = typeMap[type] || { color: 'default', label: type };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '敏感级别',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      width: 100,
      render: (level: string) => {
        const levelMap: Record<string, { color: string; label: string }> = {
          PUBLIC: { color: 'green', label: '公开' },
          INTERNAL: { color: 'blue', label: '内部' },
          CONFIDENTIAL: { color: 'orange', label: '机密' },
          RESTRICTED: { color: 'red', label: '限制' },
        };
        const config = levelMap[level] || { color: 'default', label: level };
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 120,
      render: (time: string) => time ? new Date(time).toLocaleDateString() : '-',
    },
  ];

  // 质量概览数据
  const qualityItems = [
    {
      title: '总规则数',
      value: qualityOverview.totalRules || 0,
      icon: <CheckCircleOutlined />,
      color: '#1890ff',
    },
    {
      title: '通过规则',
      value: qualityOverview.passedRules || 0,
      icon: <CheckCircleOutlined />,
      color: '#52c41a',
    },
    {
      title: '失败规则',
      value: qualityOverview.failedRules || 0,
      icon: <WarningOutlined />,
      color: '#f5222d',
    },
    {
      title: '定时检查',
      value: qualityOverview.scheduledChecks || 0,
      icon: <ClockCircleOutlined />,
      color: '#faad14',
    },
  ];

  // 快速入口配置
  const quickActions = [
    {
      title: '注册资产',
      icon: <DatabaseOutlined />,
      path: '/assets/create',
      color: '#1890ff',
    },
    {
      title: '资产目录',
      icon: <FolderOutlined />,
      path: '/catalog/tree',
      color: '#52c41a',
    },
    {
      title: '血缘分析',
      icon: <EyeOutlined />,
      path: '/lineage/graph',
      color: '#722ed1',
    },
    {
      title: '质量检查',
      icon: <CheckCircleOutlined />,
      path: '/quality/overview',
      color: '#faad14',
    },
  ];

  return (
    <div className={styles.container}>
      {/* 欢迎区域 */}
      <div className={styles.welcome}>
        <div>
          <Title level={3} className={styles.welcomeTitle}>
            欢迎回来 👋
          </Title>
          <Text type="secondary">
            这里展示您的数据资产概览和最近活动
          </Text>
        </div>
        <Space>
          <Button onClick={() => navigate('/assets/create')}>
            <DatabaseOutlined /> 注册资产
          </Button>
          <Button type="primary" onClick={() => navigate('/lineage/graph')}>
            <EyeOutlined /> 数据血缘
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className={styles.statisticsRow}>
        {statisticsCards.map((card) => (
          <Col xs={24} sm={12} lg={6} key={card.key}>
            <Card className={styles.statisticCard} loading={loading}>
              <Statistic
                title={card.title}
                value={statistics[card.key] || 0}
                prefix={<span style={{ color: card.color }}>{card.icon}</span>}
                suffix={card.suffix}
                valueStyle={{ color: card.color }}
              />
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        {/* 质量概览 */}
        <Col xs={24} lg={12}>
          <Card
            title="数据质量概览"
            extra={
              <Button type="link" onClick={() => navigate('/quality/overview')}>
                查看详情 <ArrowRightOutlined />
              </Button>
            }
            className={styles.card}
          >
            <Row gutter={16}>
              {qualityItems.map((item, index) => (
                <Col span={12} key={index} className={styles.qualityItem}>
                  <Space align="start">
                    <span style={{ color: item.color }}>{item.icon}</span>
                    <div>
                      <Text type="secondary">{item.title}</Text>
                      <div className={styles.qualityValue}>{item.value}</div>
                    </div>
                  </Space>
                </Col>
              ))}
            </Row>
            
            {qualityOverview.totalRules > 0 && (
              <div className={styles.qualityProgress}>
                <Text type="secondary">质量评分</Text>
                <Progress
                  percent={Math.round(
                    ((qualityOverview.passedRules || 0) / (qualityOverview.totalRules || 1)) * 100
                  )}
                  strokeColor="#52c41a"
                  format={(percent) => <span style={{ color: '#52c41a' }}>{percent}%</span>}
                />
              </div>
            )}
          </Card>
        </Col>

        {/* 快速入口 */}
        <Col xs={24} lg={12}>
          <Card title="快速入口" className={styles.card}>
            <Row gutter={[16, 16]}>
              {quickActions.map((action, index) => (
                <Col xs={12} sm={6} key={index}>
                  <div
                    className={styles.quickAction}
                    onClick={() => navigate(action.path)}
                    style={{ '--action-color': action.color } as React.CSSProperties}
                  >
                    <div className={styles.quickActionIcon} style={{ backgroundColor: action.color }}>
                      {action.icon}
                    </div>
                    <Text className={styles.quickActionTitle}>{action.title}</Text>
                  </div>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>
      </Row>

      {/* 最近访问 */}
      <Card
        title="最近访问"
        extra={
          <Button type="link" onClick={() => navigate('/assets/list')}>
            查看全部 <ArrowRightOutlined />
          </Button>
        }
        className={styles.card}
      >
        <Table
          columns={recentAssetColumns}
          dataSource={recentAssets}
          rowKey="id"
          loading={loading}
          pagination={false}
          size="middle"
        />
      </Card>
    </div>
  );
};

export default Home;
