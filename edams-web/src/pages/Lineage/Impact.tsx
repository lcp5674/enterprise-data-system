/**
 * 影响分析页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Table,
  Space,
  Typography,
  Tag,
  Button,
  Input,
  Select,
  Alert,
  Descriptions,
  Divider,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  SearchOutlined,
  ArrowRightOutlined,
  WarningOutlined,
  CheckCircleOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import * as lineageService from '../../services/lineage';
import styles from './index.less';

const { Title, Text, Paragraph } = Typography;
const { Search } = Input;

interface ImpactAsset {
  id: string;
  name: string;
  type: string;
  domainName: string;
  ownerName: string;
  impactLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  usageCount: number;
}

const ImpactAnalysis: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState<any>(null);
  const [impactAssets, setImpactAssets] = useState<ImpactAsset[]>([]);
  const [searchKeyword, setSearchKeyword] = useState('');

  // 模拟影响分析数据
  const mockImpactAssets: ImpactAsset[] = [
    { id: '1', name: 'dim_users', type: 'TABLE', domainName: '用户域', ownerName: '张三', impactLevel: 'HIGH', usageCount: 45 },
    { id: '2', name: 'fact_orders', type: 'TABLE', domainName: '交易域', ownerName: '李四', impactLevel: 'HIGH', usageCount: 38 },
    { id: '3', name: 'rpt_daily_sales', type: 'VIEW', domainName: '分析域', ownerName: '王五', impactLevel: 'MEDIUM', usageCount: 12 },
    { id: '4', name: '/api/order/list', type: 'API', domainName: '交易域', ownerName: '李四', impactLevel: 'LOW', usageCount: 5 },
  ];

  useEffect(() => {
    // 模拟加载
    setImpactAssets(mockImpactAssets);
  }, []);

  // 搜索
  const handleSearch = () => {
    if (!searchKeyword) {
      setImpactAssets(mockImpactAssets);
      return;
    }
    const filtered = mockImpactAssets.filter(
      (asset) => asset.name.toLowerCase().includes(searchKeyword.toLowerCase())
    );
    setImpactAssets(filtered);
  };

  // 选择资产进行影响分析
  const handleAnalyze = async (asset?: any) => {
    setLoading(true);
    try {
      if (asset) {
        setSelectedAsset(asset);
        const result = await lineageService.getImpactAnalysis({ assetId: asset.id });
        setImpactAssets(result.downstream || mockImpactAssets);
      }
    } catch (error) {
      message.error('分析失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取影响级别颜色
  const getImpactLevelColor = (level: string) => {
    switch (level) {
      case 'HIGH':
        return '#f5222d';
      case 'MEDIUM':
        return '#faad14';
      case 'LOW':
        return '#52c41a';
      default:
        return '#8c8c8c';
    }
  };

  // 获取影响级别标签
  const getImpactLevelTag = (level: string) => {
    const config: Record<string, { color: string; label: string }> = {
      HIGH: { color: 'red', label: '高影响' },
      MEDIUM: { color: 'orange', label: '中影响' },
      LOW: { color: 'green', label: '低影响' },
    };
    return config[level] || { color: 'default', label: level };
  };

  // 表格列配置
  const columns: ColumnsType<ImpactAsset> = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <a onClick={() => history.push(`/assets/detail/${record.id}`)}>{name}</a>
          <Tag>{record.type}</Tag>
        </Space>
      ),
    },
    {
      title: '所属域',
      dataIndex: 'domainName',
      key: 'domainName',
      width: 120,
    },
    {
      title: 'Owner',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 100,
    },
    {
      title: '影响级别',
      dataIndex: 'impactLevel',
      key: 'impactLevel',
      width: 100,
      render: (level: string) => {
        const config = getImpactLevelTag(level);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '被引用次数',
      dataIndex: 'usageCount',
      key: 'usageCount',
      width: 120,
      align: 'right',
      render: (count: number) => (
        <Text strong style={{ color: count > 30 ? '#f5222d' : count > 10 ? '#faad14' : '#52c41a' }}>
          {count}
        </Text>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => history.push(`/assets/detail/${record.id}`)}
          >
            详情
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => history.push(`/lineage/graph?assetId=${record.id}`)}
          >
            血缘 <ArrowRightOutlined />
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      {/* 说明 */}
      <Alert
        message="影响分析"
        description="选择一个资产，分析其变更将对哪些下游资产产生影响，帮助评估变更风险。"
        type="info"
        icon={<InfoCircleOutlined />}
        showIcon
        className={styles.alert}
      />

      {/* 搜索区域 */}
      <Card className={styles.searchCard}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Space>
              <Input
                placeholder="输入资产名称"
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onPressEnter={handleSearch}
                style={{ width: 300 }}
                allowClear
              />
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                分析
              </Button>
            </Space>
          </Col>
          {selectedAsset && (
            <Col flex="none">
              <Tag icon={<CheckCircleOutlined />} color="blue">
                当前分析: {selectedAsset.name}
              </Tag>
            </Col>
          )}
        </Row>
      </Card>

      {/* 分析结果 */}
      <Card title="影响分析结果" className={styles.resultCard}>
        {/* 统计信息 */}
        <Row gutter={16} className={styles.statsRow}>
          <Col span={8}>
            <Card size="small" className={styles.statCard}>
              <Space>
                <WarningOutlined style={{ fontSize: 24, color: '#f5222d' }} />
                <div>
                  <Text type="secondary">高影响资产</Text>
                  <div className={styles.statValue} style={{ color: '#f5222d' }}>
                    {impactAssets.filter((a) => a.impactLevel === 'HIGH').length}
                  </div>
                </div>
              </Space>
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small" className={styles.statCard}>
              <Space>
                <WarningOutlined style={{ fontSize: 24, color: '#faad14' }} />
                <div>
                  <Text type="secondary">中影响资产</Text>
                  <div className={styles.statValue} style={{ color: '#faad14' }}>
                    {impactAssets.filter((a) => a.impactLevel === 'MEDIUM').length}
                  </div>
                </div>
              </Space>
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small" className={styles.statCard}>
              <Space>
                <CheckCircleOutlined style={{ fontSize: 24, color: '#52c41a' }} />
                <div>
                  <Text type="secondary">低影响资产</Text>
                  <div className={styles.statValue} style={{ color: '#52c41a' }}>
                    {impactAssets.filter((a) => a.impactLevel === 'LOW').length}
                  </div>
                </div>
              </Space>
            </Card>
          </Col>
        </Row>

        <Divider />

        {/* 下游资产列表 */}
        <Table
          columns={columns}
          dataSource={impactAssets}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showTotal: (total) => `共 ${total} 个下游资产`,
          }}
        />
      </Card>

      {/* 建议 */}
      <Card title="变更建议" className={styles.suggestCard}>
        <Alert
          message="在变更此资产前，建议采取以下措施"
          type="warning"
          showIcon
          action={
            <Button size="small" type="primary">
              查看详细报告
            </Button>
          }
        >
          <ul style={{ marginBottom: 0, paddingLeft: 20 }}>
            <li>通知 {impactAssets.filter((a) => a.impactLevel === 'HIGH').length} 个高影响资产 Owner</li>
            <li>安排联合测试或回归测试</li>
            <li>提前发布变更通知，准备回滚方案</li>
          </ul>
        </Alert>
      </Card>
    </div>
  );
};

export default ImpactAnalysis;
