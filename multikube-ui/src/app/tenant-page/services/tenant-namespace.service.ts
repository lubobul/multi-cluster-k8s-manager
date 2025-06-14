import {Injectable} from '@angular/core';
import {QueryParams, QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';
import {TenantNamespacesApiService} from '../../common/rest/api-services/tenant/tenant-namespaces-api.service';
import {RegisterClusterRequest} from '../../common/rest/types/provider/requests/RegisterClusterRequest';
import {
    TenantNamespaceResponse,
    TenantNamespaceSummaryResponse
} from '../../common/rest/types/tenant/responses/TenantNamespace';
import {CreateNamespaceRequest} from '../../common/rest/types/tenant/requests/CreateNamespaceRequest';

@Injectable({
    providedIn: 'root',
})
export class TenantNamespaceService {

    constructor(
        private clustersApiService: TenantNamespacesApiService,
    ) {
    }

    public createNamespace(namespace: CreateNamespaceRequest): Observable<TenantNamespaceResponse> {
        return this.clustersApiService.createNamespace(namespace);
    }

    public getNamespaces(params: QueryParams | any): Observable<PaginatedResponse<TenantNamespaceSummaryResponse>> {
        return this.clustersApiService.getNamespaces(params);
    }

    public getNamespace(namespaceId: number): Observable<TenantNamespaceResponse> {
        return this.clustersApiService.getNamespace(namespaceId);
    }

    public getAllNamespaceSummaries(queryRequest: QueryRequest): Observable<TenantNamespaceSummaryResponse[]>{
        return getAllElementsFromAllPages<TenantNamespaceSummaryResponse>(this.getNamespaces.bind(this), queryRequest);
    }

}
