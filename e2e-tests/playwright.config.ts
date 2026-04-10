/**
 * Playwright E2E 测试配置
 */
import { defineConfig, devices } from '@playwright/test';

/**
 * E2E 测试配置
 * 
 * 使用方式:
 * 1. 开发环境: npx playwright test --project=chromium
 * 2. 测试环境: npx playwright test --project=chromium --config=e2e-config.ts
 */
export default defineConfig({
  // 测试目录
  testDir: './e2e-tests',

  // 完整测试时启用
  fullyParallel: true,
  
  // 失败时重试次数
  retries: process.env.CI ? 2 : 0,
  
  // 并行工作线程数
  workers: process.env.CI ? 1 : undefined,

  // 报告器配置
  reporter: [
    ['html', { outputFolder: 'e2e-tests/reports/html', open: 'never' }],
    ['json', { outputFile: 'e2e-tests/reports/json/results.json' }],
    ['list'],
  ],

  // 全局超时配置
  timeout: 60000,
  expect: {
    timeout: 10000,
  },

  // 全局预处理
  use: {
    // 基础 URL
    baseURL: process.env.BASE_URL || 'http://localhost:8000',

    // 追踪配置
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',

    // 截图模式
    screenshot: {
      mode: 'only-on-failure',
      fullPage: true,
    },

    // 默认超时
    actionTimeout: 15000,
    navigationTimeout: 30000,

    // 默认请求头
    extraHTTPHeaders: {
      'X-Request-ID': `${Date.now()}-${Math.random().toString(36).substring(7)}`,
    },
  },

  // 项目配置
  projects: [
    // Chromium 配置
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 1920, height: 1080 },
        launchOptions: {
          args: ['--disable-dev-shm-usage', '--no-sandbox'],
        },
      },
    },

    // Firefox 配置
    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox'],
        viewport: { width: 1920, height: 1080 },
      },
    },

    // WebKit 配置
    {
      name: 'webkit',
      use: {
        ...devices['Desktop Safari'],
        viewport: { width: 1920, height: 1080 },
      },
    },

    // 移动端 Chrome
    {
      name: 'Mobile Chrome',
      use: {
        ...devices['Pixel 5'],
      },
    },

    // 移动端 Safari
    {
      name: 'Mobile Safari',
      use: {
        ...devices['iPhone 12'],
      },
    },
  ],

  // Web 服务器配置
  webServer: process.env.CI ? undefined : {
    command: 'npm run dev',
    url: 'http://localhost:8000',
    reuseExistingServer: true,
    timeout: 120000,
  },
});
