/**
 * 资产注册/创建页面
 */

import React, { useState } from 'react';
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Steps,
  Space,
  Typography,
  Row,
  Col,
  InputNumber,
  Switch,
  Divider,
  message,
  Result,
} from 'antd';
import {
  ArrowLeftOutlined,
  ArrowRightOutlined,
  CheckOutlined,
  DatabaseOutlined,
  CloudServerOutlined,
  ApiOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { history, useNavigate } from '@umijs/max';
import * as assetService from '../../services/asset';
import { ASSET_TYPE_OPTIONS, SENSITIVITY_LEVEL_OPTIONS } from '../../types';
import type { DataAsset } from '../../types';
import styles from './index.less';

const { Title, Text, Paragraph } = Typography;
const { TextArea } = Input;

const { Option } = Select;

interface BasicInfoValues {
  name: string;
  assetType: string;
  sensitivityLevel: string;
  domainId: string;
  description: string;
}

interface SourceInfoValues {
  datasourceId: string;
  schema: string;
  tableName: string;
  database: string;
}

interface MetaInfoValues {
  owner: string;
  ownerDept: string;
  tags: string[];
  businessDesc: string;
}

const AssetCreate: React.FC = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [basicForm] = Form.useForm<BasicInfoValues>();
  const [sourceForm] = Form.useForm<SourceInfoValues>();
  const [metaForm] = Form.useForm<MetaInfoValues>();
  const [createdAssetId, setCreatedAssetId] = useState<string>('');

  const steps = [
    { title: '基本信息', icon: <DatabaseOutlined /> },
    { title: '数据源信息', icon: <CloudServerOutlined /> },
    { title: '元数据信息', icon: <FileTextOutlined /> },
    { title: '完成', icon: <CheckOutlined /> },
  ];

  // 获取资产类型对应的图标
  const getAssetTypeIcon = (type: string) => {
    const icons: Record<string, React.ReactNode> = {
      TABLE: <DatabaseOutlined />,
      VIEW: <DatabaseOutlined />,
      FILE: <FileTextOutlined />,
      API: <ApiOutlined />,
      STREAM: <CloudServerOutlined />,
    };
    return icons[type] || <DatabaseOutlined />;
  };

  // 下一步
  const handleNext = async () => {
    try {
      if (currentStep === 0) {
        await basicForm.validateFields();
      } else if (currentStep === 1) {
        await sourceForm.validateFields();
      } else if (currentStep === 2) {
        await metaForm.validateFields();
      }
      setCurrentStep(currentStep + 1);
    } catch (error) {
      // 表单验证失败
    }
  };

  // 上一步
  const handlePrev = () => {
    setCurrentStep(currentStep - 1);
  };

  // 提交表单
  const handleSubmit = async () => {
    setLoading(true);
    try {
      const basicInfo = basicForm.getFieldsValue();
      const sourceInfo = sourceForm.getFieldsValue();
      const metaInfo = metaForm.getFieldsValue();

      const assetData: Partial<DataAsset> = {
        name: basicInfo.name,
        assetType: basicInfo.assetType,
        sensitivityLevel: basicInfo.sensitivityLevel,
        domainId: basicInfo.domainId,
        description: basicInfo.description,
        datasourceId: sourceInfo.datasourceId,
        schema: sourceInfo.schema,
        tableName: sourceInfo.tableName,
        database: sourceInfo.database,
        owner: metaInfo.owner,
        ownerDept: metaInfo.ownerDept,
        tags: metaInfo.tags,
      };

      const result = await assetService.createAsset(assetData as any);
      setCreatedAssetId(result.id);
      setCurrentStep(3);
      message.success('资产注册成功');
    } catch (error: any) {
      message.error(error.message || '注册失败');
    } finally {
      setLoading(false);
    }
  };

  // 返回列表
  const handleBackToList = () => {
    navigate('/assets/list');
  };

  // 查看详情
  const handleViewDetail = () => {
    navigate(`/assets/detail/${createdAssetId}`);
  };

  // 继续添加
  const handleContinueAdd = () => {
    setCurrentStep(0);
    basicForm.resetFields();
    sourceForm.resetFields();
    metaForm.resetFields();
    setCreatedAssetId('');
  };

  // 渲染步骤内容
  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <Form
            form={basicForm}
            layout="vertical"
            size="large"
            className={styles.form}
          >
            <Form.Item
              name="name"
              label="资产名称"
              rules={[{ required: true, message: '请输入资产名称' }]}
            >
              <Input placeholder="请输入资产名称" />
            </Form.Item>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="assetType"
                  label="资产类型"
                  rules={[{ required: true, message: '请选择资产类型' }]}
                >
                  <Select placeholder="请选择资产类型">
                    {ASSET_TYPE_OPTIONS.map((option) => (
                      <Option key={option.value} value={option.value}>
                        <Space>
                          {getAssetTypeIcon(option.value)}
                          {option.label}
                        </Space>
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="sensitivityLevel"
                  label="敏感级别"
                  rules={[{ required: true, message: '请选择敏感级别' }]}
                >
                  <Select placeholder="请选择敏感级别">
                    {SENSITIVITY_LEVEL_OPTIONS.map((option) => (
                      <Option key={option.value} value={option.value}>
                        <Tag color={option.color}>{option.label}</Tag>
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              name="domainId"
              label="所属域"
              rules={[{ required: true, message: '请选择所属域' }]}
            >
              <Select placeholder="请选择所属域">
                <Option value="domain-1">交易域</Option>
                <Option value="domain-2">用户域</Option>
                <Option value="domain-3">产品域</Option>
                <Option value="domain-4">财务域</Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="description"
              label="描述"
            >
              <TextArea
                rows={4}
                placeholder="请输入资产描述"
                maxLength={500}
                showCount
              />
            </Form.Item>
          </Form>
        );

      case 1:
        return (
          <Form
            form={sourceForm}
            layout="vertical"
            size="large"
            className={styles.form}
          >
            <Paragraph type="secondary">
              请提供该资产对应的数据源信息，用于系统进行数据连接和血缘分析。
            </Paragraph>

            <Form.Item
              name="datasourceId"
              label="数据源"
              rules={[{ required: true, message: '请选择数据源' }]}
            >
              <Select placeholder="请选择数据源">
                <Option value="ds-1">MySQL 生产库</Option>
                <Option value="ds-2">PostgreSQL 数据仓库</Option>
                <Option value="ds-3">Hive 数据湖</Option>
                <Option value="ds-4">Kafka 消息队列</Option>
              </Select>
            </Form.Item>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="database"
                  label="数据库/实例"
                  rules={[{ required: true, message: '请输入数据库名称' }]}
                >
                  <Input placeholder="例如: edams_prod" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="schema"
                  label="Schema/模式"
                >
                  <Input placeholder="例如: public" />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              name="tableName"
              label="表名/API路径"
              rules={[{ required: true, message: '请输入表名或API路径' }]}
            >
              <Input placeholder="例如: dim_user_info" />
            </Form.Item>
          </Form>
        );

      case 2:
        return (
          <Form
            form={metaForm}
            layout="vertical"
            size="large"
            className={styles.form}
          >
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="owner"
                  label="负责人"
                  rules={[{ required: true, message: '请输入负责人' }]}
                >
                  <Input placeholder="请输入负责人姓名" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="ownerDept"
                  label="负责部门"
                >
                  <Input placeholder="请输入负责部门" />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item
              name="tags"
              label="标签"
            >
              <Select mode="tags" placeholder="输入标签后按回车添加">
                <Option value="核心数据">核心数据</Option>
                <Option value="客户数据">客户数据</Option>
                <Option value="交易数据">交易数据</Option>
                <Option value="敏感数据">敏感数据</Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="businessDesc"
              label="业务描述"
            >
              <TextArea
                rows={4}
                placeholder="请输入业务描述，帮助其他用户理解该资产的用途"
              />
            </Form.Item>
          </Form>
        );

      case 3:
        return (
          <Result
            status="success"
            title="资产注册成功！"
            subTitle={`资产名称: ${basicForm.getFieldValue('name')}`}
            extra={[
              <Button type="primary" key="detail" onClick={handleViewDetail}>
                查看详情
              </Button>,
              <Button key="continue" onClick={handleContinueAdd}>
                继续添加
              </Button>,
              <Button key="list" onClick={handleBackToList}>
                返回列表
              </Button>,
            ]}
          />
        );

      default:
        return null;
    }
  };

  return (
    <div className={styles.container}>
      {/* 顶部导航 */}
      <div className={styles.header}>
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/assets/list')}
        >
          返回
        </Button>
        <Title level={4}>注册数据资产</Title>
      </div>

      {/* 步骤条 */}
      {currentStep < 3 && (
        <Card className={styles.stepsCard}>
          <Steps current={currentStep} items={steps} size="small" />
        </Card>
      )}

      {/* 表单内容 */}
      <Card className={styles.formCard}>
        {renderStepContent()}

        {/* 底部按钮 */}
        {currentStep < 3 && (
          <div className={styles.footer}>
            <Space>
              {currentStep > 0 && (
                <Button onClick={handlePrev}>
                  <ArrowLeftOutlined /> 上一步
                </Button>
              )}
              <Button onClick={handleBackToList}>取消</Button>
            </Space>
            <Space>
              {currentStep < 2 ? (
                <Button type="primary" onClick={handleNext}>
                  下一步 <ArrowRightOutlined />
                </Button>
              ) : (
                <Button type="primary" onClick={handleSubmit} loading={loading}>
                  提交
                </Button>
              )}
            </Space>
          </div>
        )}
      </Card>
    </div>
  );
};

// 修复 JSX 类型
const Tag: React.FC<any> = ({ color, children }) => (
  <span style={{ color }}>{children}</span>
);

export default AssetCreate;
