import {Injectable} from '@angular/core';
import {QueryParams, QueryRequest} from '../../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {buildQueryParams, getAllElementsFromAllPages} from '../../common/utils/util-functions';
import {TenantTemplatesApiService} from '../../common/rest/api-services/tenant/templates-api.service';
import {CreateWorkloadTemplateRequest} from '../../common/rest/types/tenant/requests/CreateTemplateRequest';
import {WorkloadTemplate} from '../../common/rest/types/tenant/responses/WorkloadTemplate';
@Injectable({
    providedIn: 'root',
})
export class TenantTemplateService {

    constructor(
        private clustersApiService: TenantTemplatesApiService,
    ) {
    }

    createTemplate(template: CreateWorkloadTemplateRequest): Observable<WorkloadTemplate> {
        return this.clustersApiService.createTemplate(template);
    }

    getTemplates(queryRequest: QueryParams | any): Observable<PaginatedResponse<WorkloadTemplate>> {
        const params = buildQueryParams(queryRequest) as any;

        return this.clustersApiService.getTemplates(params)
    }

    getTemplate(templateId: number): Observable<WorkloadTemplate> {
        return this.clustersApiService.getTemplate(templateId);
    }

    deleteTemplate(templateId: number): Observable<void> {
        return this.clustersApiService.deleteTemplate(templateId);
    }

}
