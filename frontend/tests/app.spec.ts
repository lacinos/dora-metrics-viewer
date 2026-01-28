import { test, expect } from '@playwright/test';

test('Application Loads', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveTitle(/DoraMetricsViewer/);
  await expect(page.getByRole('heading', { name: 'DORA Metrics Viewer' })).toBeVisible();
  await expect(page.getByPlaceholder('Enter GitHub Repository URL')).toBeVisible();
});