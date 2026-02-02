import { test, expect } from '@playwright/test';

test('verify dora metrics analysis for helm repo', async ({ page }) => {
  // 1. Navigate to the app
  await page.goto('http://localhost:4200');

  // 2. Check title
  await expect(page.getByRole('heading', { name: 'Dora Metrics Viewer' })).toBeVisible();

  // 3. Fill Repository URL
  const repoInput = page.getByPlaceholder('Enter GitHub Repository URL');
  await repoInput.fill('https://github.com/helm/helm');

  // 4. Fill Dates
  await page.getByLabel('Start Date').fill('2023-01-01');
  await page.getByLabel('End Date').fill('2023-12-31');

  // 5. Click Analyze
  await page.getByRole('button', { name: 'Analyze Metrics' }).click();

  // 6. Wait for loading to finish
  // Expect "Scanning..." to appear
  await expect(page.getByText('Scanning...')).toBeVisible();
  // Expect "Scanning..." to disappear (long timeout as fetching from GitHub might be slow)
  await expect(page.getByText('Scanning...')).toBeHidden({ timeout: 120000 });

  // 7. Check if error appeared (fail test if error is visible)
  const errorMsg = page.locator('.bg-red-100');
  if (await errorMsg.isVisible()) {
    console.log(await errorMsg.textContent());
  }
  await expect(errorMsg).toBeHidden();

  // 8. Verify Charts/Metrics are displayed
  // The charts use these labels in the computed signals
  await expect(page.getByText('Lead Time (Hours)')).toBeVisible();
  await expect(page.getByText('Avg / Day')).toBeVisible();
  await expect(page.getByText('MTTR (Hours)')).toBeVisible();
  
  // Optional: Take a screenshot
  // await page.screenshot({ path: 'verification-result.png' });
});
