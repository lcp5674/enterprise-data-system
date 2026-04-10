/**
 * E2E 测试辅助工具
 */
import { Page, Request, Response } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

/**
 * 加载测试配置
 */
export function loadTestConfig() {
  const configPath = path.join(__dirname, 'e2e-config.json');
  const configData = fs.readFileSync(configPath, 'utf-8');
  return JSON.parse(configData);
}

/**
 * 全局测试配置
 */
export const testConfig = loadTestConfig();

/**
 * 等待指定时间
 */
export async function waitForTimeout(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * 等待元素可见
 */
export async function waitForElementVisible(
  page: Page,
  selector: string,
  timeout: number = 10000
): Promise<void> {
  await page.waitForSelector(selector, { state: 'visible', timeout });
}

/**
 * 等待元素消失
 */
export async function waitForElementHidden(
  page: Page,
  selector: string,
  timeout: number = 10000
): Promise<void> {
  await page.waitForSelector(selector, { state: 'hidden', timeout });
}

/**
 * 点击元素并等待导航
 */
export async function clickAndWaitForNavigation(
  page: Page,
  selector: string,
  options?: { timeout?: number }
): Promise<void> {
  await Promise.all([
    page.waitForNavigation(options),
    page.click(selector),
  ]);
}

/**
 * 填写表单字段
 */
export async function fillFormField(
  page: Page,
  selector: string,
  value: string
): Promise<void> {
  await page.click(selector);
  await page.fill(selector, value);
}

/**
 * 清除并填写表单字段
 */
export async function clearAndFill(
  page: Page,
  selector: string,
  value: string
): Promise<void> {
  await page.click(selector);
  await page.selectText(selector);
  await page.fill(selector, value);
}

/**
 * 等待下拉选项加载
 */
export async function waitForDropdownOptions(
  page: Page,
  selector: string,
  optionsCount: number = 1,
  timeout: number = 5000
): Promise<void> {
  await page.waitForFunction(
    ({ sel, count }) => {
      const options = document.querySelectorAll(sel);
      return options.length >= count;
    },
    { sel: selector, count: optionsCount },
    { timeout }
  );
}

/**
 * 选择下拉选项
 */
export async function selectDropdownOption(
  page: Page,
  triggerSelector: string,
  optionText: string
): Promise<void> {
  await page.click(triggerSelector);
  await page.waitForSelector('.ant-select-dropdown', { state: 'visible' });
  await page.click(`.ant-select-dropdown >> text=${optionText}`);
}

/**
 * 上传文件
 */
export async function uploadFile(
  page: Page,
  selector: string,
  filePath: string
): Promise<void> {
  const input = await page.$(selector);
  if (input) {
    await input.setInputFiles(filePath);
  }
}

/**
 * 截图并保存
 */
export async function takeScreenshot(
  page: Page,
  name: string,
  options?: { fullPage?: boolean }
): Promise<string> {
  const screenshotPath = path.join(
    __dirname,
    'reports',
    'screenshots',
    `${name}-${Date.now()}.png`
  );
  
  // 确保目录存在
  const dir = path.dirname(screenshotPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
  
  await page.screenshot({
    path: screenshotPath,
    fullPage: options?.fullPage ?? true,
  });
  
  return screenshotPath;
}

/**
 * 获取页面标题
 */
export async function getPageTitle(page: Page): Promise<string> {
  return page.title();
}

/**
 * 等待网络空闲
 */
export async function waitForNetworkIdle(
  page: Page,
  timeout: number = 5000
): Promise<void> {
  await page.waitForLoadState('networkidle', { timeout });
}

/**
 * 拦截请求并模拟响应
 */
export async function mockApiResponse(
  page: Page,
  url: string | RegExp,
  response: object,
  status: number = 200
): Promise<void> {
  await page.route(url, (route: Request) => {
    route.fulfill({
      status,
      contentType: 'application/json',
      body: JSON.stringify(response),
    });
  });
}

/**
 * 模拟 API 错误响应
 */
export async function mockApiError(
  page: Page,
  url: string | RegExp,
  errorMessage: string,
  status: number = 400
): Promise<void> {
  await page.route(url, (route: Request) => {
    route.fulfill({
      status,
      contentType: 'application/json',
      body: JSON.stringify({
        code: status,
        message: errorMessage,
        data: null,
      }),
    });
  });
}

/**
 * 等待弹窗出现
 */
export async function waitForModal(
  page: Page,
  selector: string = '.ant-modal',
  timeout: number = 5000
): Promise<void> {
  await page.waitForSelector(selector, { state: 'visible', timeout });
}

/**
 * 关闭弹窗
 */
export async function closeModal(
  page: Page,
  selector: string = '.ant-modal'
): Promise<void> {
  await page.click(`${selector} .ant-modal-close`);
  await page.waitForSelector(selector, { state: 'hidden' });
}

/**
 * 确认对话框
 */
export async function acceptConfirmDialog(page: Page): Promise<void> {
  page.on('dialog', async (dialog) => {
    await dialog.accept();
  });
}

/**
 * 取消对话框
 */
export async function dismissConfirmDialog(page: Page): Promise<void> {
  page.on('dialog', async (dialog) => {
    await dialog.dismiss();
  });
}

/**
 * 获取 Toast 消息内容
 */
export async function getToastMessage(page: Page): Promise<string | null> {
  const toast = await page.$('.ant-message');
  if (toast) {
    const content = await toast.textContent();
    return content;
  }
  return null;
}

/**
 * 等待 Toast 消失
 */
export async function waitForToastDisappear(
  page: Page,
  timeout: number = 3000
): Promise<void> {
  await page.waitForSelector('.ant-message', { state: 'hidden', timeout });
}

/**
 * 滚动到页面顶部
 */
export async function scrollToTop(page: Page): Promise<void> {
  await page.evaluate(() => window.scrollTo(0, 0));
}

/**
 * 滚动到页面底部
 */
export async function scrollToBottom(page: Page): Promise<void> {
  await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
}

/**
 * 滚动到元素位置
 */
export async function scrollToElement(
  page: Page,
  selector: string
): Promise<void> {
  await page.evaluate((sel: string) => {
    const element = document.querySelector(sel);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, selector);
}

/**
 * 检查元素是否存在
 */
export async function isElementExists(
  page: Page,
  selector: string
): Promise<boolean> {
  const element = await page.$(selector);
  return element !== null;
}

/**
 * 检查元素是否可见
 */
export async function isElementVisible(
  page: Page,
  selector: string
): Promise<boolean> {
  const element = await page.$(selector);
  if (element) {
    return await element.isVisible();
  }
  return false;
}

/**
 * 获取元素文本内容
 */
export async function getElementText(
  page: Page,
  selector: string
): Promise<string | null> {
  const element = await page.$(selector);
  if (element) {
    return await element.textContent();
  }
  return null;
}

/**
 * 清除浏览器缓存
 */
export async function clearBrowserCache(page: Page): Promise<void> {
  await page.context().clearCookies();
  await page.context().clearPermissions();
}

/**
 * 最大化窗口
 */
export async function maximizeWindow(page: Page): Promise<void> {
  const viewport = page.viewportSize();
  if (viewport) {
    await page.setViewportSize({
      width: 1920,
      height: 1080,
    });
  }
}

/**
 * 生成测试数据
 */
export function generateTestData(prefix: string = 'test'): object {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(7);
  
  return {
    name: `${prefix}_${timestamp}_${random}`,
    code: `${prefix}_${timestamp}_${random}`.toUpperCase(),
    description: `测试数据 ${timestamp}`,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
}

/**
 * 等待加载指示器消失
 */
export async function waitForLoadingIndicator(
  page: Page,
  timeout: number = 30000
): Promise<void> {
  try {
    await page.waitForSelector('.ant-spin, [class*="loading"]', {
      state: 'hidden',
      timeout,
    });
  } catch (error) {
    console.log('加载指示器未找到或已消失');
  }
}

/**
 * 点击按钮并等待响应
 */
export async function clickAndWaitForResponse(
  page: Page,
  buttonSelector: string,
  urlPattern: string | RegExp,
  timeout: number = 10000
): Promise<Response | null> {
  const [response] = await Promise.all([
    page.waitForResponse(urlPattern, { timeout }),
    page.click(buttonSelector),
  ]);
  return response;
}

/**
 * 双击元素
 */
export async function doubleClick(
  page: Page,
  selector: string
): Promise<void> {
  await page.dblclick(selector);
}

/**
 * 右键点击元素
 */
export async function rightClick(
  page: Page,
  selector: string
): Promise<void> {
  await page.click(selector, { button: 'right' });
}

/**
 * 悬停在元素上
 */
export async function hover(
  page: Page,
  selector: string
): Promise<void> {
  await page.hover(selector);
}

/**
 * 按键盘按键
 */
export async function pressKey(
  page: Page,
  key: string
): Promise<void> {
  await page.keyboard.press(key);
}

/**
 * 按键盘组合键
 */
export async function pressKeyboardShortcut(
  page: Page,
  ...keys: string[]
): Promise<void> {
  await page.keyboard.press(keys.join('+'));
}

/**
 * 导出默认配置
 */
export default {
  loadTestConfig,
  testConfig,
  waitForTimeout,
  waitForElementVisible,
  waitForElementHidden,
  clickAndWaitForNavigation,
  fillFormField,
  clearAndFill,
  waitForDropdownOptions,
  selectDropdownOption,
  uploadFile,
  takeScreenshot,
  getPageTitle,
  waitForNetworkIdle,
  mockApiResponse,
  mockApiError,
  waitForModal,
  closeModal,
  acceptConfirmDialog,
  dismissConfirmDialog,
  getToastMessage,
  waitForToastDisappear,
  scrollToTop,
  scrollToBottom,
  scrollToElement,
  isElementExists,
  isElementVisible,
  getElementText,
  clearBrowserCache,
  maximizeWindow,
  generateTestData,
  waitForLoadingIndicator,
  clickAndWaitForResponse,
  doubleClick,
  rightClick,
  hover,
  pressKey,
  pressKeyboardShortcut,
};
