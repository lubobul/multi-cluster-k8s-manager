import {
    importProvidersFrom,
    provideZoneChangeDetection,
    Renderer2
} from '@angular/core';
import {provideRouter, RouterModule} from '@angular/router';

import {routes} from './app.routes';
import {provideHttpClient, withInterceptors, withInterceptorsFromDi} from '@angular/common/http';
import {authInterceptor, loginRedirectInterceptor} from './common/interceptors/auth.interceptor';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ClarityModule, ClrInputModule, ClrPasswordModule} from '@clr/angular';
import {JWT_OPTIONS, JwtHelperService} from '@auth0/angular-jwt';
import '@cds/core/icon/register.js';
import * as allIcons from '@cds/core/icon';
import {ClarityIcons, chatBubbleIcon, talkBubblesIcon, usersIcon,} from '@cds/core/icon';
import {MonacoEditorModule, NGX_MONACO_EDITOR_CONFIG} from 'ngx-monaco-editor-v2';

ClarityIcons.addIcons(
    ...allIcons.coreCollectionIcons,
    ...allIcons.socialCollectionIcons,
    ...allIcons.essentialCollectionIcons,
    ...allIcons.technologyCollectionIcons,
);
export const appConfig: { providers: any[] } = {
    providers:
        [
            provideZoneChangeDetection({eventCoalescing: true}),
            provideRouter(routes),
            provideHttpClient(
                withInterceptors([
                    authInterceptor,
                    loginRedirectInterceptor,
                ]),
                withInterceptorsFromDi()
            ),
            importProvidersFrom(
                BrowserModule,
                BrowserAnimationsModule,
                ClarityModule, // Import ClarityModule
                ClrInputModule, // Import ClrInputModule
                ClrPasswordModule, // Import ClrPasswordModule
                MonacoEditorModule.forRoot()
            ),
            {provide: JWT_OPTIONS, useValue: JWT_OPTIONS},
            JwtHelperService,
            Renderer2,
            {
                provide: NGX_MONACO_EDITOR_CONFIG,
                useFactory: () => ({
                    baseUrl: `assets/monaco/min/vs`,
                }),
            },
        ],
};
