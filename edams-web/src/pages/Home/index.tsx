/**
 * 首页/工作台
 */
import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Table, Tag, Space, Button, Typography, List, Avatar } from 'antd';
import {
  DatabaseOutlined,
  FileTextOutlined,
  ApiOutlined,
  SafetyOutlined,
  ArrowRightOutlined,
  CalendarOutlined,
  BellOutlined,
} from '@ant-design/icons';
import { useNavigate } from '@umijs/max';
import StatisticsCard from '@/components/StatisticsCard';
import {
  AssetGrowthChart,
  AssetDistributionChart,
  QualityTrendChart,
  IssueTrendChart,
} from '@/components/Charts';
import type { ColumnsType } from 'antd/es/table';
import type { DataAsset } from '@/types';
import styles from './index.less';

const { Title, Text } = Typography;

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalAssets: 0,
    totalTables: 0,
    totalApis: 0,
    totalFiles: 0,
    qualityScore: 0,
    pendingIssues: 0,
  });

  useEffect(() => {
    // 模拟加载数据
    setTimeout(() => {
      setStats({
        totalAssets: 1256,
        totalTables: 856,
        totalApis: 320,
        totalFiles: 80,
        qualityScore: 85,
        pendingIssues: 23,
      });
      setLoading(false);
    }, 500);
  }, []);

  const recentAssets: DataAsset[] = [
    {
      id: '1',
      name: 'customer_info',
      type: 'TABLE',
      description: '客户信息主表',
      sensitivityLevel: 'SENSITIVE',
      owner: { id: '1', username: 'zhangsan', realName: '张三' },
      createdAt: '2024-01-15',
    },
    {
      id: '2',
      name: 'order_service',
      type: 'API',
      description: '订单查询服务API',
      sensitivityLevel: 'INTERNAL',
      owner: { id: '2', username: 'lisi', realName: '李四' },
      createdAt: '2024-01-14',
    },
    {
      id: '3',
      name: 'product_catalog',
      type: 'TABLE',
      description: '产品目录表',
      sensitivityLevel: 'PUBLIC',
      owner: { id: '3', username: 'wangwu', realName: '王五' },
      createdAt: '2024-01-13',
    },
    {
      id: '4',
      name: 'sales_report',
      type: 'FILE',
      description: '销售报表导出文件',
      sensitivityLevel: 'INTERNAL',
      owner: { id: '4', username: 'zhaoliu', realName: '赵六' },
      createdAt: '2024-01-12',
    },
  ];

  const quickActions = [
    { name: '注册资产', icon: <DatabaseOutlined />, path: '/assets/create', color: '#1890ff' },
    { name: '资产搜索', icon: <FileTextOutlined />, path: '/assets/list', color: '#52c41a' },
    { name: '查看血缘', icon: <ApiOutlined />, path: '/lineage/graph', color: '#722ed1' },
    { name: '质量检测', icon: <SafetyOutlined />, path: '/quality/rules', color: '#fa8c16' },
  ];

  const columns: ColumnsType<DataAsset> = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '类型', dataIndex: 'type', key: 'type', render: (type) => <Tag>{type}</Tag> },
    {
      title: '敏感度',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      render: (level) => {
        const colors: Record<string, string> = {
          PUBLIC: 'green',
          INTERNAL: 'blue',
          SENSITIVE: 'orange',
          HIGHLY_SENSITIVE: 'red',
        };
        return <Tag color={colors[level]}>{level}</Tag>;
      },
    },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button type="link" onClick={() => navigate(`/assets/detail/${record.id}`)}>
          查看详情
        </Button>
      ),
    },
  ];

  const notifications = [
    { id: 1, title: '质量检测完成', desc: 'customer_info 表质量评分 95 分', time: '10分钟前' },
    { id: 2, title: '新资产注册', desc: 'order_history 表已注册', time: '1小时前' },
    { id: 3, title: '血缘更新', desc: 'sales_report 血缘关系已更新', time: '2小时前' },
  ];

  return (
    <div className={styles.homePage}>
      {/* 欢迎区域 */}
      <div className={styles.welcomeSection}>
        <Title level={3}>欢迎回来</Title>
        <Text type="secondary">今天是美好的一天，开始管理您的数据资产吧！</Text>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col xs={24} sm={12} lg={6}>
          <StatisticsCard
            title="资产总数"
            value={stats.totalAssets}
            change={{ value: 12.5, trend: 'up', label: '较上月' }}
            icon={<DatabaseOutlined />}
            color="#1890ff"
            loading={loading}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatisticsCard
            title="数据库表"
            value={stats.totalTables}
            change={{ value: 8.2, trend: 'up' }}
            icon={<FileTextOutlined />}
            color="#52c41a"
            loading={loading}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatisticsCard
            title="API 服务"
            value={stats.totalApis}
            change={{ value: 15.3, trend: 'up' }}
            icon={<ApiOutlined />}
            color="#722ed1"
            loading={loading}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatisticsCard
            title="质量评分"
            value={stats.qualityScore}
            suffix="分"
            precision={1}
            change={{ value: 3.2, trend: 'up' }}
            icon={<SafetyOutlined />}
            color="#fa8c16"
            loading={loading}
          />
        </Col>
      </Row>

      {/* 快捷入口 */}
      <Card title="快捷入口" className={styles.quickActions}>
        <Row gutter={16}>
          {quickActions.map((action) => (
            <Col xs={12} sm={6} key={action.name}>
              <div
                className={styles.quickActionItem}
                onClick={() => navigate(action.path)}
                style={{ borderColor: action.color }}
              >
                <div className={styles.quickActionIcon} style={{ color: action.color }}>
                  {action.icon}
                </div>
                <span>{action.name}</span>
              </div>
            </Col>
          ))}
        </Row>
      </Card>

      {/* 图表区域 */}
      <Row gutter={[16, 16]} className={styles.chartsRow}>
        <Col xs={24} lg={12}>
          <Card title="资产增长趋势">
            <AssetGrowthChart loading={loading} height={300} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="资产分布">
            <AssetDistributionChart loading={loading} height={300} />
          </Card>
        </Col>
      </Row>

      {/* 下方两栏 */}
      <Row gutter={[16, 16]} className={styles.bottomRow}>
        {/* 最近资产 */}
        <Col xs={24} lg={14}>
          <Card
            title="最近资产"
            extra={
              <Button type="link" onClick={() => navigate('/assets/list')}>
                查看全部 <ArrowRightOutlined />
              </Button>
            }
          >
            <Table
              columns={columns}
              dataSource={recentAssets}
              rowKey="id"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>

        {/* 右侧边栏 */}
        <Col xs={24} lg={10}>
          {/* 通知 */}
          <Card title="通知" className={styles.notificationCard}>
            <List
              itemLayout="horizontal"
              dataSource={notifications}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={<Avatar icon={<BellOutlined />} style={{ backgroundColor: '#1890ff' }} />}
                    title={item.title}
                    description={
                      <Space direction="vertical" size={0}>
                        <Text type="secondary">{item.desc}</Text>
                        <Text type="secondary" className={styles.timeText}>
                          <CalendarOutlined /> {item.time}
                        </Text>
                      </Space>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>

          {/* 待办事项 */}
          <Card title="待办事项" className={styles.todoCard}>
            <List
              itemLayout="horizontal"
              dataSource={[
                { id: 1, title: '审核新注册资产', count: 5 },
                { id: 2, title: '处理质量问题', count: 3 },
                { id: 3, title: '更新血缘关系', count: 2 },
              ]}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    <Button type="primary" size="small" key="handle">
                      处理
                    </Button>,
                  ]}
                >
                  <List.Item.Meta
                    title={<Text>{item.title}</Text>}
                    description={<Tag color="blue">{item.count} 项待处理</Tag>}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default HomePage;
