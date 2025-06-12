import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {ClusterResponse} from '../../types/provider/responses/ClusterResponse';
import {RegisterClusterRequest} from '../../types/provider/requests/RegisterClusterRequest';
import {AllocateClusterRequest} from '../../types/provider/requests/AllocateClusterRequest';
import {RestMessageResponse} from '../../types/auth-types';


@Injectable({
    providedIn: 'root',
})
export class ClustersApiService {
    private readonly apiUrl = '/api/v1/provider/clusters';

    constructor(private http: HttpClient) {}

    getClusters(params: QueryParams | any): Observable<PaginatedResponse<ClusterResponse>> {
        return this.http.get<PaginatedResponse<ClusterResponse>>(this.apiUrl, { params });
    }

    getCluster(clusterId: number): Observable<ClusterResponse> {
        return this.http.get<ClusterResponse>(`${this.apiUrl}/${clusterId}`);
    }

    createCluster(cluster: RegisterClusterRequest): Observable<ClusterResponse> {
        return this.http.post<ClusterResponse>(this.apiUrl, cluster);
    }

    allocateCluster(clusterId: number, clusterAllocation: AllocateClusterRequest): Observable<RestMessageResponse> {
        return this.http.post<RestMessageResponse>(`${this.apiUrl}/${clusterId}/allocation`, clusterAllocation);
    }

    deallocateCluster(clusterId: number): Observable<RestMessageResponse> {
        return this.http.delete<RestMessageResponse>(`${this.apiUrl}/${clusterId}/allocation`);
    }
}
