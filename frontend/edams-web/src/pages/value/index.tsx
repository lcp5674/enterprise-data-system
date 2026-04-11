/**
 * 数据价值评估页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Button,
  Space,
  Progress,
  Tag,
  Modal,
  Descriptions,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  ReloadOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import styles from './index.less';

interface AssetValue {
  id: string;
  name: string;
  type: string;
  valueScore: number;
  usageFrequency: number;
  updateTimeliness: number;
  completenessScore: number;
  valueLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  lastAccessTime?: string;
}

interface ValueDistribution {
  level: string;
  count: number;
  percentage: number;
  color: string;
}

const ValueAssessment: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<AssetValue[]>([]);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState<AssetValue | null>(null);

  // 模拟数据
  const mockAssets: AssetValue[] = [
    { id: '1', name: '客户信息表', type: 'TABLE', valueScore: 95, usageFrequency: 98, updateTimeliness: 92, completenessScore: 98, valueLevel: 'HIGH', lastAccessTime: '2026-04-11' },
    { id: '2', name: '订单事实表', type: 'TABLE', valueScore: 92, usageFrequency: 95, updateTimeliness: 88, completenessScore: 95, valueLevel: 'HIGH', lastAccessTime: '2026-04-11' },
    { id: '3', name: '销售报表API', type: 'API', valueScore: 88, usageFrequency: 85, updateTimeliness: 90, completenessScore: 88, valueLevel: 'HIGH', lastAccessTime: '2026-04-10' },
    { id: '4', name: '商品目录表', type: 'TABLE', valueScore: 78, usageFrequency: 72, updateTimeliness: 80, completenessScore: 75, valueLevel: 'MEDIUM', lastAccessTime: '2026-04-09' },
    { id: '5', name: '库存快照表', type: 'TABLE', valueScore: 65, usageFrequency: 55, updateTimeliness: 70, completenessScore: 68, valueLevel: 'MEDIUM', lastAccessTime: '2026-04-08' },
    { id: '6', name: '日志归档表', type: 'TABLE', valueScore: 45, usageFrequency: 30, updateTimeliness: 50, completenessScore: 60, valueLevel: 'LOW', lastAccessTime: '2026-04-01' },
    { id: '7', name: '测试数据表', type: 'TABLE', valueScore: 25, usageFrequency: 10, updateTimeliness: 20, completenessScore: 30, valueLevel: 'LOW', lastAccessTime: '2026-03-15' },
  ];

  // 价值分布
  const valueDistribution: ValueDistribution[] = [
    { level: '高价值', count: 3, percentage: 43, color: '#52c41a' },
    { level: '中价值', count: 2, percentage: 28, color: '#faad14' },
    { level: '低价值', count: 2, percentage: 29, color: '#8c8c8c' },
  ];

  const overallScore = Math.round(mockAssets.reduce((sum, a) => sum + a.valueScore, 0) / mockAssets.length);

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDataSource(mockAssets);
      setLoading(false);
    }, 500);
  }, []);

  // 查看详情
  const handleViewDetail = (record: AssetValue) => {
    setSelectedAsset(record);
    setDetailVisible(true);
  };

  // 重新评估
  const handleReassess = () => {
    message.success('已触发重新评估任务，请稍后查看结果');
  };

  // 获取价值等级标签
  const getLevelTag = (level: string) => {
    const config: Record<string, { color: string; label: string }> = {
      HIGH: { color: 'green', label: '高价值' },
      MEDIUM: { color: 'gold', label: '中价值' },
      LOW: { color: 'default', label: '低价值' },
    };
    return config[level] || { color: 'default', label: level };
  };

  // 获取价值评分颜色
  const getScoreColor = (score: number) => {
    if (score >= 80) return '#52c41a';
    if (score >= 60) return '#faad14';
    return '#8c8c8c';
  };

  // 表格列配置
  const columns: ColumnsType<AssetValue> = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          {name}
          <Tag>{record.type}</Tag>
        </Space>
      ),
    },
    {
      title: '价值分',
      dataIndex: 'valueScore',
      key: 'valueScore',
      width: 120,
      sorter: (a, b) => a.valueScore - b.valueScore,
      render: (score: number) => (
        <Space>
          <Progress percent={score} size="small" strokeColor={getScoreColor(score)} showInfo={false} style={{ width: 60 }} />
          <span style={{ color: getScoreColor(score), fontWeight: 500 }}>{score}</span>
        </Space>
      ),
    },
    {
      title: '价值等级',
      dataIndex: 'valueLevel',
      key: 'valueLevel',
      width: 100,
      render: (level: string) => {
        const config = getLevelTag(level);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '使用频率',
      dataIndex: 'usageFrequency',
      key: 'usageFrequency',
      width: 120,
      render: (freq: number) => `${freq}%`,
    },
    {
      title: '更新及时性',
      dataIndex: 'updateTimeliness',
      key: 'updateTimeliness',
      width: 120,
      render: (timeliness: number) => `${timeliness}%`,
    },
    {
      title: '完整性得分',
      dataIndex: 'completenessScore',
      key: 'completenessScore',
      width: 120,
      render: (score: number) => `${score}%`,
    },
    {
      title: '最近访问',
      dataIndex: 'lastAccessTime',
      key: 'lastAccessTime',
      width: 120,
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button type="text" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
          详情
        </Button>
      ),
    },
  ];

  // 评分说明
  const scoreDescriptions = [
    { score: '80-100', label: '高价值资产', desc: '核心业务资产，需重点保护，优先保障质量' },
    { score: '60-79', label: '中价值资产', desc: '重要业务资产，定期评估质量和使用价值' },
    { score: '<60', label: '低价值资产', desc: '可优化资产，考虑降级或归档处理' },
  ];

  return (
    <div className={styles.container}>
      {/* 顶部统计卡片 */}
      <Row gutter={16} className={styles.statsRow}>
        <Col span={6}>
          <Card>
            <Statistic title="总资产价值分" value={overallScore} suffix="分" valueStyle={{ color: overallScore >= 80 ? '#52c41a' : overallScore >= 60 ? '#faad14' : '#8c8c8c' }} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="高价值资产数" value={mockAssets.filter(a => a.valueLevel === 'HIGH').length} valueStyle={{ color: '#52c41a' }} suffix="个" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="中价值资产数" value={mockAssets.filter(a => a.valueLevel === 'MEDIUM').length} valueStyle={{ color: '#faad14' }} suffix="个" />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="低价值资产数" value={mockAssets.filter(a => a.valueLevel === 'LOW').length} valueStyle={{ color: '#8c8c8c' }} suffix="个" />
          </Card>
        </Col>
      </Row>

      {/* 图表和表格区域 */}
      <Row gutter={16}>
        {/* 左侧饼图 */}
        <Col span={8}>
          <Card title="资产价值分布" className={styles.chartCard}>
            <div style={{ padding: '20px 0' }}>
              {valueDistribution.map(item => (
                <div key={item.level} style={{ marginBottom: 24 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                    <span>{item.level}</span>
                    <span style={{ color: item.color, fontWeight: 500 }}>{item.count} 个 ({item.percentage}%)</span>
                  </div>
                  <Progress percent={item.percentage} strokeColor={item.color} showInfo={false} />
                </div>
              ))}
            </div>
            <div style={{ textAlign: 'center', marginTop: 20 }}>
              <div style={{ fontSize: 48, fontWeight: 'bold', color: '#1890ff' }}>{mockAssets.length}</div>
              <div style={{ color: '#666' }}>总资产数</div>
            </div>
          </Card>
        </Col>

        {/* 右侧表格 */}
        <Col span={16}>
          <Card
            title="价值评分详情"
            extra={
              <Button icon={<ReloadOutlined />} onClick={handleReassess}>
                重新评估
              </Button>
            }
            className={styles.tableCard}
          >
            <Table
              columns={columns}
              dataSource={dataSource}
              rowKey="id"
              loading={loading}
              pagination={{ pageSize: 5, showTotal: (total) => `共 ${total} 条资产` }}
            />
          </Card>
        </Col>
      </Row>

      {/* 底部评分说明 */}
      <Card title="评分说明" className={styles.descCard}>
        <Table
          columns={[
            { title: '评分范围', dataIndex: 'score', key: 'score', width: 120 },
            { title: '等级', dataIndex: 'label', key: 'label', width: 120 },
            { title: '说明', dataIndex: 'desc', key: 'desc' },
          ]}
          dataSource={scoreDescriptions}
          pagination={false}
          rowKey="score"
        />
      </Card>

      {/* 详情弹窗 */}
      <Modal title="资产价值详情" open={detailVisible} onCancel={() => setDetailVisible(false)} footer={null} width={600}>
        {selectedAsset && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="资产名称">{selectedAsset.name}</Descriptions.Item>
            <Descriptions.Item label="资产类型">{selectedAsset.type}</Descriptions.Item>
            <Descriptions.Item label="价值等级">
              <Tag color={getLevelTag(selectedAsset.valueLevel).color}>{getLevelTag(selectedAsset.valueLevel).label}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="综合价值分">
              <span style={{ color: getScoreColor(selectedAsset.valueScore), fontWeight: 'bold', fontSize: 18 }}>{selectedAsset.valueScore}</span>
            </Descriptions.Item>
            <Descriptions.Item label="使用频率评分">{selectedAsset.usageFrequency}%</Descriptions.Item>
            <Descriptions.Item label="更新及时性评分">{selectedAsset.updateTimeliness}%</Descriptions.Item>
            <Descriptions.Item label="完整性评分">{selectedAsset.completenessScore}%</Descriptions.Item>
            <Descriptions.Item label="最近访问时间">{selectedAsset.lastAccessTime}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default ValueAssessment;
