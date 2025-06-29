import {Injectable} from '@angular/core';
import {QueryParams} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams} from '../../common/utils/util-functions';
import {TenantWorkloadsApiService} from '../../common/rest/api-services/tenant/workloads-api.service';
import {CreateWorkloadRequest} from '../../common/rest/types/tenant/requests/CreateWorkloadRequest';
import {WorkloadResponse} from '../../common/rest/types/tenant/responses/WorkloadResponse';
@Injectable({
    providedIn: 'root',
})
export class TenantWorkloadService {

    constructor(
        private clustersApiService: TenantWorkloadsApiService,
    ) {
    }

    createWorkload(namespaceId: number, workload: CreateWorkloadRequest): Observable<WorkloadResponse> {
        return this.clustersApiService.createWorkload(namespaceId, workload);
    }

    getWorkloads(namespaceId: number, queryRequest: QueryParams | any): Observable<PaginatedResponse<WorkloadResponse>> {
        const params = buildQueryParams(queryRequest) as any;

        return this.clustersApiService.getWorkloads(namespaceId, params);
    }

    getWorkload(namespaceId: number, templateId: number): Observable<WorkloadResponse> {
        return this.clustersApiService.getWorkload(namespaceId, templateId);
    }

    deleteWorkload(namespaceId: number, templateId: number): Observable<void> {
        return this.clustersApiService.deleteWorkload(namespaceId, templateId);
    }

}
