import {Injectable} from '@angular/core';
import {ClustersApiService} from '../../common/rest/api-services/provider/clusters-api.service';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {ClusterResponse} from '../../common/rest/types/provider/responses/ClusterResponse';


@Injectable({
    providedIn: 'root',
})
export class ClusterService {

    constructor(
        private usersApiService: ClustersApiService,
    ) {
    }

    public getClusters(queryRequest: QueryRequest): Observable<PaginatedResponse<ClusterResponse>> {
        const params = buildQueryParams(queryRequest) as any;
        params.withFriendsInfo = true;
        return this.usersApiService.getClusters(params);
    }

    public getAllClusters(queryRequest: QueryRequest): Observable<ClusterResponse[]>{
        return getAllElementsFromAllPages<ClusterResponse>(this.getClusters.bind(this), queryRequest);

    }

}
