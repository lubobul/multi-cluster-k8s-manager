import {Injectable} from '@angular/core';
import {ClustersApiService} from '../../common/rest/api-services/provider/clusters-api.service';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {ClusterResponse} from '../../common/rest/types/provider/responses/ClusterResponse';
import {RegisterClusterRequest} from '../../common/rest/types/provider/requests/RegisterClusterRequest';
import {AllocateClusterRequest} from '../../common/rest/types/provider/requests/AllocateClusterRequest';
import {RestMessageResponse} from '../../common/rest/types/auth-types';


@Injectable({
    providedIn: 'root',
})
export class ClusterService {

    constructor(
        private clustersApiService: ClustersApiService,
    ) {
    }

    public getClusters(queryRequest: QueryRequest): Observable<PaginatedResponse<ClusterResponse>> {
        const params = buildQueryParams(queryRequest) as any;
        return this.clustersApiService.getClusters(params);
    }

    public getCluster(clusterId: number): Observable<ClusterResponse> {
        return this.clustersApiService.getCluster(clusterId);
    }

    public registerCluster(cluster: RegisterClusterRequest): Observable<ClusterResponse>{
        return this.clustersApiService.createCluster(cluster);
    }

    public allocateCluster(cluster: ClusterResponse, clusterAllocation: AllocateClusterRequest): Observable<RestMessageResponse> {
        return this.clustersApiService.allocateCluster(cluster.id, clusterAllocation);
    }

    public deallocateCluster(clusterId: number): Observable<RestMessageResponse> {
        return this.clustersApiService.deallocateCluster(clusterId);
    }

    public getAllClusters(queryRequest: QueryRequest): Observable<ClusterResponse[]>{
        return getAllElementsFromAllPages<ClusterResponse>(this.getClusters.bind(this), queryRequest);

    }

}
