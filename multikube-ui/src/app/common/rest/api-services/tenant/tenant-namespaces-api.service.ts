import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {TenantNamespaceResponse, TenantNamespaceSummaryResponse} from '../../types/tenant/responses/TenantNamespace';
import {CreateNamespaceRequest} from '../../types/tenant/requests/CreateNamespaceRequest';

@Injectable({
    providedIn: 'root',
})
export class TenantNamespacesApiService {
    private readonly apiUrl = '/api/v1/tenant/namespaces';

    constructor(private http: HttpClient) {}

    createNamespace(cluster: CreateNamespaceRequest): Observable<TenantNamespaceResponse> {
        return this.http.post<TenantNamespaceResponse>(this.apiUrl, cluster);
    }

    getNamespaces(params: QueryParams | any): Observable<PaginatedResponse<TenantNamespaceSummaryResponse>> {
        return this.http.get<PaginatedResponse<TenantNamespaceSummaryResponse>>(this.apiUrl, { params });
    }

    getNamespace(namespaceId: number): Observable<TenantNamespaceResponse> {
        return this.http.get<TenantNamespaceResponse>(`${this.apiUrl}/${namespaceId}`);
    }
}
