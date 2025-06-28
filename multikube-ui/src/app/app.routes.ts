import {Routes} from '@angular/router';
import {ProviderHomePageComponent} from './provider-page/provider-home-page.component';
import {MultikubeLoginComponent} from './auth-components/multikube-login/multikube-login.component';
import {ProviderDashboardComponent} from './provider-page/provider-dashboard-component/provider-dashboard.component';
import {ProfileSettingsComponent} from './profile-settings/profile-settings.component';
import {KubernetesClustersComponent} from './provider-page/kubernetes-clusters/kubernetes-clusters.component';
import {TenantProfilesComponent} from './provider-page/tenant-profiles/tenant-profiles.component';
import {TenantUsersComponent} from './provider-page/tenant-users/tenant-users.component';
import {authGuard, nonAuthGuard} from './common/guards/auth.guard';
import {providerGuard, tenantGuard} from './common/guards/scope.guard';
import {ClusterDetailsComponent} from './provider-page/cluster-details/cluster-details.component';
import {ClusterResourcesComponent} from './provider-page/cluster-details/cluster-resources/cluster-resources.component';
import {ClusterScopeComponent} from './provider-page/cluster-details/cluster-scope/cluster-scope.component';
import {ClusterDashboardComponent} from './provider-page/cluster-details/cluster-dashboard/cluster-dashboard.component';
import {TenantHomePageComponent} from './tenant-page/tenant-home-page.component';
import {TenantDashboardComponent} from './tenant-page/tenant-dashboard-component/tenant-dashboard.component';
import {
    TenantKubernetesClustersComponent
} from './tenant-page/kubernetes-clusters/tenant-kubernetes-clusters.component';
import {TenantClusterDetailsComponent} from './tenant-page/cluster-details/tenant-cluster-details.component';
import {
    TenantClusterDashboardComponent
} from './tenant-page/cluster-details/cluster-dashboard/tenant-cluster-dashboard.component';
import {
    ClusterNamespaceComponent
} from './tenant-page/cluster-details/cluster-namespace/cluster-namespace.component';
import {
    CreateClusterNamespaceComponent
} from './tenant-page/cluster-details/create-cluster-namespace/create-cluster-namespace.component';
import {
    NamespaceDetailsComponent
} from './tenant-page/cluster-details/cluster-namespace/namespace-details/namespace-details.component';
import {
    NamespaceWorkloadsComponent
} from './tenant-page/cluster-details/cluster-namespace/namespace-workloads/namespace-workloads.component';
import {
    NamespaceConfigurationComponent
} from './tenant-page/cluster-details/cluster-namespace/namespace-configuration/namespace-configuration.component';
import {TemplateCatalogsComponent} from './tenant-page/template-catalogs/template-catalogs.component';
import {
    WorkloadTemplatesComponent
} from './tenant-page/workload-templates/workload-templates.component';


export const MULTIKUBE_ROUTE_PATHS = {
    LOGIN: 'login',
    PROVIDER_BASE: 'provider', // New base for provider
    TENANT_BASE: 'tenant',     // New base for tenant

    // Provider specific children (can be nested under PROVIDER_BASE)
    PROVIDER_DASHBOARD: 'dashboard', // Welcome screen can be a dashboard
    KUBE_CLUSTERS: 'kubernetes-clusters',
    CLUSTER_ID: 'clusters-id',
    CLUSTER_DETAILS: 'details',
    CLUSTER_DETAILS_CHILDREN: {
        DASHBOARD: "dashboard",
        SCOPE: "scope",
        RESOURCES: "resources",
    },
    TENANT_PROFILES: 'tenant-profiles',
    TENANT_USERS: 'tenant-users',
    PROFILE_SETTINGS: 'profile-settings', // Common, but can be part of layouts
};

export const TENANT_ROUTE_PATHS = {
    TENANT_DASHBOARD: "dashboard",
    KUBE_CLUSTERS: 'kubernetes-clusters',
    CLUSTER_ID: 'clusters-id',
    CLUSTER_DETAILS: 'details',
    CLUSTER_DETAILS_CHILDREN: {
        DASHBOARD: "dashboard",
        NAMESPACE_CREATE: "create-namespace",
        NAMESPACES: "namespaces",
        NAMESPACE_ID: "namespace_id",
        NAMESPACE_DETAILS_CHILDREN: {
            DETAILS: "details",
            WORKLOADS: "workloads",
            CONFIGURATION: "configuration",
        },
    },
    TEMPLATE_CATALOGS: 'template-catalogs',
    WORKLOAD_TEMPLATES: "workload-templates",
};

export const routes: Routes = [
    {
        path: MULTIKUBE_ROUTE_PATHS.LOGIN,
        component: MultikubeLoginComponent,
        canActivate: [nonAuthGuard]
    },
    {
        path: MULTIKUBE_ROUTE_PATHS.PROVIDER_BASE,
        component: ProviderHomePageComponent, // Or a specific ProviderLayoutComponent
        canActivate: [authGuard, providerGuard], // Must be logged in AND be a provider
        children: [
            {path: '', redirectTo: MULTIKUBE_ROUTE_PATHS.PROVIDER_DASHBOARD, pathMatch: 'full'},
            {
                path: MULTIKUBE_ROUTE_PATHS.PROVIDER_DASHBOARD, // e.g., 'provider/dashboard'
                component: ProviderDashboardComponent // Or a dedicated ProviderDashboardComponent
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.KUBE_CLUSTERS, // e.g., 'provider/kubernetes-clusters'
                component: KubernetesClustersComponent
            },
            {
                path: `${MULTIKUBE_ROUTE_PATHS.KUBE_CLUSTERS}/:${MULTIKUBE_ROUTE_PATHS.CLUSTER_ID}`,
                children: [
                    {
                        path: "",
                        redirectTo: MULTIKUBE_ROUTE_PATHS.CLUSTER_DETAILS,
                        pathMatch: "full",
                    },
                    {
                        path: MULTIKUBE_ROUTE_PATHS.CLUSTER_DETAILS,
                        component: ClusterDetailsComponent,
                        children: [
                            {
                                path: "",
                                redirectTo: MULTIKUBE_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.DASHBOARD,
                                pathMatch: "full",
                            },
                            {
                                path: MULTIKUBE_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.DASHBOARD,
                                component: ClusterDashboardComponent,
                            },
                            {
                                path: MULTIKUBE_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.SCOPE,
                                component: ClusterScopeComponent,
                            },
                            {
                                path: MULTIKUBE_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.RESOURCES,
                                component: ClusterResourcesComponent,
                            },
                        ]
                    },
                ]
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
        component: TenantHomePageComponent, // Or a specific TenantLayoutComponent
        canActivate: [authGuard, tenantGuard], // Must be logged in AND be a tenant
        children: [
            {path: '', redirectTo: TENANT_ROUTE_PATHS.TENANT_DASHBOARD, pathMatch: 'full'},
            {
                path: TENANT_ROUTE_PATHS.TENANT_DASHBOARD, // e.g., 'tenant/dashboard'
                component: TenantDashboardComponent // Replace with TenantDashboardComponent
            },
            {
                path: TENANT_ROUTE_PATHS.KUBE_CLUSTERS, // e.g., 'provider/kubernetes-clusters'
                component: TenantKubernetesClustersComponent
            },
            {
                path: `${TENANT_ROUTE_PATHS.KUBE_CLUSTERS}/:${TENANT_ROUTE_PATHS.CLUSTER_ID}`,
                children: [
                    {
                        path: "",
                        redirectTo: TENANT_ROUTE_PATHS.CLUSTER_DETAILS,
                        pathMatch: "full",
                    },
                    {
                        path: TENANT_ROUTE_PATHS.CLUSTER_DETAILS,
                        component: TenantClusterDetailsComponent,
                        children: [
                            {
                                path: "",
                                redirectTo: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.DASHBOARD,
                                pathMatch: "full",
                            },
                            {
                                path: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.DASHBOARD,
                                component: TenantClusterDashboardComponent,
                            },
                            {
                                path: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_CREATE,
                                component: CreateClusterNamespaceComponent,
                            },
                            {
                                path: `${TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACES}/:${TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_ID}`,
                                children: [
                                    {
                                        path: "",
                                        redirectTo: "",
                                        pathMatch: "full",
                                    },
                                    {
                                        path: "",
                                        component: ClusterNamespaceComponent,
                                        children: [
                                            {
                                                path: "",
                                                redirectTo: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_DETAILS_CHILDREN.DETAILS,
                                                pathMatch: "full",
                                            },
                                            {
                                                path: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_DETAILS_CHILDREN.DETAILS,
                                                component: NamespaceDetailsComponent,
                                            },
                                            {
                                                path: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_DETAILS_CHILDREN.WORKLOADS,
                                                component: NamespaceWorkloadsComponent,
                                            },
                                            {
                                                path: TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_DETAILS_CHILDREN.CONFIGURATION,
                                                component: NamespaceConfigurationComponent,
                                            },
                                        ]
                                    },
                                ]
                            },
                        ]
                    },
                ]
            },
            {
                path: TENANT_ROUTE_PATHS.TEMPLATE_CATALOGS, // e.g., 'provider/kubernetes-clusters'
                component: TemplateCatalogsComponent
            },
            {
                path: TENANT_ROUTE_PATHS.WORKLOAD_TEMPLATES, // e.g., 'provider/kubernetes-clusters'
                component: WorkloadTemplatesComponent
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
