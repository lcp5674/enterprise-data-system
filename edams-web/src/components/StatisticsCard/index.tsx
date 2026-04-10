/**
 * 统计卡片组件
 */
import React from 'react';
import { Card, Statistic, Tooltip } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined, InfoCircleOutlined } from '@ant-design/icons';
import './StatisticsCard.less';

export interface StatisticsCardProps {
  /** 标题 */
  title: string;
  /** 数值 */
  value: number | string;
  /** 数值前缀 */
  prefix?: React.ReactNode;
  /** 数值后缀 */
  suffix?: React.ReactNode;
  /** 精度（小数位数） */
  precision?: number;
  /** 数值变化 */
  change?: {
    value: number;
    trend: 'up' | 'down';
    label?: string;
  };
  /** 提示信息 */
  tooltip?: string;
  /** 图标 */
  icon?: React.ReactNode;
  /** 颜色 */
  color?: string;
  /** 加载状态 */
  loading?: boolean;
  /** 点击事件 */
  onClick?: () => void;
}

const StatisticsCard: React.FC<StatisticsCardProps> = ({
  title,
  value,
  prefix,
  suffix,
  precision,
  change,
  tooltip,
  icon,
  color = '#1890ff',
  loading = false,
  onClick,
}) => {
  return (
    <Card
      className="statistics-card"
      loading={loading}
      hoverable={!!onClick}
      onClick={onClick}
    >
      <div className="statistics-card-content">
        <div className="statistics-card-left">
          <div className="statistics-card-header">
            <span className="statistics-card-title">{title}</span>
            {tooltip && (
              <Tooltip title={tooltip}>
                <InfoCircleOutlined className="statistics-card-info" />
              </Tooltip>
            )}
          </div>
          <div className="statistics-card-value">
            <Statistic
              value={value}
              precision={precision}
              prefix={prefix}
              suffix={suffix}
              valueStyle={{ color: '#333', fontSize: '28px', fontWeight: 600 }}
            />
          </div>
          {change && (
            <div className={`statistics-card-change ${change.trend}`}>
              {change.trend === 'up' ? (
                <ArrowUpOutlined />
              ) : (
                <ArrowDownOutlined />
              )}
              <span>{Math.abs(change.value)}%</span>
              {change.label && <span className="change-label">{change.label}</span>}
            </div>
          )}
        </div>
        {icon && (
          <div className="statistics-card-right" style={{ color }}>
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
};

export default StatisticsCard;
