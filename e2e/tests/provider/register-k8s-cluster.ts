import { test, expect } from '@playwright/test';
test.beforeEach( async ({page, baseURL}) => {
    await page.goto(`${baseURL}/provider/kubernetes-clusters`); // Example URL
})

test.describe('Provider: Kubernetes Cluster Registration', () => {
    test('should allow system admin to navigate to clusters page', async ({ page, baseURL }) => {

        // Add an assertion to verify that you are on the correct page.
        // For example, look for a heading or a button specific to cluster management.
        await expect(page.getByRole('button', { name: /Register Cluster/i })).toBeVisible();
    });

    test('should not allow system admin to continue to register step without valid inputs', async ({ page, baseURL }) => {
        await page.getByRole('button', { name: /Register Cluster/i }).click();
        await page.fill('input[formcontrolname="name"]', '');
        await page.fill('textarea[formcontrolname="description"]', '');

        await page.getByRole('button', { name: /Next/i }).click();


        const nameMissingError = page.locator('clr-control-error').filter({ hasText: 'Cluster name field is required.' });
        // To check if it's visible:
        await expect(nameMissingError).toBeVisible();

        const descriptionMissingError = page.locator('clr-control-error').filter({ hasText: 'Cluster description field is required.' });
        // To check if it's visible:
        await expect(descriptionMissingError).toBeVisible();

    });

    const clusterName = "e2e-test-k8s-registration";

    test('should allow system admin to register k8s cluster without connectivity', async ({ page, baseURL }) => {
        await page.getByRole('button', { name: /Register Cluster/i }).click();
        await page.fill('input[formcontrolname="name"]', clusterName);
        await page.fill('textarea[formcontrolname="description"]', 'e2e-test-k8s-registration description');

        await page.getByRole('button', { name: /Next/i }).click();
        await page.waitForTimeout(1000);
        await page.getByRole('button', { name: "Register", exact: true }).click();

        const cardHeaderLocator = page.locator('div.card-header').filter({ hasText: clusterName });

        // To check if it's visible:
        await expect(cardHeaderLocator).toBeVisible();

    });

});