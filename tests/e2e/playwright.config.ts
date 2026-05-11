import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['json', { outputFile: 'playwright-results.json' }],
    ['list']
  ],

  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 15000,
    navigationTimeout: 45000,
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],

  timeout: 120000,
  expect: {
    timeout: 10000
  },

  webServer: {
    command: process.env.CI ? 'npm run build && npm run preview' : 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 180000,
    stdout: 'ignore',
    stderr: 'pipe',
  },

  fullyParallel: true,
  testMatch: '**/*.spec.ts',
});
