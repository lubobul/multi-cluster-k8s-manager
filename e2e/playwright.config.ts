import {defineConfig, devices} from '@playwright/test';

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// import dotenv from 'dotenv';
// import path from 'path';
// dotenv.config({ path: path.resolve(__dirname, '.env') });
export const SYSTEM_ADMIN_STORAGE_STATE = 'playwright/.auth/system-admin.json';

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
    testDir: './tests',
    /* Run tests in files in parallel */
    fullyParallel: true,
    /* Fail the build on CI if you accidentally left test.only in the source code. */
    forbidOnly: !!process.env.CI,
    /* Retry on CI only */
    retries: process.env.CI ? 2 : 0,
    /* Opt out of parallel tests on CI. */
    workers: process.env.CI ? 1 : undefined,
    /* Reporter to use. See https://playwright.dev/docs/test-reporters */
    reporter: 'html',
    /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
    use: {
        /* Base URL to use in actions like `await page.goto('/')`. */
        baseURL: process.env.TEST_ENVIRONMENT,

        /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
        trace: 'retain-on-failure',
        // launchOptions: {
        //     slowMo: 500, // Adds 500ms delay between operations
        // },
    },

    /* Configure projects for major browsers */
    projects: [
        /* Setup Project: System Admin Authentication */
        {
            name: 'setup-system-admin',
            // Only run the auth.system-admin.setup.ts file for this project.
            // Adjust the path if you named your setup file differently or placed it elsewhere.
            testMatch: /setup\/auth\.system-admin\.setup\.ts/,
        },
        /* Provider Test Project: Kubernetes Cluster Registration */
        // This project will focus on testing the Kubernetes cluster registration functionality.
        {
            name: 'provider-cluster-registration', // More descriptive name
            use: {
                trace: "retain-on-failure",
                ...devices['Desktop Chrome'],
                // Use the saved authentication state from the system admin setup.
                storageState: SYSTEM_ADMIN_STORAGE_STATE,
            },
            // This project depends on the 'setup-system-admin' project.
            dependencies: ['setup-system-admin'],
            // Specifically match the 'register-k8s-cluster.ts' file
            // located in the 'tests/provider/' directory.
            testMatch: /provider\/register-k8s-cluster\.ts/,
            // Since testDir is './tests', testMatch will look for 'tests/provider/register-k8s-cluster.ts'
        },
        /* Provider Test Project: Tenant Creation */
        // This project will focus on testing the Tenant creation functionality.
        {
            name: 'provider-tenant-creation', // More descriptive name
            use: {
                trace: "retain-on-failure",
                ...devices['Desktop Chrome'],
                // Use the saved authentication state from the system admin setup.
                storageState: SYSTEM_ADMIN_STORAGE_STATE,
            },
            // This project depends on the 'setup-system-admin' project.
            dependencies: ['setup-system-admin'],
            // located in the 'tests/provider/' directory.
            testMatch: /provider\/create-tenant\.ts/,
        },
        {
            name: 'chromium',
            use: {...devices['Desktop Chrome']},
        },

        /* Test against mobile viewports. */
        // {
        //   name: 'Mobile Chrome',
        //   use: { ...devices['Pixel 5'] },
        // },
        // {
        //   name: 'Mobile Safari',
        //   use: { ...devices['iPhone 12'] },
        // },

        /* Test against branded browsers. */
        // {
        //   name: 'Microsoft Edge',
        //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
        // },
        // {
        //   name: 'Google Chrome',
        //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
        // },
    ],

    /* Run your local dev server before starting the tests */
    // webServer: {
    //   command: 'npm run start',
    //   url: 'http://localhost:3000',
    //   reuseExistingServer: !process.env.CI,
    // },
});
