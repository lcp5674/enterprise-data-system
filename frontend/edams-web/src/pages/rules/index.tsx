/**
 * 规则引擎管理页面
 * 包含规则列表、规则编辑、规则测试、评估统计等功能
 */

import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  Tabs,
  message,
  Popconfirm,
  Descriptions,
  Statistic,
  Row,
  Col,
  Progress,
  Badge,
  Tooltip,
  Divider,
  Switch,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  PlayCircleOutlined,
  ThunderboltOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  CloseCircleOutlined,
  ExperimentOutlined,
  FileTextOutlined,
  BarChartOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import styles from './index.less';

// 类型定义
interface RuleRecord {
  id: number;
  ruleName: string;
  ruleCode: string;
  category: string;
  description?: string;
  ruleContent?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DRAFT';
  priority: number;
  version: string;
  createdBy?: string;
  createdTime?: string;
}

interface StatisticsData {
  totalRules: number;
  activeRules: number;
  inactiveRules: number;
  draftRules: number;
  byCategory: Record<string, number>;
}

const { TextArea } = Input;
const { TabPane } = Tabs;

const RuleEngineManagement: React.FC = () => {
  // 规则列表状态
  const [loading, setLoading] = useState(false);
  const [rules, setRules] = useState<RuleRecord[]>([]);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });
  const [filterCategory, setFilterCategory] = useState<string | undefined>(undefined);
  const [filterStatus, setFilterStatus] = useState<string | undefined>(undefined);

  // 弹窗状态
  const [ruleModalVisible, setRuleModalVisible] = useState(false);
  const [testModalVisible, setTestModalVisible] = useState(false);
  const [editingRule, setEditingRule] = useState<RuleRecord | null>(null);
  const [form] = Form.useForm();
  const [testForm] = Form.useForm();

  // 统计数据
  const [statistics, setStatistics] = useState<StatisticsData | null>(null);

  // 测试结果
  const [testResult, setTestResult] = useState<any>(null);
  const [testLoading, setTestLoading] = useState(false);

  // Mock数据
  const mockRules: RuleRecord[] = [
    {
      id: 1,
      ruleName: '质量评分 - 优秀',
      ruleCode: 'QUALITY-EXCELLENT',
      category: 'QUALITY',
      description: '无质量问题且各维度>0.95时评为优秀',
      status: 'ACTIVE',
      priority: 100,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-01',
    },
    {
      id: 2,
      ruleName: '质量评分 - 良好',
      ruleCode: 'QUALITY-GOOD',
      category: 'QUALITY',
      description: '质量问题<=2且完整度>0.8时评为良好',
      status: 'ACTIVE',
      priority: 90,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-01',
    },
    {
      id: 3,
      ruleName: '质量评分 - 待改进',
      ruleCode: 'QUALITY-NEEDS-IMPROVEMENT',
      category: 'QUALITY',
      description: '质量问题<=10且质量维度>0.6时评为待改进',
      status: 'ACTIVE',
      priority: 70,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-01',
    },
    {
      id: 4,
      ruleName: '合规检查 - 命名规范',
      ruleCode: 'COMPLIANCE-NAMING',
      category: 'COMPLIANCE',
      description: '检查数据资产命名是否符合标准规范',
      status: 'ACTIVE',
      priority: 100,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-02',
    },
    {
      id: 5,
      ruleName: '合规检查 - 数据类型',
      ruleCode: 'COMPLIANCE-DATA-TYPE',
      category: 'COMPLIANCE',
      description: '检查数据类型是否已定义',
      status: 'ACTIVE',
      priority: 80,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-02',
    },
    {
      id: 6,
      ruleName: '价值评估 - 高价值',
      ruleCode: 'VALUE-HIGH',
      category: 'VALUE',
      description: '高频访问且质量优良时评为高价值',
      status: 'ACTIVE',
      priority: 90,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-03',
    },
    {
      id: 7,
      ruleName: '价值评估 - 归档候选',
      ruleCode: 'VALUE-NO-VALUE',
      category: 'VALUE',
      description: '几乎无使用的数据资产标记为归档候选',
      status: 'ACTIVE',
      priority: 60,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-03',
    },
    {
      id: 8,
      ruleName: '生命周期 - 冷数据归档',
      ruleCode: 'LIFECYCLE-COLD',
      category: 'LIFECYCLE',
      description: '90天以上未活跃访问的数据建议归档',
      status: 'ACTIVE',
      priority: 90,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-04',
    },
    {
      id: 9,
      ruleName: '生命周期 - 退役',
      ruleCode: 'LIFECYCLE-RETIRED',
      category: 'LIFECYCLE',
      description: '一年以上完全未使用的数据建议退役',
      status: 'ACTIVE',
      priority: 70,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-04',
    },
    {
      id: 10,
      ruleName: '治理 - 严重质量问题',
      ruleCode: 'GOVERNANCE-CRITICAL',
      category: 'GOVERNANCE',
      description: '完整度低于50%或质量issues>20，需立即治理',
      status: 'ACTIVE',
      priority: 100,
      version: 'v1.0',
      createdBy: '系统',
      createdTime: '2026-04-05',
    },
    {
      id: 11,
      ruleName: '治理 - 孤儿资产',
      ruleCode: 'GOVERNANCE-ORPHANED',
      category: 'GOVERNANCE',
      description: '无负责人且未归属业务域的孤儿资产',
      status: 'ACTIVE',
      priority: 85,
      version: 'v1.1',
      createdBy: '系统',
      createdTime: '2026-04-05',
    },
    {
      id: 12,
      ruleName: '质量评分 - 差',
      ruleCode: 'QUALITY-POOR',
      category: 'QUALITY',
      description: '质量issues>10或完整度<0.6时评为差',
      status: 'DRAFT',
      priority: 60,
      version: 'v1.0',
      createdBy: '管理员',
      createdTime: '2026-04-10',
    },
  ];

  const mockStatistics: StatisticsData = {
    totalRules: 12,
    activeRules: 11,
    inactiveRules: 0,
    draftRules: 1,
    byCategory: {
      QUALITY: 4,
      COMPLIANCE: 2,
      VALUE: 2,
      LIFECYCLE: 2,
      GOVERNANCE: 2,
    },
  };

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      let filtered = [...mockRules];
      if (filterCategory) filtered = filtered.filter((r) => r.category === filterCategory);
      if (filterStatus) filtered = filtered.filter((r) => r.status === filterStatus);
      setRules(filtered);
      setPagination({ ...pagination, total: filtered.length });
      setLoading(false);
    }, 300);
  }, [filterCategory, filterStatus, pagination.current]);

  useEffect(() => {
    setStatistics(mockStatistics);
  }, []);

  // 获取分类标签配置
  const getCategoryConfig = (category: string) => {
    const config: Record<string, { color: string; label: string }> = {
      QUALITY: { color: 'green', label: '质量评分' },
      COMPLIANCE: { color: 'blue', label: '合规检查' },
      VALUE: { color: 'orange', label: '价值评估' },
      LIFECYCLE: { color: 'purple', label: '生命周期' },
      GOVERNANCE: { color: 'red', label: '治理规则' },
    };
    return config[category] || { color: 'default', label: category };
  };

  // 获取状态标签配置
  const getStatusConfig = (status: string) => {
    const config: Record<string, { color: string; label: string; icon: React.ReactNode }> = {
      ACTIVE: { color: 'green', label: '启用', icon: <CheckCircleOutlined /> },
      INACTIVE: { color: 'gray', label: '禁用', icon: <CloseCircleOutlined /> },
      DRAFT: { color: 'blue', label: '草稿', icon: <WarningOutlined /> },
    };
    return config[status] || { color: 'default', label: status, icon: null };
  };

  // 新增规则
  const handleAdd = () => {
    setEditingRule(null);
    form.resetFields();
    setRuleModalVisible(true);
  };

  // 编辑规则
  const handleEdit = (record: RuleRecord) => {
    setEditingRule(record);
    form.setFieldsValue(record);
    setRuleModalVisible(true);
  };

  // 删除规则
  const handleDelete = (id: number) => {
    message.success('规则已删除');
    setRules(rules.filter((item) => item.id !== id));
  };

  // 提交规则表单
  const handleRuleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingRule) {
        setRules(
          rules.map((item) =>
            item.id === editingRule.id ? { ...item, ...values } : item
          )
        );
        message.success('规则更新成功');
      } else {
        const newRule: RuleRecord = {
          id: Math.max(...rules.map((r) => r.id)) + 1,
          ...values,
          version: 'v1.0',
          createdBy: '当前用户',
          createdTime: new Date().toISOString().split('T')[0],
        };
        setRules([newRule, ...rules]);
        message.success('规则创建成功');
      }
      setRuleModalVisible(false);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 切换规则状态
  const handleToggleStatus = (record: RuleRecord) => {
    const newStatus = record.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    setRules(
      rules.map((item) =>
        item.id === record.id ? { ...item, status: newStatus } : item
      )
    );
    message.success(`规则已${newStatus === 'ACTIVE' ? '启用' : '禁用'}`);
  };

  // 重载规则
  const handleReloadRules = () => {
    message.loading({ content: '正在重载规则...', key: 'reload', duration: 2 });
    setTimeout(() => {
      message.success({ content: '规则重载成功', key: 'reload' });
    }, 2000);
  };

  // 规则测试
  const handleTestRule = async () => {
    try {
      const values = await testForm.validateFields();
      setTestLoading(true);
      setTestResult(null);

      // Mock测试结果
      setTimeout(() => {
        const mockTestResult = {
          quality: {
            qualityScore: values.completeness > 0.95 ? 98 : values.completeness > 0.8 ? 75 : 55,
            qualityLevel: values.completeness > 0.95 ? 'EXCELLENT' : values.completeness > 0.8 ? 'GOOD' : 'NEEDS_IMPROVEMENT',
            qualityIssues: values.qualityIssues || 0,
            triggeredRules: ['Quality-' + (values.completeness > 0.95 ? 'Excellent' : values.completeness > 0.8 ? 'Good' : 'NeedsImprovement')],
            evaluationSummary: values.completeness > 0.95 ? '数据质量优秀' : values.completeness > 0.8 ? '数据质量合格' : '数据质量待改进',
          },
          compliance: {
            complianceStatus: values.assetName && values.assetName.length >= 2 ? 'COMPLIANT' : 'NON_COMPLIANT',
            triggeredRules: ['Compliance-NamingConvention'],
            evaluationSummary: values.assetName && values.assetName.length >= 2 ? '数据完全符合标准' : '命名不规范',
          },
          value: {
            valueScore: (values.dailyAccessCount || 0) > 50 ? 92 : (values.dailyAccessCount || 0) > 10 ? 65 : 35,
            valueLevel: (values.dailyAccessCount || 0) > 50 ? 'HIGH_VALUE' : (values.dailyAccessCount || 0) > 10 ? 'MEDIUM_VALUE' : 'LOW_VALUE',
          },
          triggeredRules: ['Quality-Good', 'Compliance-NamingConvention', 'Value-MediumValue'],
        };
        setTestResult(mockTestResult);
        setTestLoading(false);
        message.success('规则测试完成');
      }, 1500);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 表格列配置
  const columns: ColumnsType<RuleRecord> = [
    {
      title: '规则名称',
      dataIndex: 'ruleName',
      key: 'ruleName',
      render: (name: string) => (
        <Space>
          <ThunderboltOutlined style={{ color: '#1890ff' }} />
          {name}
        </Space>
      ),
    },
    {
      title: '规则编码',
      dataIndex: 'ruleCode',
      key: 'ruleCode',
      width: 200,
      render: (code: string) => <Tag>{code}</Tag>,
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      width: 110,
      render: (category: string) => {
        const config = getCategoryConfig(category);
        return <Tag color={config.color}>{config.label}</Tag>;
      },
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      width: 80,
      sorter: (a, b) => a.priority - b.priority,
      render: (priority: number) => (
        <Badge
          count={priority}
          style={{
            backgroundColor: priority >= 90 ? '#52c41a' : priority >= 70 ? '#faad14' : '#d9d9d9',
          }}
        />
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = getStatusConfig(status);
        return <Tag color={config.color} icon={config.icon}>{config.label}</Tag>;
      },
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      width: 80,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="切换状态">
            <Switch
              size="small"
              checked={record.status === 'ACTIVE'}
              onChange={() => handleToggleStatus(record)}
            />
          </Tooltip>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除该规则？"
            onConfirm={() => handleDelete(record.id)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="text" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className={styles.container}>
      <Tabs defaultActiveKey="rules" type="card">
        {/* 规则列表 Tab */}
        <TabPane
          tab={
            <span>
              <FileTextOutlined />
              规则列表
            </span>
          }
          key="rules"
        >
          <Card className={styles.mainCard}>
            {/* 工具栏 */}
            <div className={styles.toolbar}>
              <Space>
                <Select
                  placeholder="规则分类"
                  allowClear
                  style={{ width: 140 }}
                  onChange={(value) => setFilterCategory(value)}
                  options={[
                    { label: '质量评分', value: 'QUALITY' },
                    { label: '合规检查', value: 'COMPLIANCE' },
                    { label: '价值评估', value: 'VALUE' },
                    { label: '生命周期', value: 'LIFECYCLE' },
                    { label: '治理规则', value: 'GOVERNANCE' },
                  ]}
                />
                <Select
                  placeholder="规则状态"
                  allowClear
                  style={{ width: 120 }}
                  onChange={(value) => setFilterStatus(value)}
                  options={[
                    { label: '启用', value: 'ACTIVE' },
                    { label: '禁用', value: 'INACTIVE' },
                    { label: '草稿', value: 'DRAFT' },
                  ]}
                />
              </Space>
              <Space>
                <Button
                  icon={<ReloadOutlined />}
                  onClick={handleReloadRules}
                >
                  重载规则
                </Button>
                <Button
                  type="primary"
                  icon={<PlusOutlined />}
                  onClick={handleAdd}
                >
                  新增规则
                </Button>
              </Space>
            </div>

            {/* 规则表格 */}
            <Table
              columns={columns}
              dataSource={rules}
              rowKey="id"
              loading={loading}
              pagination={{
                ...pagination,
                showTotal: (total) => `共 ${total} 条规则`,
                showSizeChanger: true,
                showQuickJumper: true,
              }}
              onChange={(pag) => setPagination({ ...pagination, current: pag.current || 1, pageSize: pag.pageSize || 10 })}
            />
          </Card>
        </TabPane>

        {/* 规则测试 Tab */}
        <TabPane
          tab={
            <span>
              <ExperimentOutlined />
              规则测试
            </span>
          }
          key="test"
        >
          <Row gutter={24}>
            {/* 测试输入 */}
            <Col span={10}>
              <Card title="测试输入" className={styles.testCard}>
                <Form
                  form={testForm}
                  layout="vertical"
                  initialValues={{
                    qualityIssues: 0,
                    completeness: 0.85,
                    accuracy: 0.90,
                    consistency: 0.88,
                    timeliness: 0.80,
                    uniqueness: 0.92,
                    dailyAccessCount: 25,
                    dataSizeMb: 500,
                  }}
                >
                  <Form.Item name="assetId" label="资产ID" rules={[{ required: true, message: '请输入资产ID' }]}>
                    <Input placeholder="如: ASSET-001" />
                  </Form.Item>
                  <Form.Item name="assetName" label="资产名称">
                    <Input placeholder="如: 客户信息表" />
                  </Form.Item>
                  <Form.Item name="assetType" label="资产类型">
                    <Select placeholder="请选择" options={[
                      { label: '数据库表', value: 'TABLE' },
                      { label: 'API接口', value: 'API' },
                      { label: '文件', value: 'FILE' },
                      { label: '消息队列', value: 'QUEUE' },
                    ]} />
                  </Form.Item>
                  <Form.Item name="category" label="测试规则分类" rules={[{ required: true }]}>
                    <Select placeholder="请选择要测试的规则分类" options={[
                      { label: '质量评分 (QUALITY)', value: 'QUALITY' },
                      { label: '合规检查 (COMPLIANCE)', value: 'COMPLIANCE' },
                      { label: '价值评估 (VALUE)', value: 'VALUE' },
                      { label: '生命周期 (LIFECYCLE)', value: 'LIFECYCLE' },
                      { label: '治理规则 (GOVERNANCE)', value: 'GOVERNANCE' },
                      { label: '综合评估 (ALL)', value: 'ALL' },
                    ]} />
                  </Form.Item>

                  <Divider orientation="left">数据质量维度</Divider>
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="qualityIssues" label="质量问题数">
                        <Input type="number" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="completeness" label="完整度">
                        <Input type="number" step="0.01" />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="accuracy" label="准确度">
                        <Input type="number" step="0.01" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="consistency" label="一致性">
                        <Input type="number" step="0.01" />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Row gutter={12}>
                    <Col span={12}>
                      <Form.Item name="timeliness" label="及时性">
                        <Input type="number" step="0.01" />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="uniqueness" label="唯一性">
                        <Input type="number" step="0.01" />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Divider orientation="left">使用与价值</Divider>
                  <Form.Item name="dailyAccessCount" label="日均访问量">
                    <Input type="number" />
                  </Form.Item>
                  <Form.Item name="dataSizeMb" label="数据量(MB)">
                    <Input type="number" />
                  </Form.Item>

                  <Button
                    type="primary"
                    icon={<PlayCircleOutlined />}
                    onClick={handleTestRule}
                    loading={testLoading}
                    block
                  >
                    执行规则测试
                  </Button>
                </Form>
              </Card>
            </Col>

            {/* 测试结果 */}
            <Col span={14}>
              <Card title="测试结果" className={styles.testCard}>
                {testResult ? (
                  <div className={styles.testResult}>
                    {/* 质量评分结果 */}
                    {testResult.quality && (
                      <div className={styles.resultSection}>
                        <h4><CheckCircleOutlined style={{ color: '#52c41a' }} /> 质量评分</h4>
                        <Row gutter={16}>
                          <Col span={8}>
                            <Statistic
                              title="质量得分"
                              value={testResult.quality.qualityScore}
                              suffix="/ 100"
                              valueStyle={{
                                color: testResult.quality.qualityScore >= 80 ? '#52c41a' :
                                  testResult.quality.qualityScore >= 60 ? '#faad14' : '#ff4d4f',
                              }}
                            />
                          </Col>
                          <Col span={8}>
                            <div style={{ textAlign: 'center' }}>
                              <div style={{ marginBottom: 8, color: '#888' }}>质量等级</div>
                              <Tag color={
                                testResult.quality.qualityLevel === 'EXCELLENT' ? 'green' :
                                testResult.quality.qualityLevel === 'GOOD' ? 'blue' :
                                testResult.quality.qualityLevel === 'ACCEPTABLE' ? 'orange' : 'red'
                              }>
                                {testResult.quality.qualityLevel}
                              </Tag>
                            </div>
                          </Col>
                          <Col span={8}>
                            <div style={{ textAlign: 'center' }}>
                              <div style={{ marginBottom: 8, color: '#888' }}>质量问题</div>
                              <Tag color={testResult.quality.qualityIssues > 5 ? 'red' : 'green'}>
                                {testResult.quality.qualityIssues} 个
                              </Tag>
                            </div>
                          </Col>
                        </Row>
                        <div style={{ marginTop: 12 }}>
                          <Tag color="blue">{testResult.quality.evaluationSummary}</Tag>
                        </div>
                      </div>
                    )}

                    {/* 合规检查结果 */}
                    {testResult.compliance && (
                      <div className={styles.resultSection}>
                        <h4><FileTextOutlined style={{ color: '#1890ff' }} /> 合规检查</h4>
                        <Tag color={
                          testResult.compliance.complianceStatus === 'COMPLIANT' ? 'green' :
                          testResult.compliance.complianceStatus === 'PARTIAL_COMPLIANT' ? 'orange' : 'red'
                        }>
                          {testResult.compliance.complianceStatus}
                        </Tag>
                        <div style={{ marginTop: 8 }}>
                          <Tag color="blue">{testResult.compliance.evaluationSummary}</Tag>
                        </div>
                      </div>
                    )}

                    {/* 价值评估结果 */}
                    {testResult.value && (
                      <div className={styles.resultSection}>
                        <h4 style={{ color: '#fa8c16' }}>💎 价值评估</h4>
                        <Row gutter={16}>
                          <Col span={12}>
                            <Statistic
                              title="价值得分"
                              value={testResult.value.valueScore}
                              suffix="/ 100"
                              valueStyle={{
                                color: testResult.value.valueScore >= 80 ? '#52c41a' :
                                  testResult.value.valueScore >= 60 ? '#faad14' : '#ff4d4f',
                              }}
                            />
                          </Col>
                          <Col span={12}>
                            <div style={{ textAlign: 'center' }}>
                              <div style={{ marginBottom: 8, color: '#888' }}>价值等级</div>
                              <Tag color={
                                testResult.value.valueLevel === 'HIGH_VALUE' ? 'gold' :
                                testResult.value.valueLevel === 'MEDIUM_VALUE' ? 'blue' :
                                testResult.value.valueLevel === 'LOW_VALUE' ? 'orange' : 'default'
                              }>
                                {testResult.value.valueLevel}
                              </Tag>
                            </div>
                          </Col>
                        </Row>
                      </div>
                    )}

                    {/* 触发的规则 */}
                    <div className={styles.resultSection}>
                      <h4>触发的规则</h4>
                      <div>
                        {(testResult.triggeredRules || []).map((rule: string, idx: number) => (
                          <Tag key={idx} color="processing" style={{ marginBottom: 4 }}>
                            <ThunderboltOutlined /> {rule}
                          </Tag>
                        ))}
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className={styles.emptyResult}>
                    <ExperimentOutlined style={{ fontSize: 48, color: '#d9d9d9' }} />
                    <p style={{ color: '#999', marginTop: 16 }}>请在左侧填写测试数据并执行测试</p>
                    <p style={{ color: '#bbb', fontSize: 12 }}>
                      支持对质量评分、合规检查、价值评估、生命周期、治理规则进行独立测试
                    </p>
                  </div>
                )}
              </Card>
            </Col>
          </Row>
        </TabPane>

        {/* 统计概览 Tab */}
        <TabPane
          tab={
            <span>
              <BarChartOutlined />
              统计概览
            </span>
          }
          key="statistics"
        >
          {statistics && (
            <div>
              <Card className={styles.mainCard}>
                <Row gutter={[24, 24]}>
                  <Col span={6}>
                    <Card className={styles.statCard}>
                      <Statistic
                        title="总规则数"
                        value={statistics.totalRules}
                        prefix={<FileTextOutlined />}
                      />
                    </Card>
                  </Col>
                  <Col span={6}>
                    <Card className={styles.statCard}>
                      <Statistic
                        title="已启用"
                        value={statistics.activeRules}
                        prefix={<CheckCircleOutlined />}
                        valueStyle={{ color: '#52c41a' }}
                      />
                    </Card>
                  </Col>
                  <Col span={6}>
                    <Card className={styles.statCard}>
                      <Statistic
                        title="已禁用"
                        value={statistics.inactiveRules}
                        prefix={<CloseCircleOutlined />}
                        valueStyle={{ color: '#999' }}
                      />
                    </Card>
                  </Col>
                  <Col span={6}>
                    <Card className={styles.statCard}>
                      <Statistic
                        title="草稿"
                        value={statistics.draftRules}
                        prefix={<WarningOutlined />}
                        valueStyle={{ color: '#1890ff' }}
                      />
                    </Card>
                  </Col>
                </Row>

                <Divider />

                <h3 style={{ marginBottom: 16 }}>按分类统计</h3>
                <Row gutter={16}>
                  {Object.entries(statistics.byCategory).map(([key, value]) => {
                    const config = getCategoryConfig(key);
                    return (
                      <Col span={4} key={key}>
                        <div className={styles.categoryStat}>
                          <Progress
                            type="circle"
                            percent={Math.round((value / statistics.totalRules) * 100)}
                            size={80}
                            format={() => `${value}`}
                            strokeColor={config.color}
                          />
                          <div style={{ marginTop: 8, textAlign: 'center' }}>
                            <Tag color={config.color}>{config.label}</Tag>
                          </div>
                        </div>
                      </Col>
                    );
                  })}
                </Row>
              </Card>

              {/* 规则引擎架构说明 */}
              <Card title={<span><SettingOutlined /> 规则引擎架构</span>} style={{ marginTop: 16 }}>
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="引擎类型">Drools 8.44.0</Descriptions.Item>
                  <Descriptions.Item label="规则格式">DRL (Drools Rule Language)</Descriptions.Item>
                  <Descriptions.Item label="会话模式">Stateless KieSession</Descriptions.Item>
                  <Descriptions.Item label="热重载">支持运行时动态重载</Descriptions.Item>
                  <Descriptions.Item label="规则分类">5类 (质量/合规/价值/生命周期/治理)</Descriptions.Item>
                  <Descriptions.Item label="Fact对象">AssetEvaluation (统一评估模型)</Descriptions.Item>
                  <Descriptions.Item label="服务端口">8018</Descriptions.Item>
                  <Descriptions.Item label="数据库">PostgreSQL (edams_rules)</Descriptions.Item>
                </Descriptions>
              </Card>
            </div>
          )}
        </TabPane>
      </Tabs>

      {/* 新增/编辑规则弹窗 */}
      <Modal
        title={editingRule ? '编辑规则' : '新增规则'}
        open={ruleModalVisible}
        onOk={handleRuleSubmit}
        onCancel={() => setRuleModalVisible(false)}
        okText="确认"
        cancelText="取消"
        width={720}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="ruleName"
                label="规则名称"
                rules={[{ required: true, message: '请输入规则名称' }]}
              >
                <Input placeholder="如: 质量评分 - 优秀" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="ruleCode"
                label="规则编码"
                rules={[{ required: true, message: '请输入规则编码' }]}
              >
                <Input placeholder="如: QUALITY-EXCELLENT" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="category"
                label="规则分类"
                rules={[{ required: true, message: '请选择分类' }]}
              >
                <Select placeholder="请选择分类" options={[
                  { label: '质量评分 (QUALITY)', value: 'QUALITY' },
                  { label: '合规检查 (COMPLIANCE)', value: 'COMPLIANCE' },
                  { label: '价值评估 (VALUE)', value: 'VALUE' },
                  { label: '生命周期 (LIFECYCLE)', value: 'LIFECYCLE' },
                  { label: '治理规则 (GOVERNANCE)', value: 'GOVERNANCE' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="priority" label="优先级">
                <Input type="number" placeholder="0-100" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="status" label="状态">
                <Select options={[
                  { label: '启用', value: 'ACTIVE' },
                  { label: '草稿', value: 'DRAFT' },
                  { label: '禁用', value: 'INACTIVE' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="description" label="规则描述">
            <TextArea rows={2} placeholder="请描述规则的用途和触发条件" />
          </Form.Item>
          <Form.Item name="ruleContent" label="规则内容 (DRL)">
            <TextArea
              rows={8}
              placeholder={`rule "Rule Name"\n    when\n        $asset : AssetEvaluation(condition)\n    then\n        // action\n        update($asset);\nend`}
              style={{ fontFamily: 'monospace', fontSize: 13 }}
            />
          </Form.Item>
          <Form.Item name="triggerCondition" label="触发条件">
            <Input placeholder="描述规则的触发条件" />
          </Form.Item>
          <Form.Item name="parameters" label="参数配置 (JSON)">
            <TextArea rows={3} placeholder='{"threshold": 0.8, "maxIssues": 10}' />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RuleEngineManagement;
