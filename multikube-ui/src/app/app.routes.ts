import { Routes } from '@angular/router';
import {ApplicationHomeComponent} from './application-home/application-home.component';
import {ChatLoginComponent} from './auth-components/chat-login/chat-login.component';
import {ChatRegisterUserComponent} from './auth-components/chat-register-user/chat-register-user.component';
import {TenantUsersComponent} from './provider-page/tenant-users/tenant-users.component';
import {ProfileSettingsComponent} from './profile-settings/profile-settings.component';
import {MultiKubeWelcomeScreenComponent} from './mltikube-welcome-screen/multi-kube-welcome-screen.component';
import {ChatHomeComponent} from './application-home/chat-home/chat-home.component';
import {TenantProfilesComponent} from './provider-page/tenant-profiles/tenant-profiles.component';
import {KubernetesClustersComponent} from './provider-page/kubernetes-clusters/kubernetes-clusters.component';

export const MULTIKUBE_ROUTE_PATHS = {
    HOME: "home",
    HOME_WELCOME: "welcome",
    KUBE_CLUSTERS: "kubernetes-clusters",
    KUBE_ID: "kube_id",
    KUBE_DETAILS: "details",
    TENANT_PROFILES: "tenant-profiles",
    TENANT_USERS: "tenant-users",
    LOGIN: "login",
    REGISTER: "register",
    PROFILE_SETTINGS: "profile",
}

export const routes: Routes = [
    {
        path: "",
        children: [
            {
                path: "",
                redirectTo: MULTIKUBE_ROUTE_PATHS.HOME,
                pathMatch: "full",
            },
            {
                path: MULTIKUBE_ROUTE_PATHS.HOME,
                component: ApplicationHomeComponent,
                children: [
                    {
                        path: "",
                        redirectTo: MULTIKUBE_ROUTE_PATHS.HOME_WELCOME,
                        pathMatch: "full",
                    },
                    {
                        component: MultiKubeWelcomeScreenComponent,
                        path: MULTIKUBE_ROUTE_PATHS.HOME_WELCOME
                    },
                    {
                        component: KubernetesClustersComponent,
                        path: MULTIKUBE_ROUTE_PATHS.KUBE_CLUSTERS
                        canActivate
                    },
                    {
                        path: `${MULTIKUBE_ROUTE_PATHS.KUBE_CLUSTERS}/:${MULTIKUBE_ROUTE_PATHS.KUBE_ID}`,
                        children: [
                            {
                                path: "",
                                redirectTo: MULTIKUBE_ROUTE_PATHS.KUBE_DETAILS,
                                pathMatch: "full",
                            },
                            {
                                path: MULTIKUBE_ROUTE_PATHS.KUBE_DETAILS,
                                component: ChatHomeComponent,
                                // children: [
                                //     {
                                //         path: "",
                                //         redirectTo: CHAT_ROUTE_PATHS.CHAT_CORRESPONDENCE,
                                //         pathMatch: "full",
                                //     },
                                //     {
                                //         path: CHAT_ROUTE_PATHS.CHAT_CORRESPONDENCE,
                                //         component: ChatCorrespondenceComponent,
                                //     },
                                //     {
                                //         path: CHAT_ROUTE_PATHS.CHAT_PARTICIPANTS,
                                //         component: ChannelParticipantsComponent,
                                //     },
                                //     {
                                //         path: CHAT_ROUTE_PATHS.CHAT_SETTINGS,
                                //         component: ChannelSettingsComponent,
                                //     },
                                // ]
                            },
                        ]
                    },
                    {
                        component: TenantProfilesComponent,
                        path: MULTIKUBE_ROUTE_PATHS.TENANT_PROFILES
                    },
                    {
                        component: TenantUsersComponent,
                        path: MULTIKUBE_ROUTE_PATHS.TENANT_USERS
                    },
                    {
                        component: ProfileSettingsComponent,
                        path: MULTIKUBE_ROUTE_PATHS.PROFILE_SETTINGS
                    },
                ]
            },
            {
                component: ChatLoginComponent,
                path: MULTIKUBE_ROUTE_PATHS.LOGIN
            },
            {
                component: ChatRegisterUserComponent,
                path: MULTIKUBE_ROUTE_PATHS.REGISTER
            },
        ]
    }
];
