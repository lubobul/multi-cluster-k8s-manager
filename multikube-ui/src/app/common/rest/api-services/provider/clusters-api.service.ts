import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {ClusterResponse} from '../../types/provider/responses/ClusterResponse';


@Injectable({
    providedIn: 'root',
})
export class ClustersApiService {
    private readonly apiUrl = '/api/provider/clusters';

    constructor(private http: HttpClient) {}

    getClusters(params: QueryParams | any): Observable<PaginatedResponse<ClusterResponse>> {
        return this.http.get<PaginatedResponse<ClusterResponse>>(this.apiUrl, { params });
    }
}
