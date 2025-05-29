import { Routes } from '@angular/router';
import { ApplicationHomeComponent } from './application-home/application-home.component';
import { ChatLoginComponent } from './auth-components/chat-login/chat-login.component';
import { ChatRegisterUserComponent } from './auth-components/chat-register-user/chat-register-user.component';
import { MultiKubeWelcomeScreenComponent } from './mltikube-welcome-screen/multi-kube-welcome-screen.component';
import { ProfileSettingsComponent } from './profile-settings/profile-settings.component';

// Placeholder/Actual Components (Import or lazy load as needed)
// Provider components
import { KubernetesClustersComponent } from './provider-page/kubernetes-clusters/kubernetes-clusters.component';
import { TenantProfilesComponent } from './provider-page/tenant-profiles/tenant-profiles.component';
import { TenantUsersComponent } from './provider-page/tenant-users/tenant-users.component';
import {authGuard, nonAuthGuard} from './common/guards/auth.guard';
import {providerGuard, tenantGuard} from './common/guards/scope.guard';

// Tenant components (you'll need to create these or use placeholders)
// Example: import { TenantDashboardComponent } from './tenant-page/dashboard/tenant-dashboard.component';

export const MULTIKUBE_ROUTE_PATHS = {
    LOGIN: 'login',
    REGISTER: 'register',
    PROVIDER_BASE: 'provider', // New base for provider
    TENANT_BASE: 'tenant',     // New base for tenant

    // Provider specific children (can be nested under PROVIDER_BASE)
    PROVIDER_DASHBOARD: 'dashboard', // Welcome screen can be a dashboard
    KUBE_CLUSTERS: 'kubernetes-clusters',
    TENANT_PROFILES: 'tenant-profiles',
    TENANT_USERS: 'tenant-users',
    PROFILE_SETTINGS: 'profile-settings', // Common, but can be part of layouts

    // Tenant specific children (can be nested under TENANT_BASE)
    TENANT_DASHBOARD: 'dashboard',
    TENANT_APPLICATIONS: 'applications',
    // ... other tenant routes

    // Common inside authenticated areas if needed, or specific to provider/tenant
    HOME_WELCOME: 'welcome', // Might be part of provider/tenant dashboard
};

export const routes: Routes = [
    {
        path: MULTIKUBE_ROUTE_PATHS.LOGIN,
        component: ChatLoginComponent,
        canActivate: [nonAuthGuard]
    },
    {
        path: MULTIKUBE_ROUTE_PATHS.REGISTER,
        component: ChatRegisterUserComponent,
        canActivate: [nonAuthGuard]
    },
    {
        path: MULTIKUBE_ROUTE_PATHS.PROVIDER_BASE,
        component: ApplicationHomeComponent, // Or a specific ProviderLayoutComponent
        canActivate: [authGuard, providerGuard], // Must be logged in AND be a provider
        children: [
            { path: '', redirectTo: MULTIKUBE_ROUTE_PATHS.PROVIDER_DASHBOARD, pathMatch: 'full' },
            {
                path: MULTIKUBE_ROUTE_PATHS.PROVIDER_DASHBOARD, // e.g., 'provider/dashboard'
                component: MultiKubeWelcomeScreenComponent // Or a dedicated ProviderDashboardComponent
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.KUBE_CLUSTERS, // e.g., 'provider/kubernetes-clusters'
                component: KubernetesClustersComponent
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.TENANT_PROFILES, // e.g., 'provider/tenant-profiles'
                component: TenantProfilesComponent
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.TENANT_USERS, // e.g., 'provider/tenant-users'
                component: TenantUsersComponent
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.PROFILE_SETTINGS, // e.g., 'provider/profile-settings'
                component: ProfileSettingsComponent
            },
            // ... other provider-specific routes
        ]
    },
    {
        path: MULTIKUBE_ROUTE_PATHS.TENANT_BASE,
        component: ApplicationHomeComponent, // Or a specific TenantLayoutComponent
        canActivate: [authGuard, tenantGuard], // Must be logged in AND be a tenant
        children: [
            { path: '', redirectTo: MULTIKUBE_ROUTE_PATHS.TENANT_DASHBOARD, pathMatch: 'full' },
            {
                path: MULTIKUBE_ROUTE_PATHS.TENANT_DASHBOARD, // e.g., 'tenant/dashboard'
                component: MultiKubeWelcomeScreenComponent // Replace with TenantDashboardComponent
                // loadComponent: () => import('./tenant-page/tenant-dashboard/tenant-dashboard.component').then(m => m.TenantDashboardComponent)
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.PROFILE_SETTINGS, // e.g., 'tenant/profile-settings'
                component: ProfileSettingsComponent
            },
            // ... other tenant-specific routes like 'applications', 'namespaces'
            // Example:
            // {
            //   path: MULTIKUBE_ROUTE_PATHS.TENANT_APPLICATIONS,
            //   loadComponent: () => import('./tenant-page/applications/applications.component').then(m => m.ApplicationsComponent)
            // },
        ]
    },
    {
        path: '',
        redirectTo: MULTIKUBE_ROUTE_PATHS.LOGIN, // Default fallback
        pathMatch: 'full'
    },
    {
        path: '**', // Wildcard for unmatched routes
        redirectTo: MULTIKUBE_ROUTE_PATHS.LOGIN // Or a NotFoundComponent
    }
];
