import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {TenantResponse} from '../../types/provider/responses/TenantResponse';
import {CreateTenantRequest} from '../../types/provider/requests/CreateTenantRequest';

@Injectable({
    providedIn: 'root',
})
export class TenantsApiService {
    private readonly apiUrl = '/api/provider/tenants';

    constructor(private http: HttpClient) {}

    getTenants(params: QueryParams | any): Observable<PaginatedResponse<TenantResponse>> {
        return this.http.get<PaginatedResponse<TenantResponse>>(this.apiUrl, { params });
    }

    createTenant(tenant: CreateTenantRequest): Observable<TenantResponse> {
        return this.http.post<TenantResponse>(this.apiUrl, tenant);
    }
}
