import {Injectable} from '@angular/core';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {TenantClustersApiService} from '../../common/rest/api-services/tenant/tenant-clusters-api.service';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';

@Injectable({
    providedIn: 'root',
})
export class TenantClusterService {

    constructor(
        private clustersApiService: TenantClustersApiService,
    ) {
    }

    public getClusters(queryRequest: QueryRequest): Observable<PaginatedResponse<TenantClusterResponse>> {
        const params = buildQueryParams(queryRequest) as any;
        return this.clustersApiService.getClusters(params);
    }

    public getCluster(clusterId: number): Observable<TenantClusterResponse> {
        return this.clustersApiService.getCluster(clusterId);
    }


    public getAllClusters(queryRequest: QueryRequest): Observable<TenantClusterResponse[]>{
        return getAllElementsFromAllPages<TenantClusterResponse>(this.getClusters.bind(this), queryRequest);

    }

}
