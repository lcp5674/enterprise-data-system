/**
 * 资产列表页面
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Typography,
  Row,
  Col,
  Dropdown,
  Modal,
  message,
  Tooltip,
} from 'antd';
import type { MenuProps } from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  FilterOutlined,
  StarOutlined,
  StarFilled,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  DownloadOutlined,
  ShareAltOutlined,
  MoreOutlined,
} from '@ant-design/icons';
import { history, useNavigate } from '@umijs/max';
import type { ColumnsType } from 'antd/es/table';
import type { DataAsset, PageResult } from '../../types';
import * as assetService from '../../services/asset';
import { ASSET_TYPE_OPTIONS, SENSITIVITY_LEVEL_OPTIONS } from '../../types';
import styles from './index.less';

const { Title, Text } = Typography;

interface SearchParams {
  keyword: string;
  assetType: string;
  sensitivityLevel: string;
  owner: string;
  domain: string;
}

const AssetList: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<DataAsset[]>([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  });
  const [searchParams, setSearchParams] = useState<SearchParams>({
    keyword: '',
    assetType: '',
    sensitivityLevel: '',
    owner: '',
    domain: '',
  });
  const [showAdvanced, setShowAdvanced] = useState(false);

  // 获取资产列表
  const fetchAssets = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        pageNum: pagination.current,
        pageSize: pagination.pageSize,
        keyword: searchParams.keyword,
        assetType: searchParams.assetType || undefined,
        sensitivityLevel: searchParams.sensitivityLevel || undefined,
        owner: searchParams.owner || undefined,
        domain: searchParams.domain || undefined,
      };

      const result: PageResult<DataAsset> = await assetService.getAssetList(params);

      setDataSource(result.items || []);
      setPagination((prev) => ({
        ...prev,
        total: result.total || 0,
      }));
    } catch (error) {
      console.error('获取资产列表失败:', error);
      message.error('获取资产列表失败');
    } finally {
      setLoading(false);
    }
  }, [pagination.current, pagination.pageSize, searchParams]);

  useEffect(() => {
    fetchAssets();
  }, [fetchAssets]);

  // 搜索
  const handleSearch = () => {
    setPagination((prev) => ({ ...prev, current: 1 }));
    fetchAssets();
  };

  // 重置
  const handleReset = () => {
    setSearchParams({
      keyword: '',
      assetType: '',
      sensitivityLevel: '',
      owner: '',
      domain: '',
    });
    setPagination((prev) => ({ ...prev, current: 1 }));
    fetchAssets();
  };

  // 分页变化
  const handleTableChange = (page: any) => {
    setPagination({
      current: page.current,
      pageSize: page.pageSize,
      total: pagination.total,
    });
  };

  // 收藏/取消收藏
  const handleToggleFavorite = async (record: DataAsset) => {
    try {
      if (record.isFavorite) {
        await assetService.removeFromFavorites(record.id);
        message.success('已取消收藏');
      } else {
        await assetService.addToFavorites(record.id);
        message.success('已添加到收藏');
      }
      fetchAssets();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 删除资产
  const handleDelete = (record: DataAsset) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除资产 "${record.name}" 吗？此操作不可恢复。`,
      okText: '确认删除',
      okType: 'danger',
      onOk: async () => {
        try {
          await assetService.deleteAsset(record.id);
          message.success('删除成功');
          fetchAssets();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  // 导出资产
  const handleExport = (record: DataAsset) => {
    Modal.info({
      title: '导出资产',
      content: `正在准备导出 "${record.name}" 的元数据...`,
    });
  };

  // 操作菜单
  const getActionMenu = (record: DataAsset): MenuProps => ({
    items: [
      {
        key: 'view',
        icon: <EyeOutlined />,
        label: '查看详情',
        onClick: () => navigate(`/assets/detail/${record.id}`),
      },
      {
        key: 'edit',
        icon: <EditOutlined />,
        label: '编辑',
        onClick: () => navigate(`/assets/detail/${record.id}?mode=edit`),
      },
      {
        key: 'lineage',
        icon: <ShareAltOutlined />,
        label: '查看血缘',
        onClick: () => navigate(`/lineage/graph?assetId=${record.id}`),
      },
      { type: 'divider' },
      {
        key: 'favorite',
        icon: record.isFavorite ? <StarFilled /> : <StarOutlined />,
        label: record.isFavorite ? '取消收藏' : '添加收藏',
        onClick: () => handleToggleFavorite(record),
      },
      {
        key: 'export',
        icon: <DownloadOutlined />,
        label: '导出',
        onClick: () => handleExport(record),
      },
      { type: 'divider' },
      {
        key: 'delete',
        icon: <DeleteOutlined />,
        label: '删除',
        danger: true,
        onClick: () => handleDelete(record),
      },
    ],
  });

  // 表格列配置
  const columns: ColumnsType<DataAsset> = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
      fixed: 'left',
      width: 250,
      render: (name: string, record) => (
        <Space direction="vertical" size={0}>
          <a onClick={() => navigate(`/assets/detail/${record.id}`)}>
            <Space>
              {name}
              {record.isFavorite && (
                <StarFilled style={{ color: '#faad14', fontSize: 12 }} />
              )}
            </Space>
          </a>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {record.id}
          </Text>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'assetType',
      key: 'assetType',
      width: 100,
      render: (type: string) => {
        const config = ASSET_TYPE_OPTIONS.find((o) => o.value === type);
        return config ? (
          <Tag color={config.color}>{config.label}</Tag>
        ) : (
          <Tag>{type}</Tag>
        );
      },
    },
    {
      title: '敏感级别',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      width: 100,
      render: (level: string) => {
        const config = SENSITIVITY_LEVEL_OPTIONS.find((o) => o.value === level);
        return config ? (
          <Tag color={config.color}>{config.label}</Tag>
        ) : (
          <Tag>{level}</Tag>
        );
      },
    },
    {
      title: '所属域',
      dataIndex: 'domainName',
      key: 'domainName',
      width: 120,
      ellipsis: true,
    },
    {
      title: 'Owner',
      dataIndex: 'ownerName',
      key: 'ownerName',
      width: 120,
      ellipsis: true,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 160,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="查看">
            <Button
              type="text"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/assets/detail/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="收藏">
            <Button
              type="text"
              size="small"
              icon={record.isFavorite ? <StarFilled style={{ color: '#faad14' }} /> : <StarOutlined />}
              onClick={() => handleToggleFavorite(record)}
            />
          </Tooltip>
          <Dropdown menu={getActionMenu(record)} trigger={['click']}>
            <Button type="text" size="small" icon={<MoreOutlined />} />
          </Dropdown>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      {/* 搜索区域 */}
      <Card className={styles.searchCard}>
        <Row gutter={[16, 16]} align="middle">
          <Col flex="auto">
            <Space wrap>
              <Input
                placeholder="搜索资产名称、描述、标签..."
                prefix={<SearchOutlined />}
                value={searchParams.keyword}
                onChange={(e) =>
                  setSearchParams((prev) => ({ ...prev, keyword: e.target.value }))
                }
                onPressEnter={handleSearch}
                style={{ width: 280 }}
                allowClear
              />
              <Select
                placeholder="资产类型"
                value={searchParams.assetType || undefined}
                onChange={(value) =>
                  setSearchParams((prev) => ({ ...prev, assetType: value || '' }))
                }
                style={{ width: 140 }}
                allowClear
                options={ASSET_TYPE_OPTIONS}
              />
              <Select
                placeholder="敏感级别"
                value={searchParams.sensitivityLevel || undefined}
                onChange={(value) =>
                  setSearchParams((prev) => ({ ...prev, sensitivityLevel: value || '' }))
                }
                style={{ width: 140 }}
                allowClear
                options={SENSITIVITY_LEVEL_OPTIONS}
              />
              <Button
                icon={<FilterOutlined />}
                onClick={() => setShowAdvanced(!showAdvanced)}
              >
                高级筛选
              </Button>
            </Space>

            {showAdvanced && (
              <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
                <Col span={8}>
                  <Input
                    placeholder="Owner"
                    value={searchParams.owner}
                    onChange={(e) =>
                      setSearchParams((prev) => ({ ...prev, owner: e.target.value }))
                    }
                    allowClear
                  />
                </Col>
                <Col span={8}>
                  <Input
                    placeholder="所属域"
                    value={searchParams.domain}
                    onChange={(e) =>
                      setSearchParams((prev) => ({ ...prev, domain: e.target.value }))
                    }
                    allowClear
                  />
                </Col>
              </Row>
            )}
          </Col>
          <Col>
            <Space>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                重置
              </Button>
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                搜索
              </Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/assets/create')}>
                注册资产
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 统计信息 */}
      <div className={styles.statsInfo}>
        <Text type="secondary">
          共找到 <Text strong>{pagination.total}</Text> 个资产
        </Text>
      </div>

      {/* 表格区域 */}
      <Card className={styles.tableCard}>
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          onChange={handleTableChange}
          scroll={{ x: 1300 }}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>
    </div>
  );
};

export default AssetList;
