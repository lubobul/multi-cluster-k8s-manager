import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import { inject } from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {Router} from '@angular/router';
import {catchError, throwError} from 'rxjs';
import {MULTIKUBE_ROUTE_PATHS} from '../../app.routes';

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
                // Clear JWT and redirect to login on authentication failure
                router.navigate([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
            } else if (error.status === 403) {
                // Optional: Show a forbidden message or handle gracefully
                console.error('Access denied. You do not have permission to perform this action.');
            } else if (error.status === 404) {
                // Optional: Show a forbidden message or handle gracefully
                router.navigate([`/${MULTIKUBE_ROUTE_PATHS.HOME}`]);
            }
            return throwError(() => error);
        })
    );
};
