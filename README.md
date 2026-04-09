# 企业数据资产管理系统 - 快速原型

## 🚀 快速开始

### 环境要求
- Node.js 18+
- npm 或 yarn

### 安装依赖

```bash
cd enterprise-data-mvp
npm install
```

### 配置环境变量

```bash
cp .env.example .env.local
```

编辑 `.env.local` 文件，填入您的 Supabase 项目配置：

```env
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key
```

### 初始化数据库

1. 登录 [Supabase](https://supabase.com)
2. 创建一个新项目
3. 在 SQL Editor 中运行 `supabase/schema.sql` 脚本
4. 获取项目的 URL 和 anon key

### 启动开发服务器

```bash
npm run dev
```

打开 [http://localhost:3000](http://localhost:3000) 查看应用。

### 构建生产版本

```bash
npm run build
npm start
```

## 📁 项目结构

```
enterprise-data-mvp/
├── app/                      # Next.js App Router 页面
│   ├── (auth)/              # 认证相关页面
│   │   └── login/          # 登录页面
│   ├── asset/              # 资产相关页面
│   │   ├── page.tsx        # 数据地图
│   │   └── [id]/           # 资产详情
│   │       └── page.tsx    # 资产详情页
│   ├── lineage/             # 血缘分析页面
│   │   └── page.tsx        # 血缘图
│   ├── feedback/            # 反馈中心
│   │   └── page.tsx        # 反馈页面
│   ├── page.tsx            # 首页工作台
│   ├── layout.tsx          # 根布局
│   └── globals.css         # 全局样式
├── components/             # React 组件
│   ├── ui/                 # 基础 UI 组件
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── input.tsx
│   │   ├── select.tsx
│   │   └── ...
│   ├── layout/             # 布局组件
│   │   ├── header.tsx
│   │   ├── sidebar.tsx
│   │   └── main-layout.tsx
│   ├── dashboard/          # 仪表盘组件
│   │   ├── stat-card.tsx
│   │   └── recent-assets.tsx
│   └── asset/              # 资产相关组件
│       └── asset-card.tsx
├── lib/                    # 工具函数
│   ├── utils.ts
│   └── supabase/
│       ├── client.ts
│       └── server.ts
├── types/                  # TypeScript 类型定义
│   └── index.ts
├── supabase/               # Supabase 配置
│   └── schema.sql          # 数据库初始化脚本
└── public/                 # 静态资源
```

## 🎯 功能模块

### 已实现
- **首页工作台**: 统计概览、最近访问、收藏资产、通知中心
- **数据地图**: 分类目录树、资产卡片/列表视图、筛选搜索
- **资产详情**: 表结构展示、血缘关系、质量报告、元数据
- **血缘分析**: 交互式血缘图谱、上下游追溯、节点详情
- **反馈中心**: 反馈提交、NPS评分、反馈列表
- **用户认证**: 登录页面（支持多种登录方式）

### 即将实现
- [ ] 用户注册
- [ ] 个人设置
- [ ] 资产搜索高级功能
- [ ] 权限管理
- [ ] 数据质量管理
- [ ] 智能问答

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 前端框架 | Next.js 14 (App Router) |
| UI 组件 | shadcn/ui + Radix UI |
| 样式 | Tailwind CSS |
| 状态管理 | Zustand |
| 后端即服务 | Supabase |
| 数据库 | PostgreSQL |
| 认证 | Supabase Auth |
| 部署 | Vercel |

## 📝 开发指南

### 添加新的 UI 组件

推荐使用 shadcn/ui 的组件：

```bash
npx shadcn-ui@latest add [component-name]
```

### 添加新的页面

在 `app/` 目录下创建新的文件夹和 `page.tsx` 文件：

```tsx
// app/new-page/page.tsx
export default function NewPage() {
  return <div>新页面</div>;
}
```

### 数据获取

使用 Supabase 客户端获取数据：

```tsx
import { createClient } from '@/lib/supabase/client';

const supabase = createClient();

const { data, error } = await supabase
  .from('data_assets')
  .select('*');
```

## 🔧 Supabase 配置

### 启用邮箱登录

1. 在 Supabase Dashboard 中进入 Authentication > Providers
2. 启用 Email provider
3. 配置 SMTP 设置（可选，用于发送验证邮件）

### 配置 Row Level Security

项目已包含完整的 RLS 策略，确保数据安全隔离。

## 🚀 部署

### Vercel 部署

1. Fork 此仓库到 GitHub
2. 在 Vercel 中导入项目
3. 配置环境变量
4. 部署

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
