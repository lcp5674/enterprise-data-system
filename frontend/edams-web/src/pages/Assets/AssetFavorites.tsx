/**
 * 我的收藏页面
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Typography,
  message,
  Empty,
} from 'antd';
import {
  StarFilled,
  EyeOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import type { ColumnsType } from 'antd/es/table';
import type { DataAsset } from '../../types';
import * as assetService from '../../services/asset';
import { ASSET_TYPE_OPTIONS, SENSITIVITY_LEVEL_OPTIONS } from '../../types';
import styles from './index.less';

const { Title, Text } = Typography;

const AssetFavorites: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<DataAsset[]>([]);
  const [total, setTotal] = useState(0);

  // 获取收藏列表
  const fetchFavorites = useCallback(async () => {
    setLoading(true);
    try {
      const result = await assetService.getFavorites({ pageNum: 1, pageSize: 100 });
      setDataSource(result.items || []);
      setTotal(result.total || 0);
    } catch (error) {
      console.error('获取收藏列表失败:', error);
      message.error('获取收藏列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchFavorites();
  }, [fetchFavorites]);

  // 取消收藏
  const handleRemoveFavorite = async (id: string) => {
    try {
      await assetService.removeFromFavorites(id);
      message.success('已取消收藏');
      fetchFavorites();
    } catch (error) {
      message.error('操作失败');
    }
  };

  // 表格列配置
  const columns: ColumnsType<DataAsset> = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <StarFilled style={{ color: '#faad14' }} />
          <a onClick={() => history.push(`/assets/detail/${record.id}`)}>
            {name}
          </a>
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
        return config ? <Tag color={config.color}>{config.label}</Tag> : <Tag>{type}</Tag>;
      },
    },
    {
      title: '敏感级别',
      dataIndex: 'sensitivityLevel',
      key: 'sensitivityLevel',
      width: 100,
      render: (level: string) => {
        const config = SENSITIVITY_LEVEL_OPTIONS.find((o) => o.value === level);
        return config ? <Tag color={config.color}>{config.label}</Tag> : <Tag>{level}</Tag>;
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
    },
    {
      title: '收藏时间',
      dataIndex: 'favoriteTime',
      key: 'favoriteTime',
      width: 180,
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
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
            onClick={() => history.push(`/assets/detail/${record.id}`)}
          >
            查看
          </Button>
          <Button
            type="text"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleRemoveFavorite(record.id)}
          >
            取消
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Card className={styles.headerCard}>
        <Space>
          <StarFilled style={{ color: '#faad14', fontSize: 24 }} />
          <div>
            <Title level={4} style={{ marginBottom: 4 }}>我的收藏</Title>
            <Text type="secondary">共 {total} 个收藏资产</Text>
          </div>
        </Space>
      </Card>

      <Card className={styles.tableCard}>
        {total === 0 ? (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="暂无收藏资产"
          >
            <Button type="primary" onClick={() => history.push('/assets/list')}>
              去浏览资产
            </Button>
          </Empty>
        ) : (
          <Table
            columns={columns}
            dataSource={dataSource}
            rowKey="id"
            loading={loading}
            pagination={{
              total,
              showTotal: (t) => `共 ${t} 条`,
            }}
          />
        )}
      </Card>
    </div>
  );
};

export default AssetFavorites;
