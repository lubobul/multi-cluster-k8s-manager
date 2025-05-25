import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
    RegisterRequest,
    LoginRequest,
    JwtResponse,
    RestMessageResponse,
    UpdateProfileRequest
} from '../types/auth-types';
import {UserResponse} from '../types/responses/user-response';

@Injectable({
    providedIn: 'root',
})
export class ProfileApiService {
    private readonly apiUrl = '/api/profile';

    constructor(private http: HttpClient) {}

    updateProfile(request: UpdateProfileRequest): Observable<UserResponse> {
        return this.http.put<UserResponse>(this.apiUrl, request);
    }

    deleteProfile(): Observable<void> {
        return this.http.delete<void>(this.apiUrl);
    }
}
