import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {CreateWorkloadTemplateRequest} from '../../types/tenant/requests/CreateTemplateRequest';
import {WorkloadTemplate} from '../../types/tenant/responses/WorkloadTemplate';

@Injectable({
    providedIn: 'root',
})
export class TenantTemplatesApiService {
    private readonly apiUrl = '/api/v1/tenant/workload-templates';


    constructor(private http: HttpClient) {}

    createTemplate(template: CreateWorkloadTemplateRequest): Observable<WorkloadTemplate> {
        return this.http.post<WorkloadTemplate>(this.apiUrl, template);
    }

    getTemplates(params: QueryParams | any): Observable<PaginatedResponse<WorkloadTemplate>> {
        return this.http.get<PaginatedResponse<WorkloadTemplate>>(this.apiUrl, { params });
    }

    getTemplate(templateId: number): Observable<WorkloadTemplate> {
        return this.http.get<WorkloadTemplate>(`${this.apiUrl}/${templateId}`);
    }

    deleteTemplate(templateId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${templateId}`);
    }
}
