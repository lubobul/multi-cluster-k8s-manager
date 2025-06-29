import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QueryParams } from '../../types/requests/query-request';
import { PaginatedResponse } from '../../types/responses/paginated-response';
import { CreateWorkloadRequest } from '../../types/tenant/requests/CreateWorkloadRequest';
import {WorkloadResponse} from '../../types/tenant/responses/WorkloadResponse';

@Injectable({
    providedIn: 'root',
})
export class TenantWorkloadsApiService {
    private readonly baseUrl = '/api/v1/tenant/namespaces';

    constructor(private http: HttpClient) {}

    /**
     * Creates a new workload instance within a specific namespace.
     * @param namespaceId The ID of the parent namespace.
     * @param workload The request payload containing the workload details.
     * @returns An Observable of the created TenantWorkload.
     */
    createWorkload(namespaceId: number, workload: CreateWorkloadRequest): Observable<WorkloadResponse> {
        const url = `${this.baseUrl}/${namespaceId}/workloads`;
        return this.http.post<WorkloadResponse>(url, workload);
    }

    /**
     * Retrieves a paginated list of workloads for a specific namespace.
     * @param namespaceId The ID of the parent namespace.
     * @param params Query parameters for pagination, sorting, and filtering.
     * @returns An Observable of the paginated workload summaries.
     */
    getWorkloads(namespaceId: number, params: QueryParams | any): Observable<PaginatedResponse<WorkloadResponse>> {
        const url = `${this.baseUrl}/${namespaceId}/workloads`;
        return this.http.get<PaginatedResponse<WorkloadResponse>>(url, { params });
    }

    /**
     * Retrieves the detailed information for a single workload.
     * @param namespaceId The ID of the parent namespace.
     * @param workloadId The ID of the workload to retrieve.
     * @returns An Observable of the detailed TenantWorkload.
     */
    getWorkload(namespaceId: number, workloadId: number): Observable<WorkloadResponse> {
        const url = `${this.baseUrl}/${namespaceId}/workloads/${workloadId}`;
        return this.http.get<WorkloadResponse>(url);
    }

    /**
     * Deletes a specific workload.
     * @param namespaceId The ID of the parent namespace.
     * @param workloadId The ID of the workload to delete.
     * @returns An Observable of void.
     */
    deleteWorkload(namespaceId: number, workloadId: number): Observable<void> {
        const url = `${this.baseUrl}/${namespaceId}/workloads/${workloadId}`;
        return this.http.delete<void>(url);
    }
}
