/**
 * 通知设置页面
 */

import React, { useState } from 'react';
import {
  Card,
  Form,
  Switch,
  Button,
  Space,
  Typography,
  Row,
  Col,
  Divider,
  message,
  Tag,
} from 'antd';
import {
  SaveOutlined,
  MailOutlined,
  BellOutlined,
  MessageOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import styles from './index.less';

const { Title, Text } = Typography;

interface NotificationSettings {
  email: {
    enabled: boolean;
    qualityAlert: boolean;
    assetChange: boolean;
    taskComplete: boolean;
    systemNotice: boolean;
  };
  sms: {
    enabled: boolean;
    qualityAlert: boolean;
    urgentNotice: boolean;
  };
  inApp: {
    enabled: boolean;
    all: boolean;
  };
}

const SystemNotifications: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  // 初始化表单值
  const initialValues: NotificationSettings = {
    email: {
      enabled: true,
      qualityAlert: true,
      assetChange: false,
      taskComplete: true,
      systemNotice: true,
    },
    sms: {
      enabled: false,
      qualityAlert: true,
      urgentNotice: true,
    },
    inApp: {
      enabled: true,
      all: true,
    },
  };

  // 保存设置
  const handleSave = async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      console.log('保存通知设置:', values);
      message.success('设置已保存');
    } catch (error) {
      message.error('保存失败');
    } finally {
      setLoading(false);
    }
  };

  // 重置设置
  const handleReset = () => {
    form.setFieldsValue(initialValues);
    message.info('已重置为默认设置');
  };

  return (
    <div className={styles.container}>
      <Card
        title="通知设置"
        extra={
          <Space>
            <Button onClick={handleReset}>重置</Button>
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSave} loading={loading}>
              保存
            </Button>
          </Space>
        }
        className={styles.card}
      >
        <Form form={form} initialValues={initialValues} layout="vertical">
          {/* 邮件通知 */}
          <Card
            type="inner"
            title={
              <Space>
                <MailOutlined style={{ color: '#1890ff' }} />
                <span>邮件通知</span>
                <Switch
                  size="small"
                  checked={form.getFieldValue(['email', 'enabled'])}
                  onChange={(checked) => form.setFieldValue(['email', 'enabled'], checked)}
                />
              </Space>
            }
            className={styles.innerCard}
          >
            <Form.Item name={['email', 'enabled']} hidden>
              <Switch />
            </Form.Item>
            
            <Form.Item
              name={['email', 'qualityAlert']}
              valuePropName="checked"
              label={
                <Space>
                  <WarningOutlined style={{ color: '#f5222d' }} />
                  <span>质量告警通知</span>
                  <Tag color="red">重要</Tag>
                </Space>
              }
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name={['email', 'assetChange']}
              valuePropName="checked"
              label="资产变更通知"
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name={['email', 'taskComplete']}
              valuePropName="checked"
              label="任务完成通知"
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name={['email', 'systemNotice']}
              valuePropName="checked"
              label="系统公告"
            >
              <Switch />
            </Form.Item>
          </Card>

          <Divider />

          {/* 短信通知 */}
          <Card
            type="inner"
            title={
              <Space>
                <MessageOutlined style={{ color: '#52c41a' }} />
                <span>短信通知</span>
                <Switch
                  size="small"
                  checked={form.getFieldValue(['sms', 'enabled'])}
                  onChange={(checked) => form.setFieldValue(['sms', 'enabled'], checked)}
                />
              </Space>
            }
            className={styles.innerCard}
          >
            <Form.Item name={['sms', 'enabled']} hidden>
              <Switch />
            </Form.Item>

            <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
              短信通知仅用于紧急和重要消息，以免产生过多费用。
            </Text>

            <Form.Item
              name={['sms', 'qualityAlert']}
              valuePropName="checked"
              label={
                <Space>
                  <WarningOutlined style={{ color: '#f5222d' }} />
                  <span>质量紧急告警</span>
                  <Tag color="red">重要</Tag>
                </Space>
              }
            >
              <Switch />
            </Form.Item>

            <Form.Item
              name={['sms', 'urgentNotice']}
              valuePropName="checked"
              label="紧急系统通知"
            >
              <Switch />
            </Form.Item>
          </Card>

          <Divider />

          {/* 应用内通知 */}
          <Card
            type="inner"
            title={
              <Space>
                <BellOutlined style={{ color: '#faad14' }} />
                <span>应用内通知</span>
                <Switch
                  size="small"
                  checked={form.getFieldValue(['inApp', 'enabled'])}
                  onChange={(checked) => form.setFieldValue(['inApp', 'enabled'], checked)}
                />
              </Space>
            }
            className={styles.innerCard}
          >
            <Form.Item name={['inApp', 'enabled']} hidden>
              <Switch />
            </Form.Item>

            <Form.Item
              name={['inApp', 'all']}
              valuePropName="checked"
              label="接收所有应用内通知"
            >
              <Switch />
            </Form.Item>
          </Card>

          <Divider />

          {/* 通知频率 */}
          <Card type="inner" title="通知频率设置" className={styles.innerCard}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item label="质量报告发送频率" name="reportFrequency">
                  <Space direction="vertical">
                    <Text type="secondary">定期质量报告将通过邮件发送</Text>
                  </Space>
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item label="免打扰时段" name="quietHours">
                  <Space direction="vertical">
                    <Text type="secondary">设置免打扰时段，不发送通知</Text>
                  </Space>
                </Form.Item>
              </Col>
            </Row>
          </Card>
        </Form>
      </Card>
    </div>
  );
};

export default SystemNotifications;
