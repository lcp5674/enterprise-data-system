/**
 * 目录树页面
 */

import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Tree,
  Input,
  Space,
  Button,
  Typography,
  Spin,
  message,
  Dropdown,
  Modal,
} from 'antd';
import type { DataNode } from 'antd/es/tree';
import type { MenuProps } from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  FolderOutlined,
  FolderOpenOutlined,
  DatabaseOutlined,
  ApiOutlined,
  FileTextOutlined,
  MoreOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import * as assetService from '../../services/asset';
import styles from './index.less';

const { Title, Text } = Typography;
const { Search } = Input;

interface TreeNode {
  key: string;
  title: string;
  icon?: React.ReactNode;
  isLeaf?: boolean;
  children?: TreeNode[];
  assetType?: string;
  assetId?: string;
}

const CatalogTree: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [treeData, setTreeData] = useState<TreeNode[]>([]);
  const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
  const [selectedKey, setSelectedKey] = useState<string>('');
  const [searchValue, setSearchValue] = useState('');

  // 模拟目录数据
  const mockTreeData: TreeNode[] = [
    {
      key: 'domain-1',
      title: '交易域',
      icon: <FolderOutlined />,
      children: [
        {
          key: 'domain-1-module-1',
          title: '订单管理',
          icon: <FolderOutlined />,
          children: [
            { key: 'domain-1-module-1-table-1', title: 'fact_orders', icon: <DatabaseOutlined />, isLeaf: true, assetType: 'TABLE', assetId: 'asset-001' },
            { key: 'domain-1-module-1-table-2', title: 'dim_products', icon: <DatabaseOutlined />, isLeaf: true, assetType: 'TABLE', assetId: 'asset-002' },
          ],
        },
        {
          key: 'domain-1-module-2',
          title: '支付管理',
          icon: <FolderOutlined />,
          children: [
            { key: 'domain-1-module-2-table-1', title: 'fact_payments', icon: <DatabaseOutlined />, isLeaf: true, assetType: 'TABLE', assetId: 'asset-003' },
          ],
        },
      ],
    },
    {
      key: 'domain-2',
      title: '用户域',
      icon: <FolderOutlined />,
      children: [
        {
          key: 'domain-2-module-1',
          title: '用户中心',
          icon: <FolderOutlined />,
          children: [
            { key: 'domain-2-module-1-table-1', title: 'dim_users', icon: <DatabaseOutlined />, isLeaf: true, assetType: 'TABLE', assetId: 'asset-004' },
          ],
        },
      ],
    },
    {
      key: 'domain-3',
      title: '产品域',
      icon: <FolderOutlined />,
      children: [],
    },
    {
      key: 'api-group-1',
      title: 'API 资产',
      icon: <ApiOutlined />,
      children: [
        { key: 'api-1', title: '/api/users/list', icon: <ApiOutlined />, isLeaf: true, assetType: 'API', assetId: 'asset-api-001' },
        { key: 'api-2', title: '/api/orders/create', icon: <ApiOutlined />, isLeaf: true, assetType: 'API', assetId: 'asset-api-002' },
      ],
    },
  ];

  useEffect(() => {
    setLoading(true);
    // 模拟加载
    setTimeout(() => {
      setTreeData(mockTreeData);
      setExpandedKeys(['domain-1', 'domain-2']);
      setLoading(false);
    }, 500);
  }, []);

  // 获取资产类型图标
  const getAssetIcon = (type: string, expanded?: boolean) => {
    const icons: Record<string, React.ReactNode> = {
      TABLE: <DatabaseOutlined />,
      VIEW: <DatabaseOutlined />,
      FILE: <FileTextOutlined />,
      API: <ApiOutlined />,
      STREAM: <FolderOutlined />,
    };
    return icons[type] || <FolderOutlined />;
  };

  // 树节点右键菜单
  const getContextMenu = (node: TreeNode): MenuProps => ({
    items: [
      {
        key: 'view',
        icon: <EyeOutlined />,
        label: '查看详情',
        onClick: () => {
          if (node.assetId) {
            history.push(`/assets/detail/${node.assetId}`);
          }
        },
      },
      {
        key: 'viewLineage',
        icon: <EyeOutlined />,
        label: '查看血缘',
        onClick: () => {
          if (node.assetId) {
            history.push(`/lineage/graph?assetId=${node.assetId}`);
          }
        },
        disabled: !node.assetId,
      },
      { type: 'divider' },
      {
        key: 'edit',
        icon: <EditOutlined />,
        label: '编辑',
        onClick: () => message.info('编辑功能开发中'),
      },
      {
        key: 'delete',
        icon: <DeleteOutlined />,
        label: '删除',
        danger: true,
        onClick: () => handleDeleteNode(node),
      },
    ],
  });

  // 删除节点
  const handleDeleteNode = (node: TreeNode) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除 "${node.title}" 吗？`,
      onOk: () => {
        message.success('删除成功');
        // 实际删除逻辑
      },
    });
  };

  // 点击节点
  const handleSelect = (selectedKeys: string[]) => {
    if (selectedKeys.length > 0) {
      setSelectedKey(selectedKeys[0]);
      const node = findNode(selectedKeys[0], treeData);
      if (node?.assetId) {
        history.push(`/assets/detail/${node.assetId}`);
      }
    }
  };

  // 查找节点
  const findNode = (key: string, nodes: TreeNode[]): TreeNode | null => {
    for (const node of nodes) {
      if (node.key === key) return node;
      if (node.children) {
        const found = findNode(key, node.children);
        if (found) return found;
      }
    }
    return null;
  };

  // 搜索过滤
  const filterTree = (value: string) => {
    setSearchValue(value);
    if (!value) {
      setExpandedKeys([]);
      return;
    }
    // 简单过滤：展开包含关键词的父节点
    const expanded = mockTreeData
      .filter(node => node.title.includes(value) || node.children?.some(child => child.title.includes(value)))
      .map(node => node.key);
    setExpandedKeys(expanded);
  };

  return (
    <div className={styles.container}>
      <Card className={styles.searchCard}>
        <Space>
          <Search
            placeholder="搜索目录或资产..."
            prefix={<SearchOutlined />}
            value={searchValue}
            onChange={(e) => filterTree(e.target.value)}
            style={{ width: 300 }}
            allowClear
          />
          <Button icon={<PlusOutlined />}>新增目录</Button>
        </Space>
      </Card>

      <Card className={styles.treeCard}>
        {loading ? (
          <div className={styles.loading}>
            <Spin size="large" />
          </div>
        ) : (
          <Tree
            showIcon
            showLine={{ showLeafIcon: false }}
            expandedKeys={expandedKeys}
            selectedKeys={[selectedKey]}
            onExpand={(keys) => setExpandedKeys(keys)}
            onSelect={handleSelect}
            treeData={treeData}
            blockNode
            titleRender={(nodeData: any) => (
              <Dropdown menu={getContextMenu(nodeData)} trigger={['contextMenu']}>
                <Space>
                  {nodeData.isLeaf ? getAssetIcon(nodeData.assetType) : <FolderOutlined />}
                  <Text>{nodeData.title}</Text>
                </Space>
              </Dropdown>
            )}
          />
        )}
      </Card>
    </div>
  );
};

export default CatalogTree;
