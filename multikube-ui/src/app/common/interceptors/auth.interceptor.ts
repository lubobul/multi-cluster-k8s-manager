import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth.service'; // Ensure this path is correct
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { MULTIKUBE_ROUTE_PATHS } from '../../app.routes'; // Ensure this path is correct

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const modifiedReq = req.clone({
        withCredentials: true, // Include cookies with every request
    });
    return next(modifiedReq);
};

export const loginRedirectInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401) {
                // Unauthorized: Clear identity (if any was attempted to be set by a failing request)
                // and redirect to login. AuthService.logout() handles this well if appropriate.
                // Or, more directly if logout performs other actions not desired here:
                // authService.clearUserIdentity(); // Assuming you add such a method or handle it
                router.navigate([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
            } else if (error.status === 403) {
                // Forbidden: User is authenticated but not authorized for the specific resource.
                // You might want to navigate to an "access-denied" page or back to their dashboard.
                console.error('Access denied (403). You do not have permission to perform this action.');
                // Potentially redirect to their dashboard:
                // authService.navigateToDashboard();
                // Or a dedicated access-denied page:
                // router.navigate(['/access-denied']);
            } else if (error.status === 404) {
                // Not Found: The requested API endpoint doesn't exist.
                // Navigate the user to their appropriate dashboard or login page.
                // The authService.navigateToDashboard() will handle whether they are
                // logged in (and go to provider/tenant dashboard) or not (and go to login).
                console.warn('API endpoint not found (404). Navigating to appropriate dashboard/login.');
                authService.navigateToDashboard();
            }
            return throwError(() => error);
        })
    );
};
