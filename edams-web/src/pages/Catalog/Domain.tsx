/**
 * 业务域页面
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Space,
  Typography,
  Tag,
  Progress,
  Button,
  Table,
  Modal,
  Form,
  Input,
  Select,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  DatabaseOutlined,
  FolderOutlined,
  UserOutlined,
  ArrowRightOutlined,
  EditOutlined,
} from '@ant-design/icons';
import { history } from '@umijs/max';
import styles from './index.less';

const { Title, Text } = Typography;

interface Domain {
  id: string;
  name: string;
  code: string;
  description: string;
  owner: string;
  assetCount: number;
  tableCount: number;
  apiCount: number;
  qualityScore: number;
}

const DomainCatalog: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [domains, setDomains] = useState<Domain[]>([]);
  const [totalAssets, setTotalAssets] = useState(0);
  const [modalVisible, setModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 模拟业务域数据
  const mockDomains: Domain[] = [
    {
      id: 'domain-1',
      name: '交易域',
      code: 'TRADE',
      description: '负责订单、支付、结算等交易相关业务',
      owner: '张三',
      assetCount: 128,
      tableCount: 89,
      apiCount: 39,
      qualityScore: 92,
    },
    {
      id: 'domain-2',
      name: '用户域',
      code: 'USER',
      description: '负责用户注册、登录、会员等用户相关业务',
      owner: '李四',
      assetCount: 56,
      tableCount: 34,
      apiCount: 22,
      qualityScore: 88,
    },
    {
      id: 'domain-3',
      name: '产品域',
      code: 'PRODUCT',
      description: '负责商品、库存、分类等产品相关业务',
      owner: '王五',
      assetCount: 89,
      tableCount: 67,
      apiCount: 22,
      qualityScore: 85,
    },
    {
      id: 'domain-4',
      name: '财务域',
      code: 'FINANCE',
      description: '负责账务、发票、结算等财务相关业务',
      owner: '赵六',
      assetCount: 45,
      tableCount: 32,
      apiCount: 13,
      qualityScore: 95,
    },
    {
      id: 'domain-5',
      name: '营销域',
      code: 'MARKETING',
      description: '负责活动、优惠券、推荐等营销相关业务',
      owner: '孙七',
      assetCount: 67,
      tableCount: 45,
      apiCount: 22,
      qualityScore: 78,
    },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setDomains(mockDomains);
      setTotalAssets(mockDomains.reduce((sum, d) => sum + d.assetCount, 0));
      setLoading(false);
    }, 500);
  }, []);

  // 新增业务域
  const handleAdd = () => {
    form.resetFields();
    setModalVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      message.success('新增业务域成功');
      setModalVisible(false);
      // 实际新增逻辑
    } catch (error) {
      // 表单验证失败
    }
  };

  // 查看业务域下的资产
  const handleViewAssets = (domain: Domain) => {
    history.push(`/assets/list?domain=${domain.id}`);
  };

  // 获取质量评分颜色
  const getQualityColor = (score: number) => {
    if (score >= 90) return '#52c41a';
    if (score >= 80) return '#1890ff';
    if (score >= 70) return '#faad14';
    return '#f5222d';
  };

  // 业务域详情表格列
  const detailColumns: ColumnsType<Domain> = [
    {
      title: '业务域',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <Tag color="blue">{record.code}</Tag>
          <a onClick={() => handleViewAssets(record)}>{name}</a>
        </Space>
      ),
    },
    {
      title: '资产数',
      dataIndex: 'assetCount',
      key: 'assetCount',
      width: 100,
      align: 'right',
      render: (count: number) => <Text strong>{count}</Text>,
    },
    {
      title: '数据表',
      dataIndex: 'tableCount',
      key: 'tableCount',
      width: 100,
      align: 'right',
      render: (count: number) => count,
    },
    {
      title: 'API',
      dataIndex: 'apiCount',
      key: 'apiCount',
      width: 80,
      align: 'right',
      render: (count: number) => count,
    },
    {
      title: '负责人',
      dataIndex: 'owner',
      key: 'owner',
      width: 100,
    },
    {
      title: '质量评分',
      dataIndex: 'qualityScore',
      key: 'qualityScore',
      width: 150,
      render: (score: number) => (
        <Progress
          percent={score}
          size="small"
          strokeColor={getQualityColor(score)}
          format={(p) => <span style={{ color: getQualityColor(score) }}>{p}</span>}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button type="link" onClick={() => handleViewAssets(record)}>
          查看资产 <ArrowRightOutlined />
        </Button>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      {/* 统计概览 */}
      <Row gutter={[16, 16]} className={styles.statsRow}>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="业务域总数"
              value={domains.length}
              prefix={<FolderOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="资产总数"
              value={totalAssets}
              prefix={<DatabaseOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="数据表总数"
              value={domains.reduce((sum, d) => sum + d.tableCount, 0)}
              prefix={<DatabaseOutlined style={{ color: '#722ed1' }} />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card className={styles.statCard}>
            <Statistic
              title="API 总数"
              value={domains.reduce((sum, d) => sum + d.apiCount, 0)}
              prefix={<DatabaseOutlined style={{ color: '#faad14' }} />}
            />
          </Card>
        </Col>
      </Row>

      {/* 业务域卡片 */}
      <Row gutter={[16, 16]} className={styles.domainRow}>
        {domains.map((domain) => (
          <Col xs={24} sm={12} lg={8} xl={6} key={domain.id}>
            <Card
              className={styles.domainCard}
              hoverable
              onClick={() => handleViewAssets(domain)}
            >
              <div className={styles.domainHeader}>
                <Tag color="blue">{domain.code}</Tag>
                <Text type="secondary"><UserOutlined /> {domain.owner}</Text>
              </div>
              <Title level={5} className={styles.domainName}>
                {domain.name}
              </Title>
              <Text type="secondary" className={styles.domainDesc}>
                {domain.description}
              </Text>
              <div className={styles.domainStats}>
                <div className={styles.statItem}>
                  <DatabaseOutlined />
                  <span>{domain.assetCount}</span>
                </div>
                <div className={styles.statItem}>
                  <FolderOutlined />
                  <span>{domain.tableCount}</span>
                </div>
                <Progress
                  type="circle"
                  percent={domain.qualityScore}
                  size={50}
                  strokeColor={getQualityColor(domain.qualityScore)}
                  format={(p) => <span style={{ fontSize: 10 }}>{p}</span>}
                />
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      {/* 业务域详情表格 */}
      <Card
        title="业务域详情"
        className={styles.tableCard}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增业务域
          </Button>
        }
      >
        <Table
          columns={detailColumns}
          dataSource={domains}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      {/* 新增业务域弹窗 */}
      <Modal
        title="新增业务域"
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="业务域名称"
            rules={[{ required: true, message: '请输入业务域名称' }]}
          >
            <Input placeholder="请输入业务域名称" />
          </Form.Item>
          <Form.Item
            name="code"
            label="业务域编码"
            rules={[{ required: true, message: '请输入业务域编码' }]}
          >
            <Input placeholder="请输入业务域编码，如：TRADE" />
          </Form.Item>
          <Form.Item
            name="owner"
            label="负责人"
            rules={[{ required: true, message: '请选择负责人' }]}
          >
            <Select placeholder="请选择负责人">
              <Select.Option value="user-1">张三</Select.Option>
              <Select.Option value="user-2">李四</Select.Option>
              <Select.Option value="user-3">王五</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="请输入业务域描述" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DomainCatalog;
