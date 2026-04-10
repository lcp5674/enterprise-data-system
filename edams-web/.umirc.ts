import { defineConfig } from '@umijs/max';
import { resolve } from 'path';

export default defineConfig({
  alias: {
    '@': resolve(__dirname, 'src'),
    '@@': resolve(__dirname, 'src/.umi'),
  },
  
  // 应用基础配置
  base: '/',
  publicPath: '/',
  
  // 输出配置
  outputPath: './dist',
  
  // 运行时配置
  runtimePublicPath: true,
  
  // 浏览器兼容
  targets: {
    chrome: 80,
    firefox: 70,
    safari: 13,
    edge: 80,
  },
  
  // 代理配置
  proxy: {
    '/api': {
      target: process.env.API_BASE_URL || 'http://localhost:8888',
      changeOrigin: true,
      secure: false,
    },
  },
  
  // 插件配置
  plugins: [
    '@umijs/plugins/plugins/ant-design',
    '@umijs/plugins/plugins/access',
    '@umijs/plugins/plugins/initial-state',
    '@umijs/plugins/plugins/model',
    '@umijs/plugins/plugins/request',
    '@umijs/plugins/plugins/layout',
  ],
  
  // 主题配置
  theme: {
    'primary-color': '#1890ff',
    'link-color': '#1890ff',
    'success-color': '#52c41a',
    'warning-color': '#faad14',
    'error-color': '#f5222d',
    'font-size-base': '14px',
    'border-radius-base': '4px',
  },
  
  // 国际化
  locale: {
    default: 'zh-CN',
    antd: true,
    baseSeparator: '-',
  },
  
  // 权限配置
  access: {},
  
  // 初始状态配置
  initialState: {},
  
  // 请求配置
  request: {},
  
  // 布局配置
  layout: {
    title: '企业数据资产管理系统',
    logo: '/logo.svg',
    locale: true,
  },
  
  // 模型配置
  model: {},
  
  // 路由配置
  routes: [
    {
      path: '/user',
      layout: false,
      routes: [
        { path: '/user/login', component: 'user/Login' },
        { path: '/user/register', component: 'user/Register' },
        { path: '/user/forgot-password', component: 'user/ForgotPassword' },
        { path: '/user/mfa', component: 'user/MFA' },
      ],
    },
    {
      path: '/',
      component: '@/layouts/BasicLayout',
      routes: [
        { path: '/', redirect: '/home' },
        { path: '/home', component: 'Home' },
        {
          path: '/assets',
          name: '资产管理',
          icon: 'DatabaseOutlined',
          routes: [
            { path: '/assets/list', component: 'Assets/AssetList', name: '资产列表' },
            { path: '/assets/detail/:id', component: 'Assets/AssetDetail', name: '资产详情', hideInMenu: true },
            { path: '/assets/create', component: 'Assets/AssetCreate', name: '注册资产' },
            { path: '/assets/favorites', component: 'Assets/AssetFavorites', name: '我的收藏' },
          ],
        },
        {
          path: '/catalog',
          name: '数据目录',
          icon: 'FolderOutlined',
          routes: [
            { path: '/catalog/tree', component: 'Catalog/Tree', name: '目录树' },
            { path: '/catalog/domain', component: 'Catalog/Domain', name: '业务域' },
          ],
        },
        {
          path: '/lineage',
          name: '数据地图',
          icon: 'ShareAltOutlined',
          routes: [
            { path: '/lineage/graph', component: 'Lineage/Graph', name: '血缘图' },
            { path: '/lineage/impact', component: 'Lineage/Impact', name: '影响分析' },
          ],
        },
        {
          path: '/quality',
          name: '质量管理',
          icon: 'CheckCircleOutlined',
          routes: [
            { path: '/quality/overview', component: 'Quality/Overview', name: '质量概览' },
            { path: '/quality/rules', component: 'Quality/Rules', name: '质量规则' },
            { path: '/quality/reports', component: 'Quality/Reports', name: '质量报告' },
            { path: '/quality/issues', component: 'Quality/Issues', name: '问题追踪' },
          ],
        },
        {
          path: '/system',
          name: '系统管理',
          icon: 'SettingOutlined',
          access: 'canAdmin',
          routes: [
            { path: '/system/users', component: 'System/Users', name: '用户管理' },
            { path: '/system/roles', component: 'System/Roles', name: '角色权限' },
            { path: '/system/datasources', component: 'System/Datasources', name: '数据源配置' },
            { path: '/system/notifications', component: 'System/Notifications', name: '通知设置' },
          ],
        },
      ],
    },
    { path: '*', component: '404' },
  ],
  
  // Mock配置
  mock: false,
  
  // 样式配置
  cssLoader: {},
  cssLoaderModules: {
    prefix: 'edams',
  },
  
  // CSS预处理器
  cssLoaderTransform: true,
  cssLoaderMode: 'local',
  
  // Less配置
  lessLoader: {
    javascriptEnabled: true,
    modifyVars: {},
  },
  
  // 图片处理
  inlineStyleLimit: 4096,
  
  // 代码分割
  codeSplitting: {
    jsStrategy: 'granularChunks',
  },
  
  // 外部依赖
  externals: {
    '@antv/g6': 'window.G6',
  },
  
  // Scripts
  scripts: [],
  
  // Monorepo配置
  monorepoRedirect: false,
  
  // 忽略Moment.js的locale文件
  ignoreMomentLocale: true,
  
  // 启用PWA
  pwa: false,
  
  // 快速刷新
  fastRefresh: true,
  
  // 开发者工具
  devtool: process.env.NODE_ENV === 'development' ? 'eval-source-map' : false,
  
  // 严格模式
  reactStrictMode: true,
  
  // 分析模式
  analyze: process.env.ANALYZE === 'true',
  
  // 配置加载
  configCommit: false,
  
  //terser配置
  terserOptions: {
    compress: {
      drop_console: true,
      drop_debugger: true,
    },
  },
  
  // Hash后缀
  hash: true,
  
  // sourcemap
  sourcemap: process.env.NODE_ENV === 'development',
  
  // 预渲染
  prerender: {
    routes: ['/'],
    // root: 'ssr',
  },
});
