import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../../services/auth.service'; // Adjust path
import { MULTIKUBE_ROUTE_PATHS } from '../../app.routes';

export const authGuard: CanActivateFn = (): boolean | UrlTree => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isLoggedIn()) {
        return true;
    }
    return router.createUrlTree([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
};

export const nonAuthGuard: CanActivateFn = (): boolean | UrlTree => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isLoggedIn()) {
        // User is already logged in, redirect them away from login/register
        if (authService.isProvider()) {
            return router.createUrlTree(['/provider']);
        } else if (authService.isTenant()) {
            return router.createUrlTree(['/tenant']);
        }
        // Fallback if role is unclear but logged in (should ideally not happen)
        return router.createUrlTree(['/']); // Or a generic authenticated landing page
    }
    return true; // Not logged in, allow access
};
