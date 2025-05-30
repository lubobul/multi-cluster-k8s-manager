import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
    RegisterUserRequest,
    LoginRequest,
    JwtResponse,
    RestMessageResponse,
    UpdateProfileRequest
} from '../types/auth-types';
import {UserResponse} from '../types/responses/user-response';

@Injectable({
    providedIn: 'root',
})
export class AuthApiService {
    private readonly apiUrl = '/api/auth';

    constructor(private http: HttpClient) {}

    register(request: RegisterUserRequest): Observable<RestMessageResponse> {
        return this.http.post<RestMessageResponse>(`${this.apiUrl}/register`, request);
    }

    login(request: LoginRequest): Observable<JwtResponse> {
        return this.http.post<JwtResponse>(`${this.apiUrl}/login`, request);
    }

    logout(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/logout`, null);
    }
}
