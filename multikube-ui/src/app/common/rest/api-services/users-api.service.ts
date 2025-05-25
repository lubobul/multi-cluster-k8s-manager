import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegisterRequest, LoginRequest, JwtResponse, RestMessageResponse } from '../types/auth-types';
import {PaginatedResponse} from '../types/responses/paginated-response';
import {UserResponse} from '../types/responses/user-response';
import {QueryParams, QueryRequest} from '../types/requests/query-request';
import {buildQueryParams} from '../../utils/util-functions';

@Injectable({
    providedIn: 'root',
})
export class UsersApiService {
    private readonly apiUrl = '/api/users';

    constructor(private http: HttpClient) {}

    getUsers(params: QueryParams | any): Observable<PaginatedResponse<UserResponse>> {
        return this.http.get<PaginatedResponse<UserResponse>>(this.apiUrl, { params });
    }

    getUser(userId: number): Observable<UserResponse> {
        return this.http.get<UserResponse>(`${this.apiUrl}/${userId}`);
    }
}
