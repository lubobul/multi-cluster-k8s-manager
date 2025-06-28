import {Injectable} from '@angular/core';
import {QueryParams, QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {TenantClustersApiService} from '../../common/rest/api-services/tenant/tenant-clusters-api.service';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';
import {TenantCatalogsApiService} from '../../common/rest/api-services/tenant/catalogs-api.service';
import {CreateCatalogRequest} from '../../common/rest/types/tenant/requests/CreateCatalogRequest';
import {TemplateCatalog} from '../../common/rest/types/tenant/responses/TemplateCatalog';

@Injectable({
    providedIn: 'root',
})
export class TenantCatalogService {

    constructor(
        private clustersApiService: TenantCatalogsApiService,
    ) {
    }

    createCatalog(catalog: CreateCatalogRequest): Observable<TemplateCatalog> {
        return this.clustersApiService.createCatalog(catalog);
    }

    getCatalogs(queryRequest: QueryParams | any): Observable<PaginatedResponse<TemplateCatalog>> {
        const params = buildQueryParams(queryRequest) as any;

        return this.clustersApiService.getCatalogs(params)
    }

    getCatalog(catalogId: number): Observable<TemplateCatalog> {
        return this.clustersApiService.getCatalog(catalogId);
    }

    deleteCatalog(catalogId: number): Observable<void> {
        return this.clustersApiService.deleteCatalog(catalogId);
    }

}
