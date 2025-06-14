import {Injectable} from '@angular/core';
import {QueryParams, QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {TenantNamespacesApiService} from '../../common/rest/api-services/tenant/tenant-namespaces-api.service';
import {
    TenantNamespaceResponse,
    TenantNamespaceSummaryResponse
} from '../../common/rest/types/tenant/responses/TenantNamespace';
import {CreateNamespaceRequest} from '../../common/rest/types/tenant/requests/CreateNamespaceRequest';
import {NamespaceConfigurationResponse} from '../../common/rest/types/tenant/responses/TenantNamespaceResources';

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

    public getNamespaces(clusterId: number, queryRequest: QueryParams | any): Observable<PaginatedResponse<TenantNamespaceSummaryResponse>> {
        const params = buildQueryParams(queryRequest) as any;
        return this.clustersApiService.getNamespaces(clusterId, params);
    }

    public getNamespace(namespaceId: number): Observable<TenantNamespaceResponse> {
        return this.clustersApiService.getNamespace(namespaceId);
    }

    public getNamespaceConfigurations(namespaceId: number, queryRequest: QueryParams | any): Observable<PaginatedResponse<NamespaceConfigurationResponse>> {
        const params = buildQueryParams(queryRequest) as any;
        return this.clustersApiService.getNamespaceConfigurations(namespaceId, params);
    }

    public getNamespaceConfiguration(namespaceId: number, configurationId: number): Observable<NamespaceConfigurationResponse> {
        return this.clustersApiService.getNamespaceConfiguration(namespaceId, configurationId);
    }

    public getAllNamespaceSummaries(clusterId: number, queryRequest: QueryRequest | any): Observable<TenantNamespaceSummaryResponse[]>{
        return getAllElementsFromAllPages<TenantNamespaceSummaryResponse>(this.getNamespaces.bind(this, clusterId), queryRequest);
    }

}
