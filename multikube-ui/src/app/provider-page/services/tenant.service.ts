import {Injectable} from '@angular/core';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {TenantsApiService} from '../../common/rest/api-services/provider/tenants-api.service';
import {TenantResponse} from '../../common/rest/types/provider/responses/TenantResponse';
import {CreateTenantRequest} from '../../common/rest/types/provider/requests/CreateTenantRequest';


@Injectable({
    providedIn: 'root',
})
export class TenantService {

    constructor(
        private usersApiService: TenantsApiService,
    ) {
    }

    public getTenants(queryRequest: QueryRequest): Observable<PaginatedResponse<TenantResponse>> {
        const params = buildQueryParams(queryRequest) as any;
        params.withFriendsInfo = true;
        return this.usersApiService.getTenants(params);
    }

    public createTenant(cluster: CreateTenantRequest): Observable<TenantResponse>{
        return this.usersApiService.createTenant(cluster);
    }

}
