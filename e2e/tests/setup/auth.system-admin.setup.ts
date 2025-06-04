// e2e/tests/setup/auth.system-admin.setup.ts
import { test as setup, expect } from '@playwright/test';

// Path where the authentication state will be saved.
// Make sure this path is gitignored if you don't want to commit the auth state.
const systemAdminStorageStateFile = 'playwright/.auth/system-admin.json';

setup('authenticate as system admin', async ({ page, baseURL }) => {
    // Navigate to the login page of your frontend application.
    // The baseURL is configured in playwright.config.ts.
    await page.goto(`${baseURL}/login`); // Adjust '/login' if your login path is different

    // Fill in the login form.
    // Update selectors to match your actual login form elements.
    await page.fill('input[name="email"]', 'admin@multikube.com'); // System admin email
    await page.fill('input[name="password"]', 'password'); // System admin password

    // Click the login button.
    // Update selector for the login button.
    await page.getByRole('button', { name: "Login" }).click(); // Example selector, adjust as needed

    // Wait for navigation to a post-login page or for a specific element to appear
    // that confirms successful login.
    // Example: Check if the URL changes to a dashboard page.
    await expect(page).toHaveURL(new RegExp(`${baseURL}/provider/dashboard`), { timeout: 10000 }); // Adjust '/dashboard' and timeout
    // Example: Check for a welcome message or a unique element on the dashboard.
    // await expect(page.getByText('Welcome, Admin!')).toBeVisible();

    // Save the authentication state (cookies, localStorage, etc.) to the specified file.
    // This state will be used by other tests to start authenticated.
    await page.context().storageState({ path: systemAdminStorageStateFile });

    console.log(`System admin authentication state saved to ${systemAdminStorageStateFile}`);
});