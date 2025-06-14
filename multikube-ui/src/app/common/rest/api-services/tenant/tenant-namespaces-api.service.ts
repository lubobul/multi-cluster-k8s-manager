import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {TenantNamespaceResponse, TenantNamespaceSummaryResponse} from '../../types/tenant/responses/TenantNamespace';
import {CreateNamespaceRequest} from '../../types/tenant/requests/CreateNamespaceRequest';
import {NamespaceConfigurationResponse} from '../../types/tenant/responses/TenantNamespaceResources';

@Injectable({
    providedIn: 'root',
})
export class TenantNamespacesApiService {
    private readonly apiUrl = '/api/v1/tenant/namespaces';

    private configurationsUrl(namespaceId: number): string {
        return `${this.apiUrl}/${namespaceId}/configurations`;
    }

    constructor(private http: HttpClient) {}

    createNamespace(cluster: CreateNamespaceRequest): Observable<TenantNamespaceResponse> {
        return this.http.post<TenantNamespaceResponse>(this.apiUrl, cluster);
    }

    getNamespaces(clusterId: number, params: QueryParams | any): Observable<PaginatedResponse<TenantNamespaceSummaryResponse>> {
        params.clusterId = clusterId;
        return this.http.get<PaginatedResponse<TenantNamespaceSummaryResponse>>(this.apiUrl, { params });
    }

    getNamespace(namespaceId: number): Observable<TenantNamespaceResponse> {
        return this.http.get<TenantNamespaceResponse>(`${this.apiUrl}/${namespaceId}`);
    }

    getNamespaceConfigurations(namespaceId: number, params: QueryParams | any): Observable<PaginatedResponse<NamespaceConfigurationResponse>> {
        return this.http.get<PaginatedResponse<NamespaceConfigurationResponse>>(this.configurationsUrl(namespaceId), { params });
    }

    getNamespaceConfiguration(namespaceId: number, configurationId: number): Observable<NamespaceConfigurationResponse> {
        return this.http.get<NamespaceConfigurationResponse>(`${this.configurationsUrl(namespaceId)}/${configurationId}`);
    }
}
