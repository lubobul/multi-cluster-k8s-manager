import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../../services/auth.service'; // Adjust path
import { MULTIKUBE_ROUTE_PATHS } from '../../app.routes';

export const providerGuard: CanActivateFn = (): boolean | UrlTree => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isLoggedIn() && authService.isProvider()) {
        return true;
    } else if (authService.isLoggedIn() && !authService.isProvider()) {
        // Logged in but not a provider, redirect to tenant area or a 'forbidden' page
        return router.createUrlTree(['/tenant']); // Or an access-denied page
    }
    // Not logged in or not a provider, redirect to login
    return router.createUrlTree([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
};

export const tenantGuard: CanActivateFn = (): boolean | UrlTree => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isLoggedIn() && authService.isTenant()) {
        return true;
    } else if (authService.isLoggedIn() && !authService.isTenant()) {
        // Logged in but not a tenant (i.e., a provider), redirect to provider area
        return router.createUrlTree(['/provider']); // Or an access-denied page
    }
    // Not logged in or not a tenant, redirect to login
    return router.createUrlTree([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
};
