import {Injectable, inject} from '@angular/core';
import {AuthApiService} from '../common/rest/api-services/auth-api.service';
import {
    JwtResponse,
    LoginRequest,
    RegisterUserRequest, // Ensure this is used if you have a specific RegisterRequest for the backend
    RestMessageResponse,
    UpdateProfileRequest
} from '../common/rest/types/auth-types';
import {BehaviorSubject, Observable, Subject, tap} from 'rxjs';
import {UserResponse} from '../common/rest/types/responses/user-response';
import {ProfileApiService} from '../common/rest/api-services/profile-api.service';
import {Router} from '@angular/router';
import {MULTIKUBE_ROUTE_PATHS} from '../app.routes'; // Assuming app.routes will export this

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    private readonly USER_IDENTITY_KEY = 'USER_IDENTITY';
    private readonly SYSTEM_USER_TENANT_NAME = 'System';
    private currentUserSubject = new BehaviorSubject<UserResponse>(this.getUserIdentityFromStorage());
    public currentUser$ = this.currentUserSubject.asObservable();

    // This subject can be used if other parts of the app need to react to profile updates specifically
    public userProfileUpdate: Subject<UserResponse> = new Subject<UserResponse>();

    private router = inject(Router);

    constructor(
        private authApiService: AuthApiService,
        private profileApiService: ProfileApiService,
    ) {}

    private getUserIdentityFromStorage(): UserResponse {
        const identity = localStorage.getItem(this.USER_IDENTITY_KEY);
        return identity ? JSON.parse(identity) : null;
    }

    public register(request: RegisterUserRequest): Observable<RestMessageResponse> { // Ensure RegisterRequest matches backend
        return this.authApiService.register(request);
    }

    public login(request: LoginRequest): Observable<JwtResponse> {
        return this.authApiService.login(request).pipe(
            tap((response: JwtResponse) => { // Explicitly type response
                this.storeUserIdentity(response.user);
                this.navigateToDashboard(response.user);
            })
        );
    }

    logout(): Observable<void> {
        return this.authApiService.logout().pipe(
            tap(() => {
                this.clearUserIdentity();
                this.router.navigate([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
            })
        );
    }

    updateProfile(request: UpdateProfileRequest): Observable<UserResponse> {
        return this.profileApiService.updateProfile(request).pipe(
            tap((response: UserResponse) => { // Explicitly type response
                this.storeUserIdentity(response);
            })
        );
    }

    deleteProfile(): Observable<void> {
        return this.profileApiService.deleteProfile().pipe(
            tap(() => {
                this.clearUserIdentity();
                this.router.navigate([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
            })
        );
    }

    private storeUserIdentity(userIdentity: UserResponse): void {
        localStorage.setItem(this.USER_IDENTITY_KEY, JSON.stringify(userIdentity));
        this.currentUserSubject.next(userIdentity);
        this.userProfileUpdate.next(userIdentity); // Continue to emit on this if used elsewhere
    }

    public clearUserIdentity(): void {
        localStorage.removeItem(this.USER_IDENTITY_KEY); // Use removeItem for specific key
        this.currentUserSubject.next(null);
    }

    public getUserIdentity(): UserResponse { // Renamed to avoid conflict with getter
        return this.currentUserSubject.value;
    }

    public isLoggedIn(): boolean {
        return !!this.currentUserSubject.value;
    }

    /**
     * Checks if the current user is a Provider Admin (belongs to "System" tenant).
     */
    public isProvider(): boolean {
        const user = this.currentUserSubject.value;
        return !!user && user.tenant?.name === 'System';
    }

    /**
     * Checks if the current user is a regular Tenant user (not "System" tenant).
     */
    public isTenant(): boolean {
        const user = this.currentUserSubject.value;
        return !!user && user.tenant?.name !== 'System';
    }

    /**
     * Navigates the user to the appropriate dashboard based on their tenant type.
     */
    public navigateToDashboard(user: UserResponse | null = this.currentUserSubject.value): void {
        if (!user) {
            this.router.navigate([`/${MULTIKUBE_ROUTE_PATHS.LOGIN}`]);
            return;
        }

        if (user.tenant?.name === this.SYSTEM_USER_TENANT_NAME) {
            // Navigate to a provider-specific landing page, e.g., provider dashboard
            // For now, let's assume a top-level '/provider' route will exist
            this.router.navigate([`/${MULTIKUBE_ROUTE_PATHS.PROVIDER_BASE}`]);
        } else {
            // Navigate to a tenant-specific landing page, e.g., tenant dashboard
            // For now, let's assume a top-level '/tenant' route will exist
            this.router.navigate([`/${MULTIKUBE_ROUTE_PATHS.TENANT_BASE}`]);
        }
    }
}
