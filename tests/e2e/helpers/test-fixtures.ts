import { test as base, Page, expect } from '@playwright/test';

interface TestFixtures {
  authenticatedPage: Page;
  adminPage: Page;
}

export const test = base.extend<TestFixtures>({
  authenticatedPage: async ({ page }, use) => {
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('/dashboard', { timeout: 30000 });
    await use(page);
  },

  adminPage: async ({ browser }, use) => {
    const context = await browser.newContext({
      viewport: { width: 1920, height: 1080 }
    });
    const page = await context.newPage();
    
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('/dashboard', { timeout: 30000 });
    
    await use(page);
    await context.close();
  }
});

export { expect, Page };
