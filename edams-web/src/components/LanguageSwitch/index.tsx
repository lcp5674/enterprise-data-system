/**
 * 语言切换组件
 */

import React from 'react';
import { Dropdown, Space } from 'antd';
import { GlobalOutlined } from '@ant-design/icons';
import { useAppStore } from '../../stores';
import styles from './index.less';

interface LanguageOption {
  key: string;
  label: string;
  icon?: React.ReactNode;
}

const LanguageSwitch: React.FC = () => {
  const { language, setLanguage } = useAppStore();

  const languageOptions: LanguageOption[] = [
    {
      key: 'zh-CN',
      label: '简体中文',
      icon: '🇨🇳',
    },
    {
      key: 'en-US',
      label: 'English',
      icon: '🇺🇸',
    },
  ];

  const handleLanguageChange = (key: string) => {
    setLanguage(key);
    localStorage.setItem('language', key);
    window.location.reload();
  };

  const items = languageOptions.map((option) => ({
    key: option.key,
    label: (
      <Space>
        <span>{option.icon}</span>
        <span>{option.label}</span>
      </Space>
    ),
  }));

  const currentLanguage = languageOptions.find((opt) => opt.key === language);

  return (
    <Dropdown
      menu={{
        items,
        selectedKeys: [language],
        onClick: ({ key }) => handleLanguageChange(key),
      }}
      trigger={['click']}
    >
      <div className={styles.languageSwitch}>
        <GlobalOutlined className={styles.icon} />
        {currentLanguage && (
          <span className={styles.text}>{currentLanguage.label}</span>
        )}
      </div>
    </Dropdown>
  );
};

export default LanguageSwitch;