import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {QueryParams} from '../../types/requests/query-request';
import {PaginatedResponse} from '../../types/responses/paginated-response';
import {CreateCatalogRequest} from '../../types/tenant/requests/CreateCatalogRequest';
import {TemplateCatalog} from '../../types/tenant/responses/TemplateCatalog';

@Injectable({
    providedIn: 'root',
})
export class TenantCatalogsApiService {
    private readonly apiUrl = '/api/v1/tenant/catalogs';


    constructor(private http: HttpClient) {}

    createCatalog(catalog: CreateCatalogRequest): Observable<TemplateCatalog> {
        return this.http.post<TemplateCatalog>(this.apiUrl, catalog);
    }

    getCatalogs(params: QueryParams | any): Observable<PaginatedResponse<TemplateCatalog>> {
        return this.http.get<PaginatedResponse<TemplateCatalog>>(this.apiUrl, { params });
    }

    getCatalog(catalogId: number): Observable<TemplateCatalog> {
        return this.http.get<TemplateCatalog>(`${this.apiUrl}/${catalogId}`);
    }

    deleteCatalog(catalogId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${catalogId}`);
    }
}
