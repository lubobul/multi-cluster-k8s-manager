import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {TenantClusterResponse} from '../../types/tenant/responses/TenantClusterResponse';


@Injectable({
    providedIn: 'root',
})
export class TenantClustersApiService {
    private readonly apiUrl = '/api/v1/tenant/clusters';

    constructor(private http: HttpClient) {}

    getClusters(params: QueryParams | any): Observable<PaginatedResponse<TenantClusterResponse>> {
        return this.http.get<PaginatedResponse<TenantClusterResponse>>(this.apiUrl, { params });
    }

    getCluster(clusterId: number): Observable<TenantClusterResponse> {
        return this.http.get<TenantClusterResponse>(`${this.apiUrl}/${clusterId}`);
    }
}
