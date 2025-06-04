import { test, expect } from '@playwright/test';
test.beforeEach( async ({page, baseURL}) => {
    await page.goto(`${baseURL}/provider/tenant-profiles`); // Example URL
})

test.describe('Provider: Tenant creation', () => {
    test('should allow system admin to navigate to tenants page', async ({ page, baseURL }) => {

        // Add an assertion to verify that you are on the correct page.
        // For example, look for a heading or a button specific to cluster management.
        await expect(page.getByRole('button', { name: /Create tenant/i })).toBeVisible();
    });

    test('should not allow system admin to continue to create step without valid inputs', async ({ page, baseURL }) => {
        await page.getByRole('button', { name: /Create tenant/i }).click();
        await page.fill('input[formcontrolname="name"]', '');
        await page.fill('textarea[formcontrolname="description"]', '');

        await page.getByRole('button', { name: /Next/i }).click();


        const nameMissingError = page.locator('clr-control-error').filter({ hasText: 'Organization name field is required.' });
        // To check if it's visible:
        await expect(nameMissingError).toBeVisible();

        const descriptionMissingError = page.locator('clr-control-error').filter({ hasText: 'Tenant description field is required.' });
        // To check if it's visible:
        await expect(descriptionMissingError).toBeVisible();

    });

    const tenantName = "e2eTestTenant";

    test('should allow system admin to register k8s cluster without connectivity', async ({ page, baseURL }) => {
        await page.getByRole('button', { name: /Create tenant/i }).click();
        await page.fill('input[formcontrolname="name"]', tenantName);
        await page.fill('textarea[formcontrolname="description"]', 'tenant description');

        await page.getByRole('button', { name: /Next/i }).click();
        await page.waitForTimeout(1000);


        await page.fill('input[formcontrolname="defaultAdminName"]', 'admin');
        await page.fill('input[formcontrolname="defaultAdminPassword"]', 'password@123');
        await page.fill('input[formcontrolname="repeatDefaultAdminPassword"]', 'password@123');

        const createTenantButtonLocator = page.locator('button[ui-e2e="create-tenant"]');

        await expect(createTenantButtonLocator).toBeVisible();
        await expect(createTenantButtonLocator).toBeEnabled(); // Ensures it's not disabled
        await createTenantButtonLocator.click();

        const cardHeaderLocator = page.locator('clr-dg-cell').filter({ hasText: tenantName });

        // To check if it's visible:
        await expect(cardHeaderLocator).toBeVisible();

    });

});