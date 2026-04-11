/**
 * 404 页面
 */

import React from 'react';
import { Button, Result } from 'antd';
import { history } from '@umijs/max';
import styles from './index.less';

const NotFound: React.FC = () => {
  return (
    <div className={styles.container}>
      <Result
        status="404"
        title="404"
        subTitle="抱歉，您访问的页面不存在。"
        extra={
          <Button type="primary" onClick={() => history.push('/')}>
            返回首页
          </Button>
        }
      />
    </div>
  );
};

export default NotFound;
