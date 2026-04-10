/**
 * 资产详情页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Tag,
  Space,
  Button,
  Tabs,
  Table,
  Typography,
  Row,
  Col,
  Breadcrumb,
  Spin,
  message,
  Dropdown,
  Tooltip,
} from 'antd';
import type { MenuProps } from 'antd';
import type { TabsProps } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  ShareAltOutlined,
  StarOutlined,
  StarFilled,
  DownloadOutlined,
  HistoryOutlined,
  EyeOutlined,
  MoreOutlined,
  DatabaseOutlined,
  FieldStringOutlined,
  UserOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { history, useNavigate, useParams, useSearchParams } from '@umijs/max';
import * as assetService from '../../services/asset';
import * as lineageService from '../../services/lineage';
import { ASSET_TYPE_OPTIONS, SENSITIVITY_LEVEL_OPTIONS } from '../../types';
import type { DataAsset, DataField, LineageGraph } from '../../types';
import styles from './index.less';

const { Title, Text, Paragraph } = Typography;

const AssetDetail: React.FC = () => {
  const navigate = useNavigate();
  const params = useParams();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(true);
  const [asset, setAsset] = useState<DataAsset | null>(null);
  const [fields, setFields] = useState<DataField[]>([]);
  const [lineage, setLineage] = useState<LineageGraph | null>(null);
  const [activeTab, setActiveTab] = useState('info');

  const isEditMode = searchParams.get('mode') === 'edit';
  const assetId = params.id as string;

  // 获取资产详情
  const fetchAssetDetail = async () => {
    if (!assetId) return;
    
    setLoading(true);
    try {
      const [assetResult, fieldsResult] = await Promise.all([
        assetService.getAssetDetail(assetId),
        assetService.getAssetFields(assetId),
      ]);
      
      setAsset(assetResult);
      setFields(fieldsResult.items || []);
    } catch (error) {
      console.error('获取资产详情失败:', error);
      message.error('获取资产详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取血缘信息
  const fetchLineage = async () => {
    if (!assetId) return;
    
    try {
      const result = await lineageService.getLineageGraph({ assetId });
      setLineage(result);
    } catch (error) {
      console.error('获取血缘信息失败:', error);
    }
  };

  useEffect(() => {
    fetchAssetDetail();
  }, [assetId]);

  useEffect(() => {
    if (activeTab === 'lineage') {
      fetchLineage();
    }
  }, [activeTab, assetId]);

  // 收藏/取消收藏
  const handleToggleFavorite = async () => {
    if (!asset) return;
    
    try {
      if (asset.isFavorite) {
        await assetService.removeFromFavorites(assetId);
        message.success('已取消收藏');
      } else {
        await assetService.addToFavorites(assetId);
        message.success('已添加到收藏');
      }
      fetchAssetDetail();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 删除资产
  const handleDelete = () => {
    // TODO: 实现删除逻辑
    message.info('删除功能开发中');
  };

  // 导出资产
  const handleExport = () => {
    message.info('导出功能开发中');
  };

  // 操作菜单
  const actionMenu: MenuProps = {
    items: [
      {
        key: 'edit',
        icon: <EditOutlined />,
        label: '编辑',
        onClick: () => navigate(`/assets/detail/${assetId}?mode=edit`),
      },
      {
        key: 'lineage',
        icon: <ShareAltOutlined />,
        label: '查看血缘',
        onClick: () => navigate(`/lineage/graph?assetId=${assetId}`),
      },
      {
        key: 'export',
        icon: <DownloadOutlined />,
        label: '导出',
        onClick: handleExport,
      },
      { type: 'divider' },
      {
        key: 'delete',
        icon: <DeleteOutlined />,
        label: '删除',
        danger: true,
        onClick: handleDelete,
      },
    ],
  };

  // 字段表格列配置
  const fieldColumns: ColumnsType<DataField> = [
    {
      title: '字段名',
      dataIndex: 'name',
      key: 'name',
      width: 180,
      render: (name: string) => <Text code>{name}</Text>,
    },
    {
      title: '中文名',
      dataIndex: 'comment',
      key: 'comment',
      width: 150,
      ellipsis: true,
    },
    {
      title: '数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 120,
      render: (type: string) => <Tag>{type}</Tag>,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
  ];

  // Tab 配置
  const tabItems: TabsProps['items'] = [
    {
      key: 'info',
      label: '基本信息',
      children: (
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="资产名称">{asset?.name}</Descriptions.Item>
          <Descriptions.Item label="资产类型">
            {asset?.assetType && (
              (() => {
                const config = ASSET_TYPE_OPTIONS.find((o) => o.value === asset.assetType);
                return config ? <Tag color={config.color}>{config.label}</Tag> : asset.assetType;
              })()
            )}
          </Descriptions.Item>
          <Descriptions.Item label="敏感级别">
            {asset?.sensitivityLevel && (
              (() => {
                const config = SENSITIVITY_LEVEL_OPTIONS.find((o) => o.value === asset.sensitivityLevel);
                return config ? <Tag color={config.color}>{config.label}</Tag> : asset.sensitivityLevel;
              })()
            )}
          </Descriptions.Item>
          <Descriptions.Item label="所属域">{asset?.domainName || '-'}</Descriptions.Item>
          <Descriptions.Item label="Owner">{asset?.ownerName || '-'}</Descriptions.Item>
          <Descriptions.Item label="创建者">{asset?.createBy || '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间" span={2}>
            {asset?.createTime ? new Date(asset.createTime).toLocaleString() : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="更新时间" span={2}>
            {asset?.updateTime ? new Date(asset.updateTime).toLocaleString() : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="描述" span={2}>
            {asset?.description || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="标签" span={2}>
            {asset?.tags?.map((tag) => (
              <Tag key={tag} color="blue">{tag}</Tag>
            )) || '-'}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'fields',
      label: `字段信息 (${fields.length})`,
      children: (
        <Table
          columns={fieldColumns}
          dataSource={fields}
          rowKey="name"
          pagination={false}
          size="small"
        />
      ),
    },
    {
      key: 'lineage',
      label: '血缘关系',
      children: (
        <div className={styles.lineagePlaceholder}>
          <Space direction="vertical" align="center">
            <ShareAltOutlined style={{ fontSize: 48, color: '#1890ff' }} />
            <Title level={5}>血缘图谱</Title>
            <Text type="secondary">
              点击下方按钮在新页面查看完整血缘图谱
            </Text>
            <Button
              type="primary"
              onClick={() => navigate(`/lineage/graph?assetId=${assetId}`)}
            >
              查看血缘图谱
            </Button>
          </Space>
        </div>
      ),
    },
    {
      key: 'history',
      label: '变更历史',
      children: (
        <div className={styles.historyPlaceholder}>
          <Space direction="vertical" align="center">
            <HistoryOutlined style={{ fontSize: 48, color: '#1890ff' }} />
            <Title level={5}>变更历史</Title>
            <Text type="secondary">
              暂无变更记录
            </Text>
          </Space>
        </div>
      ),
    },
  ];

  if (loading) {
    return (
      <div className={styles.loadingContainer}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  if (!asset) {
    return (
      <div className={styles.loadingContainer}>
        <Text>资产不存在</Text>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {/* 顶部导航 */}
      <div className={styles.header}>
        <Space>
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/assets/list')}
          >
            返回
          </Button>
          <Breadcrumb
            items={[
              { title: '资产管理' },
              { title: <a onClick={() => navigate('/assets/list')}>资产列表</a> },
              { title: asset.name },
            ]}
          />
        </Space>
        
        <Space>
          <Tooltip title={asset.isFavorite ? '取消收藏' : '添加收藏'}>
            <Button
              icon={asset.isFavorite ? <StarFilled style={{ color: '#faad14' }} /> : <StarOutlined />}
              onClick={handleToggleFavorite}
            />
          </Tooltip>
          <Button
            icon={<ShareAltOutlined />}
            onClick={() => navigate(`/lineage/graph?assetId=${assetId}`)}
          >
            血缘
          </Button>
          <Dropdown menu={actionMenu} trigger={['click']}>
            <Button icon={<MoreOutlined />} />
          </Dropdown>
          <Button
            type="primary"
            icon={<EditOutlined />}
            onClick={() => navigate(`/assets/detail/${assetId}?mode=edit`)}
          >
            编辑
          </Button>
        </Space>
      </div>

      {/* 资产标题 */}
      <Card className={styles.titleCard}>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <Space align="center">
              <DatabaseOutlined style={{ fontSize: 32, color: '#1890ff' }} />
              <div>
                <Title level={4} style={{ marginBottom: 4 }}>
                  {asset.name}
                  {asset.isFavorite && (
                    <StarFilled style={{ color: '#faad14', marginLeft: 8, fontSize: 16 }} />
                  )}
                </Title>
                <Space size="middle">
                  <Text type="secondary">ID: {asset.id}</Text>
                  {asset.assetType && (
                    (() => {
                      const config = ASSET_TYPE_OPTIONS.find((o) => o.value === asset.assetType);
                      return config ? (
                        <Tag color={config.color}>{config.label}</Tag>
                      ) : null;
                    })()
                  )}
                  {asset.sensitivityLevel && (
                    (() => {
                      const config = SENSITIVITY_LEVEL_OPTIONS.find((o) => o.value === asset.sensitivityLevel);
                      return config ? (
                        <Tag color={config.color}>{config.label}</Tag>
                      ) : null;
                    })()
                  )}
                </Space>
              </div>
            </Space>
          </Col>
        </Row>
        
        <Row gutter={24} className={styles.metaInfo}>
          <Col span={6}>
            <Space direction="vertical" size={0}>
              <Text type="secondary"><UserOutlined /> Owner</Text>
              <Text strong>{asset.ownerName || '-'}</Text>
            </Space>
          </Col>
          <Col span={6}>
            <Space direction="vertical" size={0}>
              <Text type="secondary"><DatabaseOutlined /> 所属域</Text>
              <Text strong>{asset.domainName || '-'}</Text>
            </Space>
          </Col>
          <Col span={6}>
            <Space direction="vertical" size={0}>
              <Text type="secondary"><FieldStringOutlined /> 字段数</Text>
              <Text strong>{fields.length}</Text>
            </Space>
          </Col>
          <Col span={6}>
            <Space direction="vertical" size={0}>
              <Text type="secondary"><ClockCircleOutlined /> 更新时间</Text>
              <Text strong>
                {asset.updateTime ? new Date(asset.updateTime).toLocaleString() : '-'}
              </Text>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* Tab 内容 */}
      <Card className={styles.contentCard}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
        />
      </Card>
    </div>
  );
};

export default AssetDetail;
